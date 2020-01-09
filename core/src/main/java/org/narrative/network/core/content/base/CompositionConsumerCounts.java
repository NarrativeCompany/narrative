package org.narrative.network.core.content.base;

import org.narrative.common.persistence.hibernate.HibernateMapType;
import org.narrative.common.util.NarrativeConstants;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionConsumerType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Transient;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 9/12/16
 * Time: 4:39 PM
 *
 * @author brian
 */
@Embeddable
public class CompositionConsumerCounts {

    private Map<CompositionConsumerType, Integer> typeCounts;

    public CompositionConsumerCounts() {}

    public CompositionConsumerCounts(CompositionConsumerCounts counts) {
        this.typeCounts = counts.getTypeCounts() == null ? null : new HashMap<>(counts.getTypeCounts());
    }

    @Basic(fetch = FetchType.LAZY, optional = true)
    @Column(columnDefinition = "varchar(" + NarrativeConstants.MAX_VARCHAR_MYSQL_FIELD_LENGTH + ")")
    @Type(type = HibernateMapType.TYPE, parameters = {@Parameter(name = HibernateMapType.MAP_KEY_TYPE_CLASS, value = CompositionConsumerType.TYPE), @Parameter(name = HibernateMapType.MAP_VALUE_TYPE_CLASS, value = "java.lang.Integer")})
    public Map<CompositionConsumerType, Integer> getTypeCounts() {
        return typeCounts;
    }

    public void setTypeCounts(Map<CompositionConsumerType, Integer> compositionConsumerTypeCounts) {
        this.typeCounts = compositionConsumerTypeCounts;
    }

    public static Object getMapKey(CompositionConsumer compositionConsumer) {
        return compositionConsumer.getCompositionConsumerType();
    }

    private Map<Object, Integer> getCountsMap(CompositionConsumer compositionConsumer, boolean init) {
        Map<CompositionConsumerType, Integer> typeCounts = getTypeCounts();
        if (typeCounts == null && init) {
            setTypeCounts(typeCounts = new HashMap<>());
        }
        // bl: erase the key type so that we can do a generic put into the map
        return (Map) typeCounts;
    }

    @Transient
    public void addCompositionConsumerUsage(CompositionConsumer compositionConsumer) {
        Object mapKey = getMapKey(compositionConsumer);
        Map<Object, Integer> counts = getCountsMap(compositionConsumer, true);
        Integer count = counts.get(mapKey);
        counts.put(mapKey, count == null ? 1 : (count + 1));
    }

    @Transient
    public void subtractCompositionConsumerUsage(CompositionConsumer compositionConsumer) {
        Object mapKey = getMapKey(compositionConsumer);
        Map<Object, Integer> counts = getCountsMap(compositionConsumer, false);
        if (counts == null) {
            return;
        }
        Integer count = counts.get(mapKey);
        if (count == null) {
            return;
        }
        // bl: just remove it from the map if reducing to 0
        if (count <= 1) {
            counts.remove(mapKey);
        } else {
            counts.put(mapKey, count - 1);
        }

        if (counts.isEmpty()) {
            setTypeCounts(null);
        }
    }

    @Transient
    public int getCountForConsumer(CompositionConsumer compositionConsumer) {
        Object mapKey = getMapKey(compositionConsumer);
        Map<Object, Integer> counts = getCountsMap(compositionConsumer, false);
        if (counts == null) {
            return 0;
        }
        Integer count = counts.get(mapKey);
        return count == null ? 0 : count;
    }

    @Transient
    public int getTotalCount() {
        int totalCount = 0;
        {
            Map<CompositionConsumerType, Integer> typeCounts = getTypeCounts();
            if (typeCounts != null) {
                for (Integer value : typeCounts.values()) {
                    if (value == null) {
                        continue;
                    }
                    totalCount += value;
                }
            }
        }
        return totalCount;
    }
}
