package org.narrative.network.core.composition.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 2, 2005
 * Time: 4:38:13 PM
 */
public class CompositionDAO extends CompositionDAOImpl<Composition, OID> {
    public CompositionDAO() {
        super(Composition.class);
    }

    @Override
    public void save(Composition composition) {
        // bl: save the Composition first.
        // due to the One-To-One relationship mapping change, we will have to manually save the FilePointerSet after.
        FilePointerSet filePointerSet = composition.getFilePointerSet();
        // bl: there's a save order issue here that is giving a null/transient value error on cascade
        // from Composition to FilePointerSet.  FilePointerSet's composition is being reported as a null/transient
        // value.  very strange, since the error is happening from the context of a call to Composition.dao().save.
        // in any event, to fix this one strange scenario, I'm going to set the FilePointerSet to null prior to save.
        // then, once the save is done, we can re-add the FilePointerSet, which should be cascaded to save automatically.
        // LAME.
        composition.setFilePointerSet(null);
        super.save(composition);
        if (exists(filePointerSet)) {
            composition.setFilePointerSet(filePointerSet);
        }
    }

    /**
     * Returns a list of CompositionOids to the number of replies for each Composition
     *
     * @return
     */
    public List<ObjectPair<OID, Integer>> getAllCompositionOidsAndReplyCountsForIndexer() {
        return (List<ObjectPair<OID, Integer>>) getGSession().getNamedQuery("composition.getAllCompositionOidsAndReplyCountsForIndexer").list();
    }

    public Map<OID, String> getCompositionOidToBody(Collection<OID> compositionOids) {
        if (isEmptyOrNull(compositionOids)) {
            return Collections.emptyMap();
        }
        List<ObjectPair<OID, String>> results = getGSession().getNamedQuery("composition.getBodiesForCompositionOids").setParameterList("compositionOids", compositionOids).list();
        return ObjectPair.getAsMap(results);
    }
}
