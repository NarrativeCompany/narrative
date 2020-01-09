package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.shared.services.AuthorProvider;

/**
 * Date: Feb 9, 2006
 * Time: 5:18:48 PM
 *
 * @author Brian
 */
public interface ReplyAuthorProvider extends AuthorProvider {
    public OID getOid();
}
