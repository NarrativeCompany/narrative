package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/27/18
 * Time: 3:51 PM
 */
public class CreateReferendumTask extends AreaTaskImpl<Referendum> {
    private final ChannelConsumer channelConsumer;
    private final ReferendumType type;

    public CreateReferendumTask(ChannelConsumer channelConsumer, ReferendumType type) {
        super(true);
        this.channelConsumer = channelConsumer;
        this.type = type;
    }

    @Override
    protected Referendum doMonitoredTask() {
        Partition compositionPartition = PartitionType.COMPOSITION.getBalancedPartition();

        // jw: first, create the referendum!
        Referendum referendum = new Referendum(channelConsumer, type, compositionPartition);
        // jw: this save will populate the OID so that we can use that below when setting up the referendum.
        Referendum.dao().save(referendum);

        // jw: next, let's create the Composition for the Referendum!
        getNetworkContext().doCompositionTask(compositionPartition, new CompositionTaskImpl<Object>(true) {
            @Override
            protected Object doMonitoredTask() {
                // jw: create the composition with the same OID as the referendum!
                Composition composition = new Composition(CompositionType.REFERENDUM, null, Area.dao().getNarrativePlatformArea());
                composition.setOid(referendum.getOid());
                composition.setBody("");
                Composition.dao().save(composition);

                return null;
            }
        });

        return referendum;
    }
}
