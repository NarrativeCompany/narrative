package org.narrative.network.core.composition.files.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Date: Dec 2, 2005
 * Time: 3:58:30 PM
 *
 * @author Brian
 */
public class FilePointerSetDAO extends CompositionDAOImpl<FilePointerSet, OID> {
    public FilePointerSetDAO() {
        super(FilePointerSet.class);
    }

    public List<FilePointerSet> getAllForCompositionOidsDeepFetch(Set<OID> compositionOids) {
        if (compositionOids == null || compositionOids.isEmpty()) {
            return Collections.emptyList();
        }
        return getGSession().getNamedQuery("filePointerSet.getAllForCompositionOidsDeepFetch").setParameterList("compositionOids", compositionOids).list();
    }
}
