package org.narrative.network.shared.context;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 19, 2005
 * Time: 5:33:01 PM
 */
public interface AreaContextAware extends NetworkContextAware, AreaContextHolder {
    public void setAreaContext(AreaContext ctx);
}
