package org.narrative.network.shared.posting;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.posting.Formattable;
import org.narrative.common.util.posting.FormattableImpl;
import org.narrative.common.util.posting.MessageTextMassager;
import org.narrative.network.core.master.graemlins.Graemlin;

/**
 * Date: Jun 23, 2006
 * Time: 11:42:13 AM
 *
 * @author Brian
 */
public class NetworkFormattableImpl extends FormattableImpl implements Formattable, Scrubbable {

    private boolean forceAllowAnyHtml;

    public NetworkFormattableImpl() {
    }

    public NetworkFormattableImpl(boolean areAllBodyElementsSupported) {
        super(areAllBodyElementsSupported);
    }

    public NetworkFormattableImpl(String body, boolean areAllBodyElementsSupported) {
        super(body, areAllBodyElementsSupported);
    }

    public void scrub() {
        if (!IPStringUtil.isEmpty(getBody())) {
            new MessageTextMassager(this, Graemlin.DEFAULT_GRAEMLIN_MAP, areAllBodyElementsSupported, forceAllowAnyHtml).massageBody();
        }
    }

    public void setForceAllowAnyHtml(boolean isAllowAnyHtml) {
        this.forceAllowAnyHtml = isAllowAnyHtml;
    }
}