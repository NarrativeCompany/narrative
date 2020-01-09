package org.narrative.network.customizations.narrative.service.api.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.content.base.SEOObject;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Date: 2019-02-13
 * Time: 10:03
 *
 * @author jonmark
 */
public abstract class ParseObjectFromUnknownIdTask<T> extends AreaTaskImpl<T> {
    private final String unknownId;
    private final String paramName;

    public ParseObjectFromUnknownIdTask(String unknownId, String paramName) {
        super(false);
        this.unknownId = unknownId;
        this.paramName = paramName;
    }

    protected abstract T getFromOid(OID oid);
    protected abstract T getFromPrettyUrlString(String prettyUrlString);

    @Override
    protected T doMonitoredTask() {
        // jw: first, parse the unknownId into something sensible.
        Pair<OID, String> parsedId = SEOObject.parseUnknownId(unknownId, paramName);

        // jw: if we were unable to parse an ID, then let's short out.
        if (parsedId==null) {
            return null;
        }

        // jw: if we parsed an OID, let's use that.
        if (parsedId.getLeft() != null) {
            return getFromOid(parsedId.getLeft());
        }

        // jw: I guess we did not find an OID, then we should definitely have a prettyUrlString.
        assert parsedId.getRight() != null : "Should always have a prettyUrlString at this point!";
        return getFromPrettyUrlString(parsedId.getRight());
    }
}
