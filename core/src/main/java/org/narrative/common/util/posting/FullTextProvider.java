package org.narrative.common.util.posting;

import org.jetbrains.annotations.NotNull;

/**
 * Date: Mar 7, 2006
 * Time: 1:16:02 PM
 *
 * @author Brian
 */
public interface FullTextProvider {
    @NotNull
    public String getFullText(boolean includeFileContents);
}
