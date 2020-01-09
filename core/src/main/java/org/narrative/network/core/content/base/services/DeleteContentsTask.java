package org.narrative.network.core.content.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.composition.base.services.DeleteCompositionConsumer;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 12/10/13
 * Time: 11:27 AM
 */
public class DeleteContentsTask extends AreaTaskImpl<Integer> {
    private final List<OID> contentOids;

    public DeleteContentsTask(List<OID> contentOids) {
        this.contentOids = contentOids;
    }

    @Override
    protected Integer doMonitoredTask() {
        if (isEmptyOrNull(contentOids)) {
            return 0;
        }
        // bl: totally stupid and lame, but to work around the dreaded "collection not processed by flush" error,
        // we need to make sure that the AreaRlm and Portfolio objects all get properly initialized in the context
        // of this session. so, just do explicit initializations here and call it good.
        HibernateUtil.initializeObject(getAreaContext().getAreaRlm());
        for (Content content : Content.dao().getObjectsFromIDs(contentOids)) {
            HibernateUtil.initializeObject(content.getPortfolio());
            // jw: because this task is only used when deleting mandatory collection objects (Calendar, Product) lets
            //     always bypass registering member has post deleted by moderator recipes.
            getAreaContext().doAreaTask(new DeleteCompositionConsumer(content));
        }

        return contentOids.size();
    }
}
