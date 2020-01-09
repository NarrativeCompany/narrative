package org.narrative.network.core.search.services;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

/**
 * Date: 11/23/11
 * Time: 1:14 PM
 *
 * @author Jonmark Weber
 */
public interface SolrClientTask<R> {
    public R doTask(SolrClient client) throws IOException, SolrServerException;
}
