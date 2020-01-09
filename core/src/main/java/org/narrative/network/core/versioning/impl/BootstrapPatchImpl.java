package org.narrative.network.core.versioning.impl;

import org.narrative.common.util.IPUtil;
import org.narrative.network.core.versioning.BootstrapPatch;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 10, 2007
 * Time: 12:04:14 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BootstrapPatchImpl implements BootstrapPatch {
    private final String name;

    protected BootstrapPatchImpl(String name) {
        this.name = name;
    }

    protected BootstrapPatchImpl() {
        this.name = IPUtil.getClassSimpleName(this.getClass());
    }

    public String getName() {
        return name;
    }
}
