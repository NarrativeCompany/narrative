package org.narrative.common.persistence;

import org.narrative.common.web.struts.StrutsStringConverterBase;

/**
 * Date: Dec 22, 2005
 * Time: 1:22:45 AM
 *
 * @author Brian
 */
public class OIDConverter extends StrutsStringConverterBase {
    @Override
    protected Class getConvertedClass() {
        return OID.class;
    }

    @Override
    protected Object getConvertedObjectFromString(String val) {
        return OID.getOIDFromString(val);
    }
}
