package org.narrative.common.core.services;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.integrator.ListenerIntegrator;
import org.narrative.common.persistence.hibernate.integrator.MetadataExtractorIntegrator;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.system.Encryption;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.NamingHelper;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;

import javax.persistence.PrimaryKeyJoinColumn;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Jun 5, 2006
 * Time: 5:31:26 PM
 * <p>
 * A class designed to allow us to exclude certain SQL statements during the script creation process.
 *
 * @author Paul
 */
public abstract class NarrativeAnnotationConfiguration extends Configuration {
    private static final NarrativeLogger LOG = new NarrativeLogger(NarrativeAnnotationConfiguration.class);
    public static final Set<String> MYISAM_TABLES = Collections.unmodifiableSet((newHashSet("PostalCode", "GeoIPLocation")));
    private static final Set<String> excludeStatementWithFragment = newHashSet();
    private static final Pattern AUTOINC_REPL_PATTERN = Pattern.compile("(.*\\s)(\\w+\\W+)(\\w+\\W+)(\\w+\\W+)(\\w+\\W+)(auto_increment)(.*)");


    private final Map<String, String> replacementStringsForAlterTableDdl = newHashMap();
    private final Map<String, String> replacementStringsForCreateIndexDdl = newHashMap();
    private final MetadataExtractorIntegrator metadataExtractorIntegrator;
    private final ListenerIntegrator listenerIntegrator;

    private boolean useHibernatesRawSql = false;

    private boolean isInitialized = false;

    private Map<String, Pattern> patternCache;

    protected abstract Set<String> getTablesToExclude();

    protected abstract Set<String> getIndexesToExclude();

    protected abstract Set<String> getConstraintsToExclude();

    public NarrativeAnnotationConfiguration(MetadataExtractorIntegrator metadataExtractorIntegrator, ListenerIntegrator listenerIntegrator) {
        super(new BootstrapServiceRegistryBuilder()
                .applyIntegrator(metadataExtractorIntegrator)
                .applyIntegrator(listenerIntegrator)
                .build()
        );
        this.metadataExtractorIntegrator = metadataExtractorIntegrator;
        this.listenerIntegrator = listenerIntegrator;
        // bl: need to specify a key length for the DefaultText.text and TranslatedText.originalText field indexes.
        replacementStringsForCreateIndexDdl.put("(text)", "(text(50))");
        replacementStringsForCreateIndexDdl.put("(originalText)", "(originalText(50))");
        // bl: need the PostalCode.location index to be a spatial index
        replacementStringsForCreateIndexDdl.put("create index postalCode_location_idx", "create spatial index postalCode_location_idx");
        // bl: need to limit the length of the Clip.title index to 191 characters to fit within the 767 byte limit
        replacementStringsForCreateIndexDdl.put("clip_title_idx on Clip (title)", "clip_title_idx on Clip (title(191))");
    }

    public void setUseHibernatesRawSql(boolean useHibernatesRawSql) {
        this.useHibernatesRawSql = useHibernatesRawSql;
    }

    private Function<Consumer<File>, List<String>> exportProcessor = (c) -> {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("export", ".sql");
            c.accept(tempFile);
            //Extract the file content into a string array and return
            List<String> res = FileUtils.readLines(tempFile);
            return fixQueries(res);
        } catch (IOException e) {
            throw new RuntimeException("Error creating temp file for export operation", e);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    };

    private void logExportExceptions(List exceptionList, String exportType){
        LOG.error("Errors processing export type {}", exportType);
        for(Object e: exceptionList){
            if (e instanceof Exception) {
                LOG.error("Err");
            }
        }
    }

    private List<String> exportSchema(SchemaExport.Action action) {
        return exportProcessor.apply(file -> {
            Metadata metadata = metadataExtractorIntegrator.getMetadata();
            SchemaExport export = new SchemaExport()
                    .setDelimiter(";")
                    //Ensure each DDL statement is one line
                    .setFormat(false)
                    .setOutputFile(file.getAbsolutePath());
            export.execute( EnumSet.of(TargetType.SCRIPT), action, metadata);
            if (CollectionUtils.isNotEmpty(export.getExceptions())){
                logExportExceptions(export.getExceptions(), action.name());
            }
        });
    }

    public String[] generateDropSchemaScript() throws HibernateException {
        return exportSchema(SchemaExport.Action.DROP).toArray(new String[0]);
    }

    public String[] generateSchemaCreationScript() throws HibernateException {
        return exportSchema(SchemaExport.Action.CREATE).toArray(new String[0]);
    }

    public List<String> generateSchemaUpdateScript() throws HibernateException {
        return exportProcessor.apply(file -> {
            Metadata metadata = metadataExtractorIntegrator.getMetadata();
            SchemaUpdate export = new SchemaUpdate()
                    .setDelimiter(";")
                    //Ensure each DDL statement is one line
                    .setFormat(false)
                    .setOutputFile(file.getAbsolutePath());
            export.execute( EnumSet.of(TargetType.SCRIPT), metadata);
            if (CollectionUtils.isNotEmpty(export.getExceptions())){
                logExportExceptions(export.getExceptions(), "UPDATE");
            }
        });
    }

    protected boolean isUseEncryption() {
        // bl: use encryption by default if it's enabled! can be overridden by subclasses as necessary.
        return Encryption.INSTANCE.isUseMySqlEncryption();
    }

    public List<String> fixQueries(List<String> scripts) {
        if (useHibernatesRawSql) {
            return scripts;
        }
        if (!isInitialized) {
            init();
        }
        LinkedList<String> list = new LinkedList<String>();
        for (String script : scripts) {
            if (!isSqlExcluded(script)) {
                //Strip trailing semicolon
                boolean scStripped = false;
                if (script.length() > 0 && script.lastIndexOf(";") == script.length() - 1){
                    script = script.substring(0, script.length() - 1);
                    scStripped = true;
                }
                if (script.contains("alter table")) {
                    script = removeIndex(script);
                    script = removeConstraint(script);
                    script = replaceStatements(script, replacementStringsForAlterTableDdl);
                } else if (script.contains("create index")) {
                    script = replaceStatements(script, replacementStringsForCreateIndexDdl);
                } else if (script.contains("create table")) {
                    // bl: need to make sure that all prettyUrlString columns use utf8 charset and collation.
                    // note that below the Content and Clip tables will get utf8mb4 charset/collation, and prettyUrlString
                    // can't support 4-byte characters or else it makes the unique index too large (based on key byte limit).
                    // also, we only allow alphanumeric plus hyphens in the prettyUrlString, so it's pointless to include
                    // 4-byte utf8 chars in that column, anyway.
                    script = script.replaceAll(Pattern.quote("prettyUrlString varchar(255)"), Matcher.quoteReplacement("prettyUrlString varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci"));
                    // bl: need utf8_bin for DefaultText, TranslatedText, TextKey, and Wordlet so that any changes to wordlets
                    // will be detected.
                    List<String> tablesForBinaryCollation = Arrays.asList("DefaultText", "TranslatedText", "TextKey", "Wordlet");
                    boolean useBinaryCollation = isForTableCreate(script, tablesForBinaryCollation);

                    List<String> tablesForUtf8mb4 = Arrays.asList("Composition", "Reply", "Content", "Clip", "FileOnDisk", "CustomProfileFieldValue", "DatabaseRecordFieldValue", "ChatMessage", "CustomGraemlin", "Niche", "ElectionNominee", "TribunalIssueReport");
                    boolean useUtf8mb4 = isForTableCreate(script, tablesForUtf8mb4);
                    String characterSet = useUtf8mb4 ? "utf8mb4" : "utf8";
                    String collation = useUtf8mb4 ? "utf8mb4_general_ci" : (useBinaryCollation ? "utf8_bin" : "utf8_general_ci");
                    script = script + " CHARACTER SET '" + characterSet + "' COLLATE '" + collation + "'";

                    boolean useMyISAM = isForTableCreate(script, MYISAM_TABLES);
                    // bl: MyISAM tables don't support encryption, so check for that first
                    if (useMyISAM) {
                        script = replaceStatements(script, Collections.singletonMap("ENGINE=InnoDB", "ENGINE=MyISAM"));
                    } else if (isUseEncryption()) {
                        // bl: enable table encryption if encryption is enabled on this environment!
                        script = script + " ENCRYPTION='Y'";
                    }
                }
                //Fix up autoinc field definitions to have a unique constraint - this is broken in Hibernate 5 SchemaExport
                script = fixAutoIncFiedDef(script);
                //Add back trailing semicolon if stripped
                list.add(script + (scStripped ? ";" : ""));
            }
        }
        // clear out the pattern cache since it isn't needed anymore
        patternCache = null;
        return list;
    }

    /**
     * Fix up autoinc field definitions - Hibernate 5 does not include the unique constraint in the table definition
     * which MySql doesn't like very much
     *
     */
    public static String fixAutoIncFiedDef(String line){
        String res = line;
        Matcher matcher = AUTOINC_REPL_PATTERN.matcher(line);
        if (matcher.matches()) {
            res = matcher.group(1) + matcher.group(2) + matcher.group(3) + matcher.group(4) + matcher.group(5) + matcher.group(6) + " unique check (" + matcher.group(2) + ">=0)" + matcher.group(7);
        }
        return res;
    }

    private static boolean isForTableCreate(String script, Collection<String> tableNames) {
        for (String tableName : tableNames) {
            // bl: in order to ensure exact matches on table names (and not table name suffixes/prefixes),
            // look for a space before/after the table name.
            if (script.contains(" " + tableName + " ")) {
                return true;
            }
        }
        return false;
    }

    public <T extends Annotation> T findAnnotationByTypeForEntity(Class<? extends DAOObject> daoObjectClass, String propertyName, Class<T> annotationClass, String associatedEntityName){
        T res = null;
        try {
            // jw: some foreign keys are defined on a super class of the actual object that is persisted, and our
            //     code needs to fall back and find the non-synthetic getter for the property.
            Class daoObjectClassForField = daoObjectClass;
            do {
                // jw: first things first, let's see if the annotation is on the field itself.
                // note: this is necessary since we are starting to define annotations on the private field, and then using
                //       lombok to create the getters and setters for us.
                try {
                    Field field = daoObjectClassForField.getDeclaredField(propertyName);
                    if (field != null) {
                        res = field.getAnnotation(annotationClass);

                        // jw: if we found it, break out.
                        if (res != null) {
                            break;
                        }
                    }
                    // jw: must be defined on a superclass, let's ignore for now and look for the getter instead. We should hit
                    //     this if we fall back to the parent that defines the field.
                } catch (NoSuchFieldException ignored) {
                    // ignored
                }

                // jw: since we did not find the annotation on the field, let's check the getter.
                PropertyDescriptor propertyDescriptor;
                try {
                    propertyDescriptor = PropertyUtils.getPropertyDescriptor(daoObjectClassForField.newInstance(), propertyName);
                } catch (InstantiationException ie) {
                    // bl: if the daoObjectClass represents an abstract class, then the newInstance above will fail.
                    // in that case, try to create the PropertyDescriptor directly.
                    // note: can't use this approach in all cases since it doesn't work properly for classes
                    // such as ForumContent that implement interfaces that have parameterized types
                    // (ForumContentBaseFields.getContent() in the case of ForumContent).
                    propertyDescriptor = new PropertyDescriptor(propertyName, daoObjectClassForField);
                }

                Method method = daoObjectClassForField.getMethod(PropertyUtils.getReadMethod(propertyDescriptor).getName());
                if (!method.isSynthetic()) {
                    res = method.getAnnotation(annotationClass);
                    break;
                } else {
                    daoObjectClassForField = daoObjectClassForField.getSuperclass();
                }
            } while (daoObjectClassForField != Object.class);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed lookup of @ForeignKey annotation for *-to-one association!  This shouldn't ever happen! table/" + daoObjectClass.getName() + " mapped to " + associatedEntityName, t);
        }

        return res;
    }

    private ForeignKey findForeignKey(PersistentClass testClass, String associatedEntityName, String propertyName, EntityType type){
        ForeignKey foreignKey = null;
        {
            Set<Table> tablesTested = newHashSet();
            while (testClass != null) {
                Table tableToTest = testClass.getTable();
                if (!tablesTested.contains(tableToTest)) {
                    tablesTested.add(tableToTest);
                    Iterator<ForeignKey> fkIter = tableToTest.getForeignKeyIterator();
                    while (fkIter.hasNext()) {
                        ForeignKey fk = fkIter.next();
                        if (isEqual(fk.getReferencedEntityName(), associatedEntityName)) {
                            assert fk.getColumnSpan() == 1 : "Currently only support single-column foreign keys!";
                            // bl: the naming strategy we use in ImplictNamingStrategyComponentPathImpl always includes the property name followed by an underscore
                            if (type.isOneToOne()) {
                                // bl: adding this check in order to get validation/detection working properly
                                // for PremiumMembershipRefund, which has a OneToOne primary key join association
                                // as well as a separate ManyToOne foreign key association to the same table (PremiumMembershipTransaction)
                                if (fk.getName().toUpperCase().matches("FK[0-9aA-Z]*")) {
                                    if (foreignKey != null) {
                                        throw UnexpectedError.getRuntimeException("Not expecting to get multiple foreign keys of a given type and name for a OneToOne association! table/" + tableToTest.getName() + "." + propertyName + " (" + associatedEntityName + ")");
                                    }
                                    foreignKey = fk;
                                }
                            } else {
                                if (fk.getColumn(0).getName().startsWith(propertyName + "_")) {
                                    if (foreignKey != null) {
                                        throw UnexpectedError.getRuntimeException("Not expecting to get multiple foreign keys of a given type and name! table/" + tableToTest.getName() + "." + propertyName + " (" + associatedEntityName + ")");
                                    }
                                    foreignKey = fk;
                                }
                            }
                        }
                    }
                }
                testClass = testClass.getSuperclass();
            }
        }

        return foreignKey;
    }

    /**
     * bl: there are bugs with Hibernate that necessitate our own post-processing of the Hibernate class metadata
     * and mappings.  in particular, Hibernate does not properly detect @ForeignKey annotations on @OneToOne
     * entity mappings that use @PrimaryKeyJoinColumn.  the actual foreign key name isn't specified until after
     * the foreign key relationship is set up for some reason.
     * <p>
     * additionally, Hibernate was creating a foreign key for the track property of AmgTrackArtist and AmgTrackArtistInc
     * even though the @ForeignKey annotation indicates that it should not.
     * <p>
     * the following init method handles the above two issues by doing post-processing on the DDL created by Hibernate.
     */
    private void init() {
        // bl: we only need to do the post-processing if we are installing.
        /*Iterator<PersistentClass> iter = getClassMappings();
        while(iter.hasNext()) {
            PersistentClass persistentClass = iter.next();
            Class<? extends DAOObject> daoObjectClass = persistentClass.getMappedClass();
            RootClass rootClass = persistentClass.getRootClass();
            Table rootTable = rootClass.getRootTable();
            Iterator<ForeignKey> fkIter = rootTable.getForeignKeyIterator();
            while(fkIter.hasNext()) {
                ForeignKey foreignKey = fkIter.next();
                String associatedEntityName = foreignKey.getReferencedEntityName();
                assert foreignKey.getColumnSpan()==1 : "Currently only support single-column foreign keys!";
                String foreignKeyColumnName = foreignKey.getColumn(0).getName();
                PrimaryKey primaryKey = foreignKey.getReferencedTable().getPrimaryKey();
                assert primaryKey.getColumnSpan()==1 : "Currently only support single-column primary keys for foreign key associations!";
                String primaryKeyColumnName = primaryKey.getColumn(0).getName();
                String propertyName = IPStringUtil.getStringAfterStripFromEnd(foreignKeyColumnName, "_" + primaryKeyColumnName);
                final org.hibernate.annotations.ForeignKey foreignKeyAnnotation;
                try {
                    Method method = PropertyUtils.getReadMethod(PropertyUtils.getPropertyDescriptor(daoObjectClass.newInstance(), propertyName));
                    foreignKeyAnnotation = method.getAnnotation(org.hibernate.annotations.ForeignKey.class);
                } catch(Throwable t) {
                    throw UnexpectedError.getRuntimeException("Failed lookup of getter field for a one-to-one association!  This shouldn't ever happen! table/" + daoObjectClass.getName() + " mapped to " + associatedEntityName, t);
                }
                if(foreignKeyAnnotation==null) {
                    throw UnexpectedError.getRuntimeException("Must supply @ForeignKey annotation on all *ToOne associations now! Missing from " + daoObjectClass.getName() + "." + propertyName + " (" + associatedEntityName + ")");
                }
                final String fkNameToUse = foreignKeyAnnotation.name();
                Table table = getClassMapping(daoObjectClass.getName()).getRootClass().getTable();
                if(isEmpty(fkNameToUse)) {
                    throw UnexpectedError.getRuntimeException("Must supply explicit foreign key names now via @ForeignKey annotation for OneToOne associations!  Missing for " + daoObjectClass.getName() + " to " + propertyName + " (" + propertyName + ")");
                }
                String fkName = foreignKey.getName();
                // bl: only care about changing mappings for classes that Hibernate did not properly detect the @ForeignKey
                // annotation for.  Hibernate fails to properly read the @ForeignKey annotation for @OneToOne associations
                // with @PrimaryKeyJoinColumn.
                if(!isEqual(fkName, fkNameToUse)) {
                    String hibernateGeneratedFkName = getHibernateForeignKeyName(table, foreignKey.getColumnIterator(), associatedEntityName);
                    assert fkName.equals(hibernateGeneratedFkName) : "Found a foreign key name mismatch between Hibernate's generated name and our calculation of Hibernate's generated name! fk1/" + fkName + " fk2/" + hibernateGeneratedFkName + " for " + daoObjectClass.getName() + "." + propertyName + " (" + propertyName + ")";
                    if(HibernateUtil.NO_FOREIGN_KEY_NAME.equals(fkNameToUse)) {
                        excludeStatementWithFragment.add(hibernateGeneratedFkName);
                    } else {
                        replacementStringsForAlterTableDdl.put(hibernateGeneratedFkName, fkNameToUse);
                    }
                }
            }
        }*/
        SessionFactory sf = getSessionFactory();
        if (sf == null) {
            throw UnexpectedError.getRuntimeException("Must supply SessionFactory via getSessionFactory() in order for query massaging to work correctly!");
        }
        Metadata metadata = metadataExtractorIntegrator.getMetadata();

        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
            Class<? extends DAOObject> daoObjectClass = persistentClass.getMappedClass();
            Table table = persistentClass.getTable();
            if (getTablesToExclude().contains(table.getName())) {
                continue;
            }
            // bl: force the @ForeignKey annotation for our joined inheritance hierarchies, too.
            if (persistentClass.isJoinedSubclass()) {
                final org.hibernate.annotations.ForeignKey foreignKeyAnnotation;
                try {
                    foreignKeyAnnotation = daoObjectClass.getAnnotation(org.hibernate.annotations.ForeignKey.class);
                } catch (Throwable t) {
                    throw UnexpectedError.getRuntimeException("Failed lookup of @ForeignKey annotation for joined inheritance!  This shouldn't ever happen! table/" + daoObjectClass.getName(), t);
                }
                if (foreignKeyAnnotation == null) {
                    throw UnexpectedError.getRuntimeException("Must supply @ForeignKey annotation on all joined inheritance hierarchy subclasses! Missing from " + daoObjectClass.getName());
                }
            }
            // bl: EntityType is either OneToOneType or ManyToOneType
            Map<EntityType, String> types = newLinkedHashMap();
            Iterator propIter = persistentClass.getPropertyIterator();
            while (propIter.hasNext()){
                Property prop = (Property) propIter.next();
                Type type = prop.getType();
                if (type instanceof EntityType) {
                    types.put((EntityType) type, prop.getName());
                }
            }
            for (Map.Entry<EntityType, String> entry : types.entrySet()) {
                EntityType type = entry.getKey();
                String associatedEntityName = type.getAssociatedEntityName();
                String propertyName = entry.getValue();
                // bl: skip all one-to-one relationships that use mappedBy
                if (type.isOneToOne() && type.getForeignKeyDirection() == ForeignKeyDirection.TO_PARENT) {
                    ForeignKey foreignKey = findForeignKey(persistentClass, associatedEntityName, propertyName, type);
                    PrimaryKeyJoinColumn primaryKeyJoinColumnAnno = findAnnotationByTypeForEntity(daoObjectClass, propertyName, PrimaryKeyJoinColumn.class, associatedEntityName);
                    if (primaryKeyJoinColumnAnno != null && StringUtils.isNotEmpty(primaryKeyJoinColumnAnno.foreignKey().name())){
                        replacementStringsForAlterTableDdl.put(foreignKey.getName(), primaryKeyJoinColumnAnno.foreignKey().name());
                    }  else {
                        org.hibernate.annotations.ForeignKey foreignKeyAnno = findAnnotationByTypeForEntity(daoObjectClass, propertyName, org.hibernate.annotations.ForeignKey.class, associatedEntityName);
                        if (foreignKey != null && foreignKeyAnno != null && !HibernateUtil.NO_FOREIGN_KEY_NAME.equals(foreignKeyAnno.name())) {
                            replacementStringsForAlterTableDdl.put(foreignKey.getName(), foreignKeyAnno.name());
                        }
                    }
                    continue;
                }

                org.hibernate.annotations.ForeignKey foreignKeyAnnotation = findAnnotationByTypeForEntity(daoObjectClass, propertyName, org.hibernate.annotations.ForeignKey.class, associatedEntityName);
                if (foreignKeyAnnotation == null) {
                    throw UnexpectedError.getRuntimeException("Must supply @ForeignKey annotation on all *ToOne associations now! Missing from " + daoObjectClass.getName() + "." + propertyName + " (" + associatedEntityName + ")");
                }
                final String fkNameToUse = foreignKeyAnnotation.name();
                if (isEmpty(fkNameToUse)) {
                    throw UnexpectedError.getRuntimeException("Must supply explicit foreign key names now via @ForeignKey annotation for OneToOne associations!  Missing for " + daoObjectClass.getName() + " to " + propertyName + " (" + propertyName + ")");
                }
                // bl: workaround for our "special" non-nullable, bi-directional one-to-one associations
                // that we have with views for GlobalContent->GlobalContentRights.
                if (HibernateUtil.VIEW_FOREIGN_KEY_SPECIAL_NAME.equals(fkNameToUse) && type.isOneToOne() && getTablesToExclude().contains(IPStringUtil.getStringAfterLastIndexOf(associatedEntityName, "."))) {
                    continue;
                }

                ForeignKey foreignKey = findForeignKey(persistentClass, associatedEntityName, propertyName, type);

                if (foreignKey == null) {
                    // bl: skip all ManyToOne associations that are set to "none" as the foreign key since Hibernate handles them properly.
                    if (!type.isOneToOne() && HibernateUtil.NO_FOREIGN_KEY_NAME.equals(fkNameToUse)) {
                        continue;
                    }
                    throw UnexpectedError.getRuntimeException("Failed identifying ForeignKey for " + table.getName() + "." + propertyName + " (" + associatedEntityName + ")");
                }
                String hibernateFkName = foreignKey.getName();
                // bl: only care about changing mappings for classes that Hibernate did not properly detect the @ForeignKey
                // annotation for.  Hibernate fails to properly read the @ForeignKey annotation for @OneToOne associations
                // with @PrimaryKeyJoinColumn.
                if (!isEqual(hibernateFkName, fkNameToUse)) {
                    if (HibernateUtil.NO_FOREIGN_KEY_NAME.equals(fkNameToUse)) {
                        excludeStatementWithFragment.add(hibernateFkName);
                    } else {
                        assert hibernateFkName.equals(getHibernateForeignKeyName(foreignKey)) : "Found a foreign key name mismatch between Hibernate's generated name and our calculation of Hibernate's generated name! fk1/" + hibernateFkName + " fk2/" + getHibernateForeignKeyName(foreignKey) + " for " + daoObjectClass.getName() + "." + propertyName + " (" + propertyName + ")";
                        replacementStringsForAlterTableDdl.put(hibernateFkName, fkNameToUse);
                    }
                }
            }
        }

        isInitialized = true;
    }

    private static final Pattern DUPLICATE_PRIMARY_KEY_INDEX = Pattern.compile("add index \\w+ \\(" + DAOObject.FIELD__OID__NAME + "\\),?", Pattern.CASE_INSENSITIVE);

    protected SessionFactory getSessionFactory() {
        return null;
    }

    private String removeIndex(String script) {
        // strip out any duplicate primary key indexes
        script = DUPLICATE_PRIMARY_KEY_INDEX.matcher(script).replaceAll("");
        for (String index : getIndexesToExclude()) {
            int start = script.indexOf("add index " + index);
            if (start > -1) {
                int end = script.indexOf(',', start);
                if (end == -1) {
                    script = script.substring(0, start);
                } else {
                    script = script.substring(0, start) + script.substring(end + 1);
                }
            }
        }
        return script;
    }

    private String removeConstraint(String script) {
        for (String constraint : getConstraintsToExclude()) {
            Pattern pattern = Pattern.compile(",(\\s*)add constraint " + constraint + "([^;,]*)", Pattern.CASE_INSENSITIVE);
            script = pattern.matcher(script).replaceAll("");
            pattern = Pattern.compile(",(\\s*)drop(\\s*)foreign key " + constraint + "([^;,]*)", Pattern.CASE_INSENSITIVE);
            script = pattern.matcher(script).replaceAll("");
        }
        return script;
    }

    private String replaceStatements(String script, Map<String, String> replacements) {
        // replace any foreign key translations as necessary
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String oldKey = entry.getKey();
            String newKey = entry.getValue();
            if (patternCache == null) {
                patternCache = newHashMap();
            }
            Pattern pattern = patternCache.get(oldKey);
            if (pattern == null) {
                patternCache.put(oldKey, pattern = Pattern.compile(oldKey, Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            }
            script = pattern.matcher(script).replaceAll(newKey);
        }
        return script;
    }

    private boolean isSqlExcluded(String statement) {
        for (String fragment : excludeStatementWithFragment) {
            if (statement.contains(fragment)) {
                return true;
            }
        }
        for (String tableToExclude : getTablesToExclude()) {
            // jw: Lets include a space after the name.  The name should be encapsulated with spaces in all places. This
            //     will ensure that we dont get any false positives. For example, when exporting the DDL for the Export
            //     we exclude StatisticRollup, but not StatisticRollupArea.  So we don't want this to match artificially
            //     on StatisticRollupArea just because it starts with StatisticRollup.
            if (statement.contains("table " + tableToExclude + " ")) {
                return true;
            }
            if (statement.contains("on " + tableToExclude + " ")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Generate a Hibernate 5.x style FK name
     *
     */
    public static String getHibernateForeignKeyName(ForeignKey fk) {
        List<Identifier> colIdentList = (List<Identifier>) fk.getReferencedColumns().stream()
                .map(col -> Identifier.toIdentifier(((Column) col).getName()))
                .collect(Collectors.toList());
        return NamingHelper.INSTANCE.generateHashedFkName(
                "FK",
                fk.getTable().getNameIdentifier(),
                fk.getReferencedTable().getNameIdentifier(),
                colIdentList);
    }

    public MetadataExtractorIntegrator getMetadataExtractorIntegrator() {
        return metadataExtractorIntegrator;
    }

    public ListenerIntegrator getListenerIntegrator() {
        return listenerIntegrator;
    }
}
