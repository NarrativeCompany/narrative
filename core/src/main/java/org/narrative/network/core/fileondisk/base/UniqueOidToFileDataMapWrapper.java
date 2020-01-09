package org.narrative.network.core.fileondisk.base;

import org.narrative.common.core.services.interceptors.SubPropertySettable;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.content.base.FileData;
import com.opensymphony.xwork2.util.CreateIfNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Struts doesn't support nested maps via ognl.  I used to have:
 * Map<FileUsageType, Map<OID,FileData> getUploadedFilesMap(),
 * but Ognl was giving a bunch of errors and it wasn't working correctly.
 * So, now we have to wrap nested maps in another class in order to facilitate this.
 * Yay.
 */
public class UniqueOidToFileDataMapWrapper<T extends FileData> {

    public static final String UNIQUE_OID_TO_FILE_DATA_MAP_PARAM = "uniqueOidToFileDataMap";

    @CreateIfNull(false)
    private final Map<OID, T> uniqueOidToFileDataMap = new HashMap<OID, T>();
    private int maxOrder = -1;

    public UniqueOidToFileDataMapWrapper() {}

    /**
     * get the unique oid to file data map
     * nb. no longer specifying @AfterPrepare here.  the containing uploadedFilesMap can
     * specify whether @AfterPrepare should be used or not. CreateContentAction no longer uses it.
     *
     * @return the unique oid to file data map
     */
    @SubPropertySettable(depth = 2)
    public Map<OID, T> getUniqueOidToFileDataMap() {
        return uniqueOidToFileDataMap;
    }

    public int getMaxOrder() {
        return maxOrder;
    }

    public void setMaxOrder(int maxOrder) {
        this.maxOrder = maxOrder;
    }

    @Nullable
    public T getSingleFileData() {
        if (uniqueOidToFileDataMap != null) {
            for (T t : uniqueOidToFileDataMap.values()) {
                if (t != null && t.isInclude()) {
                    return t;
                }
            }
        }
        return null;
    }
}
