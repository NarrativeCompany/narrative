package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.shared.util.NetworkLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Date: Mar 24, 2006
 * Time: 9:07:41 AM
 *
 * @author Brian
 */
public class FileDataUtil {

    private static final NetworkLogger logger = new NetworkLogger(FileDataUtil.class);

    /**
     * sort a List of UploadedFileData by the order field.
     *
     * @param fileDataToSort the list of UploadedFileData to sort.
     */
    public static <T extends FileData> void sortFileDataByOrder(List<T> fileDataToSort) {
        Collections.sort(fileDataToSort, new Comparator<T>() {
            public int compare(T o1, T o2) {
                int ret = Integer.valueOf(o1.getOrder()).compareTo(o2.getOrder());
                if (ret != 0) {
                    return ret;
                }
                return OID.compareOids(o1.getUniqueOid(), o2.getUniqueOid());
            }
        });
    }

    public static <T extends FileData> List<T> getSortedFileDataFromMap(Map<OID, T> uniqueOidToFileData) {
        if (uniqueOidToFileData == null) {
            return Collections.emptyList();
        }
        List<T> ret = new ArrayList<T>(uniqueOidToFileData.values());
        // sort the album images by order before returning them.
        sortFileDataByOrder(ret);
        return ret;
    }

    public static int getMaxOrderFromFileDataCollection(Collection<? extends FileData> fileDatas) {
        int ret = 0;
        if (fileDatas != null) {
            for (FileData fileData : fileDatas) {
                if (fileData != null) {
                    ret = Math.max(ret, fileData.getOrder());
                }
            }
        }
        return ret;
    }

    public static <T extends FileData> void deleteTempUploadedFilesFromFileDataCollection(Collection<T> col) {
        for (T fileData : col) {
            // todo: figure out why this isn't working.  try to delete the file, but if we can't, then don't give an error.
            fileData.deleteAllTempFiles();
        }
    }

    public static void safeDeleteFile(File fileToDelete) {
        if (fileToDelete.exists()) {
            if (!fileToDelete.delete()) {
                logger.warn("Failed deleting temp uploaded file! " + fileToDelete.getPath());
            } else if (fileToDelete.exists()) {
                logger.error("Failed deleting temp uploaded file even though Java said it worked! " + fileToDelete.getPath());
            }
        }
    }

    public static void safeDeleteFileIterator(Iterator<File> fileIterator) {
        while (fileIterator.hasNext()) {
            File fileToDelete = fileIterator.next();
            fileIterator.remove();
            FileDataUtil.safeDeleteFile(fileToDelete);
        }
    }
}
