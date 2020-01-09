package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: May 9, 2008
 * Time: 12:06:42 PM
 */
public abstract class SearchResultImpl implements SearchResult {
    private final OID oid;
    private final int resultIndex;
    private boolean isHasSetData;

    protected SearchResultImpl(OID oid, int resultIndex) {
        this.oid = oid;
        this.resultIndex = resultIndex;
    }

    public OID getOid() {
        return oid;
    }

    public int getResultIndex() {
        return resultIndex;
    }

    protected final boolean isHasSetData() {
        return isHasSetData;
    }

    protected final void setHasSetData(boolean hasSetData) {
        isHasSetData = hasSetData;
    }

    @Override
    public boolean isValidSearchResult() {
        return isHasSetData();
    }

    @Override
    public Object getIdForManageContent() {
        return getOid();
    }

}