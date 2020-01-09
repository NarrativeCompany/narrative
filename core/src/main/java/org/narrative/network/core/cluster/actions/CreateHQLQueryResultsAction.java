package org.narrative.network.core.cluster.actions;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang3.time.FastDateFormat;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metamodel.internal.EntityTypeImpl;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CustomType;
import org.hibernate.type.MapType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.hibernate.GSessionFactory;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.Debug;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.XMLUtil;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.common.web.HttpMethodType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.tasktypes.AllPartitionsTask;
import org.narrative.network.shared.tasktypes.TaskIsolationLevel;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.narrative.network.shared.util.NetworkLogger;

import javax.persistence.metamodel.EntityType;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 6, 2006
 * Time: 9:47:13 PM
 */
public class CreateHQLQueryResultsAction extends ClusterAction implements Preparable {

    private static final NetworkLogger logger = new NetworkLogger(CreateHQLQueryResultsAction.class);

    public static final String ACTION_NAME = "hqlQuery";
    private static final String FULL_ACTION_PATH = "/" + ACTION_NAME;
    private static final String URL_BASE = FULL_ACTION_PATH + "!queryGet";
    private static final int MAX_ITEMS_IN_COLLECTION_TO_SHOW = 20;

    private static final String PASSWORD_FIELD = "password";

    private String hql;
    private Class objectClass;
    private OID objectOid;
    private int page = 1;
    private int rowsPerPage = 50;
    private String type;
    private OID oid;
    private String idProps;
    private StringBuilder output = new StringBuilder();
    private boolean explain;
    private Map<Partition, List<ExplainResults>> explainResults = new LinkedHashMap<Partition, List<ExplainResults>>();
    private List<String> colNames = new LinkedList<String>();
    private String partition;
    private Partition currentPartition;
    private Map<String, String> partitions = new LinkedHashMap<String, String>();
    private SortedMap<String, String> objectTypes = new TreeMap<String, String>(new Comparator<String>() {
        public int compare(String o1, String o2) {
            String obj1 = IPStringUtil.getStringAfterLastIndexOf(o1, ".");
            String obj2 = IPStringUtil.getStringAfterLastIndexOf(o2, ".");
            return obj1.compareTo(obj2);
        }
    });
    private static final Object DUMMY_OBJECT = new Object();

    public void prepare() throws Exception {
        for (PartitionType type : PartitionType.ACTIVE_PARTITION_TYPES) {
            if (!type.isSingleton()) {
                partitions.put(type.name(), "All " + type + " Partitions");
            }
            Collection<Partition> allPartitionsOfType = Partition.dao().getAllForType(type);
            for (Partition partition : allPartitionsOfType) {
                partitions.put(partition.getOid().toString(), partition.getDisplayName());
            }
            for (EntityType<?> entityType : type.getGSessionFactory().getSessionFactory().getMetamodel().getEntities()) {
                String sObjectClassName = ((EntityTypeImpl)entityType).getTypeName();
                String shortName = entityType.getName();
                objectTypes.put(sObjectClassName, shortName);
            }
        }

        if (IPStringUtil.isEmpty(partition)) {
            partition = PartitionType.GLOBAL.getSingletonPartition().getOid().toString();
        }
    }

    @Override
    public String input() throws Exception {
        return super.input();
    }

    @MethodDetails(httpMethodType = HttpMethodType.GET)
    public String queryGet() {
        return query();
    }

    public String query() {

        final PartitionType partitionType;
        {
            PartitionType pType = null;
            try {
                pType = PartitionType.valueOf(partition);
            } catch (Throwable t) {
                // ignore
            }
            partitionType = pType;
        }

        if (isEmpty(hql) && (oid != null || !isEmpty(idProps)) && !(isEmpty(type))) {
            StringBuilder sb = new StringBuilder();
            sb.append("from ").append(type).append(" obj where ");
            if (oid != null) {
                sb.append("obj.id = ").append(oid.getValue());
            } else {
                Map<String, Collection<String>> propNameToValue = IPHTMLUtil.parseQueryString(idProps);
                int i = 0;
                for (Map.Entry<String, Collection<String>> entry : propNameToValue.entrySet()) {
                    if (i > 0) {
                        sb.append(" and ");
                    }
                    String propName = entry.getKey();
                    String propValue = entry.getValue().iterator().next();
                    sb.append("obj.id.").append(propName).append(" = ").append(propValue);
                    i++;
                }
            }
            hql = sb.toString();
        }

        AllPartitionsTask<Object> partitionTask = new AllPartitionsTask<Object>(false) {
            protected Object doMonitoredTask() {
                currentPartition = getCurrentPartition();

                //was this an actual query submittal
                if (!isEmpty(hql)) {
                    try {
                        //run the query
                        long timer = System.currentTimeMillis();
                        if (explain) {
                            SessionImplementor si = (SessionImplementor) currentPartition.getPartitionType().currentSession().getSession();
                            HQLQueryPlan plan = new HQLQueryPlan(hql, false, si.getLoadQueryInfluencers().getEnabledFilters(), si.getFactory());
                            Connection con = currentPartition.getDatabaseResources().getConnection();
                            List<ExplainResults> explainResultsList = new LinkedList<ExplainResults>();
                            explainResults.put(currentPartition, explainResultsList);
                            for (String sql : plan.getSqlStrings()) {
                                Statement stmt = con.createStatement();
                                ResultSet rs = stmt.executeQuery("explain " + sql);
                                getColNames(rs);
                                List<List<Object>> rows = new LinkedList<List<Object>>();
                                while (rs.next()) {
                                    List<Object> row = new LinkedList<Object>();
                                    rows.add(row);
                                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                        Object obj = rs.getObject(i);
                                        if (obj == null) {
                                            row.add("&nbsp;");
                                        } else {
                                            row.add(rs.getObject(i));
                                        }

                                    }
                                }
                                sql = sql.replaceAll(" \\(select ", "<br/>\\(select ");
                                sql = sql.replaceAll(" from ", "<br/>from ");
                                sql = sql.replaceAll(" inner join ", "<br/>inner join ");
                                sql = sql.replaceAll(" left join ", "<br/>left join ");
                                sql = sql.replaceAll(" where ", "<br/>where ");
                                sql = sql.replaceAll(" group ", "<br/>group ");
                                sql = sql.replaceAll(" order ", "<br/>order ");
                                explainResultsList.add(new ExplainResults(sql, rows));
                            }
                        } else {
                            // bl: we don't have any way to detect what fields are being selected, so for now, let's return an error if the text "password" is in the HQL at all.
                            if (hql.toLowerCase().contains(PASSWORD_FIELD)) {
                                throw new ApplicationError("Can't run HQL that contains '" + PASSWORD_FIELD + "' for security purposes!");
                            }
                            List results = currentPartition.getPartitionType().currentSession().createQuery(hql).setFirstResult((page - 1) * rowsPerPage).setMaxResults(rowsPerPage + 1).list();

                            //show the query execution time
                            output.append("<p/><h2>");
                            output.append(currentPartition.getDisplayName());
                            output.append("</h2><p/>Query Time: ").append(System.currentTimeMillis() - timer).append(" ms. <br/>");

                            //print the results
                            if (results.isEmpty()) {
                                output.append("<p/><b>No rows found.</b>");
                            } else {
                                //prev page
                                if (page > 1) {
                                    Map<String, String> params = newHashMap();
                                    //params.put("a", "query");
                                    params.put("page", String.valueOf(page - 1));
                                    params.put("hql", hql);
                                    params.put("partition", partition);
                                    params.put("rowsPerPage", Integer.toString(rowsPerPage));
                                    output.append("<a href='").append(IPHTMLUtil.getParametersAsURL(getHqlQueryBaseUrl(), params)).append("'>" + "&lt;Prev</a>");
                                } else {
                                    output.append("&lt;Prev");
                                }

                                //current results
                                int first = (((page - 1) * rowsPerPage) + 1);
                                int last = Math.min(first + rowsPerPage - 1, first + results.size() - 1);
                                output.append(" | Results ").append(first).append(" to ").append(last).append(" | ");

                                //next page
                                if (results.size() == (rowsPerPage + 1)) {
                                    results = results.subList(0, rowsPerPage);
                                    Map<String, String> params = newHashMap();
                                    //params.put("a", "query");
                                    params.put("page", String.valueOf(page + 1));
                                    params.put("hql", hql);
                                    params.put("partition", partition);
                                    params.put("rowsPerPage", Integer.toString(rowsPerPage));
                                    output.append("<a href='").append(IPHTMLUtil.getParametersAsURL(getHqlQueryBaseUrl(), params)).append("'>" + "Next&gt;</a>");
                                } else {
                                    output.append("Next&gt;");
                                }

                                createQueryTable(currentPartition.getPartitionType().getGSessionFactory(), output, results, rowsPerPage);
                            }

                        }

                    } catch (Exception e) {
                        if (e instanceof SQLGrammarException) {
                            output.append("<br/>").append(((SQLGrammarException) e).getSQLException().toString());
                            output.append("<p/>").append(((SQLGrammarException) e).getSQL());
                        } else {
                            logger.error("Error executing HQL.", e);
                            StatisticManager.recordException(e, false, null);
                        }
                        output.append("<p/>ERROR: ").append(Debug.stackTraceFromException(e));
                    }

                } else if (objectClass != null && objectOid != null) {
                    Object o = currentPartition.getPartitionType().currentSession().getObject(objectClass, objectOid);
                    int rows = exists(o) ? 1 : 0;
                    List results = rows == 1 ? Collections.singletonList(o) : Collections.emptyList();
                    if (rows == 0) {
                        output.append("<p/><b>No rows found.</b>");
                    } else {
                        createQueryTable(currentPartition.getPartitionType().getGSessionFactory(), output, results, rows);
                    }
                }
                return null;
            }
        };

        Map<String, String> params = newHashMap();
        params.put("page", String.valueOf(page));
        params.put("hql", hql);
        params.put("rowsPerPage", Integer.toString(rowsPerPage));
        params.put("partition", partition);
        output.append("<a href=\"").append(IPHTMLUtil.getParametersAsURL(getHqlQueryBaseUrl(), params)).append("\">Permalink</a>");

        if (partitionType != null) {
            partitionType.doTaskInAllPartitionsOfThisType(new TaskOptions(TaskIsolationLevel.ISOLATED), partitionTask);
        } else {
            Partition partitionObj = Partition.dao().get(OID.valueOf(partition));
            partitionTask.setCurrentPartition(partitionObj);
            partitionObj.getPartitionType().doTask(partitionObj, new TaskOptions(TaskIsolationLevel.ISOLATED), partitionTask);
        }

        return INPUT;
    }

    private void getColNames(ResultSet rs) throws Exception {
        if (colNames.isEmpty()) {
            for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                colNames.add(rs.getMetaData().getColumnName(i));
            }

        }
    }

    private void createQueryTable(GSessionFactory gsf, StringBuilder html, List results, int rows) throws HibernateException {
        SessionFactory sf = gsf.getSessionFactory();
        Class lastClass = null;
        ClassMetadata md = null;

        //for each row in the results
        for (Object result : results) {
            //if this is a new type then create a new header
            Object[] row = getRow(result);
            Class newRowClass = row[0] == null ? Object.class : row[0].getClass();
            if (lastClass != newRowClass) {

                if (lastClass != null) {
                    XMLUtil.closeTag(html, "TABLE");
                }

                lastClass = newRowClass;

                html.append("<TABLE BORDER='1'>");

                //create the object headers
                XMLUtil.openTag(html, "TR");
                for (int i = 0; i < row.length; i++) {
                    Object o = row[i] == null ? DUMMY_OBJECT : row[i];
                    md = sf.getClassMetadata(o.getClass());

                    if (md != null) {
                        int colSpan = md.getPropertyNames().length + 1;
                        html.append("<TD valign=\"top\" colspan=\"" + colSpan + "\">");
                        XMLUtil.openTag(html, "B");
                        getCleanClassName(o, html);
                        XMLUtil.closeTag(html, "B");
                        XMLUtil.closeTag(html, "TD");
                    } else {
                        XMLUtil.openTag(html, "TD");
                        XMLUtil.closeTag(html, "TD");
                    }
                }

                //create the column headers
                XMLUtil.openTag(html, "TR");
                for (int i = 0; i < row.length; i++) {
                    Object o = row[i] == null ? DUMMY_OBJECT : row[i];
                    md = sf.getClassMetadata(o.getClass());

                    if (md != null) {
                        List<String> idNames = new LinkedList<String>();
                        {
                            String idName = md.getIdentifierPropertyName();
                            Type idType = md.getIdentifierType();
                            if (idType.isComponentType() && idType instanceof ComponentType) {
                                for (String propertyName : ((ComponentType) idType).getPropertyNames()) {
                                    idNames.add(idName + "." + propertyName);
                                }
                            } else {
                                idNames.add(idName);
                            }
                        }

                        for (String idName : idNames) {
                            html.append("<TD valign=\"top\">");
                            XMLUtil.openTag(html, "B");
                            html.append(idName);
                            XMLUtil.closeTag(html, "B");
                            XMLUtil.closeTag(html, "TD");
                        }

                        //first the properties
                        List<String> propertyNames = new LinkedList<String>();
                        Type[] types = md.getPropertyTypes();
                        String[] names = md.getPropertyNames();
                        for (int j = 0; j < names.length; j++) {
                            String name = names[j];
                            Type type = types[j];
                            if (type.isComponentType() && type instanceof ComponentType) {
                                ComponentType cType = (ComponentType) type;
                                for (String propertyName : cType.getPropertyNames()) {
                                    propertyNames.add(name + "." + propertyName);
                                }
                            } else {
                                propertyNames.add(name);
                            }
                        }

                        for (String propertyName : propertyNames) {
                            html.append("<TD valign=\"top\">");
                            XMLUtil.openTag(html, "B");
                            html.append(propertyName);
                            XMLUtil.closeTag(html, "B");
                            XMLUtil.closeTag(html, "TD");
                        }
                    } else {
                        html.append("<TD valign=\"top\">");
                        XMLUtil.openTag(html, "B");
                        getCleanClassName(o, html);
                        XMLUtil.closeTag(html, "B");
                        XMLUtil.closeTag(html, "TD");
                    }
                }

                XMLUtil.closeTag(html, "TR");
            }

            //now do the row values
            XMLUtil.openTag(html, "TR");
            row = getRow(result);
            for (int i = 0; i < row.length; i++) {
                Object o = row[i];
                if (o != null) {
                    md = sf.getClassMetadata(o.getClass());
                } else {
                    md = null;
                }

                //this is a complex object type
                if (md != null) {

                    //first display its ID
                    List<Object> idValues = new LinkedList<Object>();
                    {
                        Serializable id = md.getIdentifier(o);
                        Type idType = md.getIdentifierType();
                        if (idType.isComponentType() && idType instanceof ComponentType) {
                            idValues.addAll(Arrays.asList(((ComponentType) idType).getPropertyValues(id, EntityMode.POJO)));
                        } else {
                            idValues.add(id);
                        }
                    }

                    for (Object idValue : idValues) {
                        html.append("<TD valign=\"top\">");
                        html.append(idValue.toString());
                        XMLUtil.closeTag(html, "TD");
                    }

                    //now display each of its values
                    List<ObjectPair<Type, Object>> propertyValues = new LinkedList<ObjectPair<Type, Object>>();
                    Type[] types = md.getPropertyTypes();
                    Object[] vals = md.getPropertyValues(o);
                    String[] names = md.getPropertyNames();
                    for (int j = 0; j < vals.length; j++) {
                        Object val = vals[j];
                        Type type = types[j];
                        String name = names[j];
                        if (type.isComponentType() && type instanceof ComponentType) {
                            ComponentType cType = (ComponentType) type;
                            String[] propNames = cType.getPropertyNames();
                            for (int k = 0; k < propNames.length; k++) {
                                String propName = propNames[k];
                                Object propVal = val == null ? null : cType.getPropertyValue(val, k, EntityMode.POJO);
                                addPropertyValue(propertyValues, name, type, propVal);
                            }
                        } else {
                            addPropertyValue(propertyValues, name, type, val);
                        }
                    }

                    for (ObjectPair<Type, Object> pair : propertyValues) {
                        Type type = pair.getOne();
                        Object propertyValue = pair.getTwo();
                        html.append("<TD valign=\"top\">");
                        outputRowValue(gsf, md, idValues.size() == 1 ? idValues.iterator().next().toString() : null, propertyValue, type, html);
                        XMLUtil.closeTag(html, "TD");
                    }

                    //simple object type
                } else {
                    html.append("<TD valign=\"top\">");
                    outputRowValue(gsf, null, null, o, null, html);
                    XMLUtil.closeTag(html, "TD");
                }
            }
            XMLUtil.closeTag(html, "TR");

        }
        XMLUtil.closeTag(html, "TABLE");
    }

    private void addPropertyValue(List<ObjectPair<Type, Object>> propertyValues, String name, Type type, Object val) {
        // bl: don't reveal _any_ password field in HQL
        if (name.toLowerCase().contains(PASSWORD_FIELD)) {
            // bl: don't ever show passwords in HQL!
            propertyValues.add(new ObjectPair<>(StandardBasicTypes.STRING, "****"));
        } else {
            propertyValues.add(new ObjectPair<>(type, val));
        }
    }

    private void getCleanClassName(Object o, StringBuilder html) {
        if (o == null) {
            html.append("{null}");
        } else {
            int start = o.getClass().getName().lastIndexOf(".") + 1;
            int end = o.getClass().getName().indexOf("$$");
            if (end == -1) {
                end = o.getClass().getName().length();
            }
            html.append(o.getClass().getName().substring(start, end));
        }
    }

    /**
     * Output for a single row value
     */
    private void outputRowValue(GSessionFactory gsf, ClassMetadata cm, String parentObjectId, Object val, Type type, StringBuilder html) throws HibernateException {

        SessionFactory sf = gsf.getSessionFactory();

        //is it null?
        if (val == null) {
            html.append("{null}");
            return;
        }

        //is it a collection type?
        if (val instanceof Collection) {
            Collection col = (Collection) val;
            if (!col.isEmpty()) {
                Class cls = col.iterator().next().getClass();
                if (Enum.class.isAssignableFrom(cls)) {
                    for (Object o : col) {
                        html.append(((Enum) o).name());
                        html.append("<br/>");
                    }

                    // jw: should we add a test in here that DAOObject.class.isAssignableFrom(cls)?  Instead of just assuming it is?
                } else {
                    if (cm != null) {
                        doCollectionShowAllLink(gsf, cls, cm, parentObjectId, col.size(), (CollectionType)type, html);
                    }
                    // bl: limit collections to showing at most 20 entries
                    int i = 0;
                    for (Object o : col) {
                        doSingleRowValue(sf, o, null, html);
                        html.append("<br/>");
                        i++;
                        if (cm != null && i >= MAX_ITEMS_IN_COLLECTION_TO_SHOW) {
                            break;
                        }
                    }
                }
            } else {
                html.append("{empty}");
            }
            return;
        }

        //is it a map type?
        if (val instanceof Map && !(val instanceof Properties)) {
            Map map = (Map) val;
            if (!map.isEmpty()) {
                Class cls = map.values().iterator().next().getClass();
                // jw: if the value of the map is a DAOObject lets include a link to view all values.
                if (DAOObject.class.isAssignableFrom(cls)) {
                    doCollectionShowAllLink(gsf, cls, cm, parentObjectId, map.size(), (MapType)type, html);
                }

                // bl: limit maps to showing at most 20 entries
                int i = 0;
                Set<Map.Entry> entries = map.entrySet();
                for (Map.Entry entry : entries) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    doSingleRowValue(sf, key, null, html);
                    html.append("=");
                    doSingleRowValue(sf, value, null, html);
                    html.append("<br/>");
                    i++;
                    if (i >= MAX_ITEMS_IN_COLLECTION_TO_SHOW) {
                        break;
                    }
                }
            } else {
                html.append("{empty}");
            }
            return;
        }

        //its a single value
        doSingleRowValue(sf, val, type, html);
    }

    private void doCollectionShowAllLink(GSessionFactory gsf, Class cls, ClassMetadata cm, String parentObjectId, int size, CollectionType type, StringBuilder html) {
        // if there is more than 1 entry in the collection, put a "show all" link that will link
        // to the HQL query page that will show all results.
        if (size <= 1) {
            return;
        }
        if (!isEmpty(parentObjectId)) {
            ClassMetadata colCm = getResolvedClassMetadata(gsf.getSessionFactory(), cls);
            StringBuilder hqlQuery = new StringBuilder();
            hqlQuery.append("from ");
            // just pick the first object from the collection
            // need to make sure we use the root entity name to cover hierarchies like FilePointer.
            // for example, the entityName might be ImageFilePointer, but we want the show all link to
            // use the root entity name of FilePointer (to include other FilePointer types).
            hqlQuery.append(colCm instanceof SingleTableEntityPersister ? ((SingleTableEntityPersister) colCm).getRootEntityName() : colCm.getEntityName());
            hqlQuery.append(" where ");
            hqlQuery.append(((OneToManyPersister)type.getAssociatedJoinable((SessionFactoryImplementor) gsf.getSessionFactory())).getMappedByProperty());
            hqlQuery.append(" = ");
            hqlQuery.append(parentObjectId);
            Map<String, String> extraParams = Collections.singletonMap("hql", hqlQuery.toString());
            doItemLink(html, "{show all " + size + "}", extraParams);
            html.append("<br/>");
        } else {
            html.append("{show all not available}<br/>");
        }
    }

    private void doSingleRowValue(SessionFactory sf, Object val, Type type, StringBuilder html) throws HibernateException {
        Class cls = val.getClass();
        ClassMetadata cm = getResolvedClassMetadata(sf, cls);

        //this is a complex object so create a hyperlink to it and display its id
        if (cm != null) {
            Map<String, String> extraParams = newHashMap();
            extraParams.put("type", cm.getEntityName());
            StringBuilder linkText = new StringBuilder();
            // bl: for some reason, sometimes the val.getOid() doesn't work, which causes id to be null and an NPE to
            // be thrown down below.
            val = concrete(val);
            Serializable id = cm.getIdentifier(val);
            Type idType = cm.getIdentifierType();
            if (idType.isComponentType() && idType instanceof ComponentType) {
                ComponentType compType = (ComponentType) idType;
                String[] propertyNames = compType.getPropertyNames();
                Object[] propertyValues = compType.getPropertyValues(id, EntityMode.POJO);
                StringBuilder idPropsString = new StringBuilder();
                linkText.append("{");
                for (int i = 0; i < propertyNames.length; i++) {
                    String propertyName = propertyNames[i];
                    Object propertyValue = propertyValues[i];
                    idPropsString.append(propertyName).append("=").append(propertyValue);
                    linkText.append(propertyName).append("=").append(propertyValue);
                    if (i < (propertyNames.length - 1)) {
                        idPropsString.append("&");
                        linkText.append(",");
                    }
                }
                linkText.append("}");
                extraParams.put("idProps", idPropsString.toString());
            } else {
                extraParams.put("oid", id.toString());
                linkText.append(id.toString());
            }
            doItemLink(html, linkText.toString(), extraParams);

            //simple value, so display the actual value
        } else {
            // bl: handle serialized properties as JSON
            if (type != null && (type instanceof CustomType) && type.sqlTypes(null)[0] == Types.BLOB && !val.getClass().isArray()) {
                JsonFactory jsonFactory = new JsonFactory();
                jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
                ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
                // bl: let's always use pretty printing
                objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                try {
                    html.append(HtmlTextMassager.convertCrAndLfToHtml(HtmlTextMassager.convertConsecutiveSpacesToNbsp(objectMapper.writeValueAsString(val))));
                } catch (IOException e) {
                    throw UnexpectedError.getRuntimeException("Failed serializing Object to JSON with Jackson!", e);
                }
            } else {
                String sVal;
                // bl: Calendar.toString isn't very easy to read. this will make it a lot more clear.
                if (val instanceof Calendar) {
                    Calendar cal = (Calendar) val;
                    sVal = FastDateFormat.getInstance("yyyy-MM-dd", cal.getTimeZone()).format(cal);
                } else if (val instanceof TimeZone) {
                    TimeZone timeZone = (TimeZone) val;
                    sVal = timeZone.getID();
                } else {
                    sVal = val.toString();
                }
                if (isEmpty(sVal)) {
                    html.append("&nbsp;");
                } else {
                    html.append(HtmlTextMassager.disableHtml(sVal));
                }
            }
        }
    }

    private ClassMetadata getResolvedClassMetadata(SessionFactory sf, Class cls) {
        ClassMetadata cm = getClassMetadataBase(sf, cls);

        if (cm != null) {
            return cm;
        }

        cls = cls.getSuperclass();

        //figure out if this is link to a complex object
        if (cls != null) {
            return getClassMetadataBase(sf, cls);
        }

        return null;
    }

    private ClassMetadata getClassMetadataBase(SessionFactory sf, Class cls) {
        try {
            return sf.getClassMetadata(cls);
        } catch(MappingException e) {
            return null;
        }
    }

    private void doItemLink(StringBuilder html, String linkText, Map<String, String> extraParams) {
        Map<String, String> params = newHashMap(extraParams);
        params.put("a", "query");
        params.put("partition", currentPartition.getOid().toString());

        html.append("<a href=\"");
        html.append(IPHTMLUtil.getParametersAsURL(getHqlQueryBaseUrl(), params));
        html.append("\">");
        html.append(linkText);
        html.append("</a>");
    }

    private String getHqlQueryBaseUrl() {
        return NetworkRegistry.getInstance().getClusterCpRelativePath() + URL_BASE;
    }

    private Object[] getRow(Object rowObj) {
        Object[] row;
        if (rowObj instanceof Object[]) {
            row = (Object[]) rowObj;
        } else {
            row = new Object[1];
            row[0] = rowObj;
        }
        return row;
    }

    public String getHtml() {
        return output.toString();
    }

    public String getHql() {
        return hql;
    }

    @BypassHtmlDisable
    public void setHql(String hql) {
        this.hql = hql;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public Map<String, String> getPartitions() {
        return partitions;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    public String getIdProps() {
        return idProps;
    }

    @BypassHtmlDisable
    public void setIdProps(String idProps) {
        this.idProps = idProps;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isExplain() {
        return explain;
    }

    public void setExplain(boolean explain) {
        this.explain = explain;
    }

    public Map<Partition, List<ExplainResults>> getExplainResults() {
        return explainResults;
    }

    public List<String> getColNames() {
        return colNames;
    }

    public Class getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(Class objectClass) {
        this.objectClass = objectClass;
    }

    public OID getObjectOid() {
        return objectOid;
    }

    public void setObjectOid(OID objectOid) {
        this.objectOid = objectOid;
    }

    public Map<String, String> getObjectTypes() {
        return objectTypes;
    }

    public static final String CLUSTER_ADMIN_MENU_RESOURCE = "clusterAdmin";

    @Override
    public String getSubMenuResource() {
        return CLUSTER_ADMIN_MENU_RESOURCE;
    }

    @Override
    public String getNestedSubMenuResource() {
        return ACTION_NAME;
    }

    public static class ExplainResults {
        private String sql;
        private List<List<Object>> explain;

        public ExplainResults(String sql, List<List<Object>> explain) {
            this.sql = sql;
            this.explain = explain;
        }

        public String getSql() {
            return sql;
        }

        public List<List<Object>> getExplain() {
            return explain;
        }
    }
}