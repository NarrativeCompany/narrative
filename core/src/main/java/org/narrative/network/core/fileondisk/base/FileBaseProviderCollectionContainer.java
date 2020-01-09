package org.narrative.network.core.fileondisk.base;

import java.util.Collection;

/**
 * Date: 5/24/12
 * Time: 11:43 AM
 * User: jonmark
 */
public interface FileBaseProviderCollectionContainer<FBP extends FileBaseProvider> {
    public Collection<FBP> getFileBaseProviderCollection();
}
