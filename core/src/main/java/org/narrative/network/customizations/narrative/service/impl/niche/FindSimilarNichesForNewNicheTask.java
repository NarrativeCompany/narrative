package org.narrative.network.customizations.narrative.service.impl.niche;

import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.service.api.model.input.SimilarNicheSearchRequest;

import java.util.List;

/**
 * Date: 10/18/18
 * Time: 9:57 AM
 *
 * @author brian
 */
public class FindSimilarNichesForNewNicheTask extends SubmitNicheDetailsBaseTask<List<Niche>, SimilarNicheSearchRequest> {
    public FindSimilarNichesForNewNicheTask(SimilarNicheSearchRequest similarNicheSearchRequest) {
        super(similarNicheSearchRequest);
    }

    @Override
    protected List<Niche> doMonitoredTask() {
        // bl: you might wonder why we need this task class at all. we need it for the request validation provided
        // in the super class here.
        return getAreaContext().doAreaTask(new FindSimilarNichesTask(nicheInput.getName(), nicheInput.getDescription(), null));
    }
}
