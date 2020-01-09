package org.narrative.network.core.search;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.apache.lucene.search.BooleanQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Paul
 * Date: May 9, 2008
 */
public class IndexHandlerManager {

    public static boolean isInitialized = false;

    public static boolean isInitialized() {
        return isInitialized;
    }

    private static final Map<IndexType, IndexHandler> indexHandlers;

    static {
        Map<IndexType, IndexHandler> map = new HashMap<IndexType, IndexHandler>();
        for (IndexType indexType : IndexType.getAllIndexTypes()) {
            try {
                map.put(indexType, indexType.getIndexHandlerClass().newInstance());
            } catch (Throwable t) {
                throw UnexpectedError.getRuntimeException("Failed instantiating IndexHandler for indexType/" + indexType, t);
            }
        }
        indexHandlers = Collections.unmodifiableMap(map);
    }

    public static IndexHandler getIndexHandler(IndexType indexType) {
        return indexHandlers.get(indexType);
    }

    public static void init(String solrServerUrl, List<String> solrZookeperHosts, String solrCloudDefaultCollection) {
        // bl: in order to allow for some large boolean queries (e.g. for postal code lookups, which can contain long
        // lists), let's override the default of 1024 to 65535. note that this will also need to be set in solrconfig.xml
        // in order to take effect on the Solr server.
        BooleanQuery.setMaxClauseCount(65535);
        IndexHandler.init(solrServerUrl, solrZookeperHosts, solrCloudDefaultCollection);

        for (IndexHandler indexHandler : indexHandlers.values()) {
            indexHandler.start();
        }

        isInitialized = true;

        IPUtil.EndOfX.endOfAppComing.addRunnable("55StopIndexHandlers", new Runnable() {
            public void run() {
                for (IndexHandler indexHandler : indexHandlers.values()) {
                    indexHandler.stop();
                }
            }
        });
    }
}