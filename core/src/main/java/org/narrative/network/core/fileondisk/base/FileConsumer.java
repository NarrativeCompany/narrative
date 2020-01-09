package org.narrative.network.core.fileondisk.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.shared.security.PrimaryRole;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Apr 13, 2007
 * Time: 10:40:01 AM
 */

public interface FileConsumer {

    /**
     * FileConsumer no longer extends DAOObject since we now support non-database FileConsumers to handle
     * Amazon images.  we still need an OID in certain cases, however, so add the getOid() method to FileConsumer.
     *
     * @return the OID for this FileConsumer object, if there is one.
     */
    public OID getOid();

    /**
     * @return Return the FileConsumerType that you represent.
     */
    public FileConsumerType getFileConsumerType();

    /**
     * @return the file URL base to use when generating a file URL of this type
     */
    public String getFileUrlBase();

    /**
     * based on whether this is a primary picture or primary enclosure, get the FilePointerBase
     *
     * @param isPrimaryPicture true if getting the FilePointerBase for the primary picture
     * @return the FilePointerBase given the params
     */
    public FileMetaDataProvider getFileMetaDataProvider(boolean isPrimaryPicture);

    /**
     * Returns all the information that FileSendAction needs to send the file back.  Should do all required securty checks
     * and return any checks as an AccessViolation object in the FileConsumerFileInfo object
     *
     * @param currentRole    The current role who is trying to view the file
     * @param filePointerOid
     * @param primaryPicture Is this a request for a primary picture
     */
    public FileConsumerFileInfo getFileInfo(PrimaryRole currentRole, OID filePointerOid, boolean primaryPicture);

    public AuthZone getAuthZone();

}
