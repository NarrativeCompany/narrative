package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.shared.services.AuthorProvider;

import javax.persistence.Transient;

import java.sql.Timestamp;

/**
 * Date: May 20, 2009
 * Time: 4:04:33 PM
 *
 * @author brian
 */
public interface PostBase extends AuthorProvider {
    @Transient
    public OID getOid();

    @Transient
    public String getBody();

    @Transient
    public String getBodyResolved();

    @Transient
    public String getBodyForEmail();

    @Transient
    public String getBodyAsExtract();

    @Transient
    public FilePointerSet getFilePointerSet();

    public void setFilePointerSet(FilePointerSet filePointerSet);

    @Transient
    public Timestamp getEditDatetime();

    @Transient
    public Composition getComposition();

}
