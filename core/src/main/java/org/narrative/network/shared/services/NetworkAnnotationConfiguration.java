package org.narrative.network.shared.services;

import org.narrative.common.core.services.NarrativeAnnotationConfiguration;
import org.narrative.common.persistence.hibernate.integrator.ListenerIntegrator;
import org.narrative.common.persistence.hibernate.integrator.MetadataExtractorIntegrator;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.hibernate.SessionFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 5, 2006
 * Time: 5:31:26 PM
 * A class designed to allow us to exclude certain sql statements during the script creation process
 */
public class NetworkAnnotationConfiguration extends NarrativeAnnotationConfiguration {

    private final PartitionType partitionType;

    private static final Set<String> TABLES_TO_EXCLUDE;
    private static final Set<String> INDEXES_TO_EXCLUDE;

    static {
        Set<String> tablesToExclude = new HashSet<>();
        Set<String> indexesToExclude = new HashSet<>();
        //this is a list of sql fragments that will be matched against all the DDL sql statements that hibernate creates.
        //If the fragment matches any part of the current statement, it will be excluded.

        // todo: figure out a better way to handle these table exclusions.  perhaps we can use some kind of annotation on the entities themselves?
        tablesToExclude.add("StatisticRollup"); //all statistic rollup tables
        tablesToExclude.add("StatisticRollupArea"); //all statistic rollup tables
        tablesToExclude.add("UserDemographics"); // the UserDemographics table
        tablesToExclude.add("UserMembership"); // the UserMembership table

        // bl: need to make sure to prevent creation of our "view" tables that Hibernate
        // will create the schema for.  using format "table ${tableName}" in order to ensure
        // that both the create table and alter table statements will be excluded.

        // global
        indexesToExclude.add("FK5C183CABABFC8BCD");   //DialogParticipant.dialog - prevent duplicate index since it already has a unique index

        // realm
        indexesToExclude.add("fk_portfolioTag_portfolio");  //PortfolioTag.portfolio - prevent duplicate index since it already has a unique index

        // composition

        // jw: set the final version of the collections here!
        TABLES_TO_EXCLUDE = Collections.unmodifiableSet(tablesToExclude);
        INDEXES_TO_EXCLUDE = Collections.unmodifiableSet(indexesToExclude);
    }

    public static NetworkAnnotationConfiguration buildNetworkAnnotationConfiguration(PartitionType partitionType){
        MetadataExtractorIntegrator metadataIntegrator = new MetadataExtractorIntegrator();
        ListenerIntegrator listenerIntegrator = new ListenerIntegrator();
        return new NetworkAnnotationConfiguration(metadataIntegrator, listenerIntegrator, partitionType);
    }

    private NetworkAnnotationConfiguration(MetadataExtractorIntegrator metadataExtractorIntegrator,
                                           ListenerIntegrator listenerIntegrator,
                                           PartitionType partitionType) {
        super(metadataExtractorIntegrator, listenerIntegrator);
        this.partitionType = partitionType;
    }

    @Override
    public Set<String> getTablesToExclude() {
        return TABLES_TO_EXCLUDE;
    }

    @Override
    protected Set<String> getIndexesToExclude() {
        return INDEXES_TO_EXCLUDE;
    }

    @Override
    protected Set<String> getConstraintsToExclude() {
        return Collections.emptySet();
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return partitionType.getGSessionFactory().getSessionFactory();
    }

}
