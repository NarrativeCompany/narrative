package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/28/18
 * Time: 11:05 AM
 */
public class FetchMostPopularCommentsForReferendumsTask extends AreaTaskImpl<Map<Referendum, List<Reply>>> {
    private final Collection<Referendum> referendums;

    public FetchMostPopularCommentsForReferendumsTask(Collection<Referendum> referendums) {
        super(false);
        this.referendums = referendums;
    }

    @Override
    protected Map<Referendum, List<Reply>> doMonitoredTask() {
        Map<Referendum, List<Reply>> results = new HashMap<>();

        // jw: we need to process each referendum in its own CompositionTask, to ensure that we have the right composition
        //     in scope when fetching the replies.
        for (Referendum referendum : referendums) {
            // jw: since this is only for the first page, lets use the same task we will use to fetch more, but allow
            //     the task to default the extra inputs to allow all results.
            results.put(referendum, getNetworkContext().doCompositionTask(referendum.getCompositionPartition(), new GetReferendumCommentsTask(referendum, null, null)));
        }

        return results;
    }
}
