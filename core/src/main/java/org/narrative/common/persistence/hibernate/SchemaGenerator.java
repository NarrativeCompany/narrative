package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.IPIOUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.config.JacksonConfiguration;
import org.narrative.config.ValidationConfig;
import org.narrative.config.cache.RedissonConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.cluster.setup.NetworkSetup;
import org.narrative.network.shared.services.NetworkAnnotationConfiguration;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 28, 2005
 * Time: 3:47:00 PM
 */
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        SolrAutoConfiguration.class
})
@Import(value = {NarrativeProperties.class, JacksonConfiguration.class, RedissonConfig.class, ValidationConfig.class})
public class SchemaGenerator {
    private static final NetworkLogger logger = new NetworkLogger(SchemaGenerator.class);
    private static final String SYSTEM_EOL_TOKEN = "[@@@]";
    private static final String NL_TOKEN = "[$$$]";

    public static void main(String[] args) {
        SpringApplication.run(SchemaGenerator.class, args);
    }

    @Bean
    public RunSchemaGenerator runSchemaGenerator(ApplicationContext applicationContext) {
        return new RunSchemaGenerator(applicationContext);
    }

    private static class RunSchemaGenerator implements CommandLineRunner {
        private final ApplicationContext applicationContext;

        public RunSchemaGenerator(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void run(String... args) {
            int res = 0;
            String outputPath = args[0];
            {
                File outputFile = new File(outputPath);
                if (!outputFile.exists()) {
                    IPIOUtil.mkdirs(outputFile);
                } else if (!outputFile.isDirectory()) {
                    outputPath = outputFile.getParent();
                }
            }

            try {
                // bl: set isInstalling=true so that we don't need a database connection in order to generate the schema
                NetworkSetup.doSetup(applicationContext, true);

                boolean isAll = args.length < 2 || IPStringUtil.isStringEqualIgnoreCase(args[1], "all");

                for (PartitionType type : PartitionType.ACTIVE_PARTITION_TYPES) {
                    if (isAll || IPStringUtil.isStringEqualIgnoreCase(args[1], type.toString())) {
                        NetworkAnnotationConfiguration cfg = type.getConfiguration();
                        for (int i = 0; i < 2; i++) {
                            // bl: disable the overrides the second time through so that we can compare
                            // the raw schema created by hibernate vs. our schema.
                            boolean useHibernatesRawSql = i > 0;
                            cfg.setUseHibernatesRawSql(useHibernatesRawSql);

                            Metadata metadata = type.getGSessionFactory().getMetadataExtractorIntegrator().getMetadata();

                            //Make sure we clean any existing file
                            File outFile = new File(outputPath + "/" + type.toString().toLowerCase() + (useHibernatesRawSql ? ".raw" : "") + ".sql");
                            if(outFile.exists()) {
                                outFile.delete();
                            }

                            SchemaExport se = new SchemaExport();
                            se.setFormat(true);
                            se.setDelimiter(";");
                            se.setOutputFile(outFile.getAbsolutePath());
                            se.setHaltOnError(true);
                            se.create(EnumSet.of(TargetType.SCRIPT, TargetType.STDOUT), metadata);

                            //Post process the file for FK name substitution etc.
                            if (!useHibernatesRawSql) {
                                postProcessFile(outFile, cfg);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error generating schema", e);
                res = -1;
            } finally {
                IPUtil.onEndOfApp();
            }

            System.exit(res);
        }

        private static void postProcessFile(File inputFile, NetworkAnnotationConfiguration cfg) throws IOException {
            String script = FileUtils.readFileToString(inputFile);
            String[] frags = script.split(";");

            //Tokenize new lines so we get a single line per statement
            List<String> tokenizedFrags =
                    Arrays.stream(frags)
                            .map(s -> s.replace(System.lineSeparator(), SYSTEM_EOL_TOKEN))
                            .map(s -> s.replace("\n", NL_TOKEN))
                            .collect(Collectors.toList());

            //Fix up the DDL - we should have single line DDL statements at this point
            List<String> resScript = cfg.fixQueries(tokenizedFrags);

            //De-tokenize new lines
            List<String> detokenizedResScript =
                   resScript.stream()
                            .map(s -> s.replace(SYSTEM_EOL_TOKEN, System.lineSeparator()))
                            .map(s -> s.replace(NL_TOKEN, "\n"))
                            .map(s -> StringUtils.isNotBlank(s) ?  s + ";" : "")
                            .collect(Collectors.toList());

            FileUtils.writeLines(inputFile, detokenizedResScript);
        }
    }
}
