package org.narrative.network.shared.util;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Class to inject as a mix-in to all Object serialization so that our {@link NetworkLoggerPropertyFilter} applies
 * to every object that is serialized during {@link NetworkLogger} log serialization.
 *
 * Date: 9/30/18
 * Time: 9:06 PM
 *
 * @author brian
 */
@JsonFilter(NetworkLoggerPropertyFilterMixIn.FILTER_NAME)
class NetworkLoggerPropertyFilterMixIn {
    static final String FILTER_NAME = "NetworkLoggerPropertyFilterMixIn";
}
