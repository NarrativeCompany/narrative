package org.narrative.network.core.content.base;

import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;

/**
 * Date: 10/21/14
 * Time: 10:51 AM
 *
 * @author brian
 */
public interface SEOObjectDAO<T extends SEOObject> {
    public T getForPrettyURLString(AreaRlm areaRlm, Portfolio portfolio, ContentType contentType, String prettyUrlString);
}
