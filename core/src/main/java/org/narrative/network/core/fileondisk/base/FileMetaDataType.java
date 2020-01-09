package org.narrative.network.core.fileondisk.base;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.usertype.UserType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Date: Jun 15, 2006
 * Time: 9:09:50 AM
 *
 * @author Brian
 */
public class FileMetaDataType implements UserType, Serializable {
    private static final long serialVersionUID = -3889568733399169898L;
    public static final String TYPE = "org.narrative.network.core.fileondisk.base.FileMetaDataType";

    public int[] sqlTypes() {
        return new int[]{sqlType()};
    }

    public int sqlType() {
        return Types.BLOB;
    }

    public Class returnedClass() {
        return FileMetaData.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return EqualsHelper.equals(x, y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        String name = names[0];
        Object val = rs.getObject(name);

        //Object is null
        if (val == null || rs.wasNull()) {
            return null;
        }

        //Stored as a string in the database
        if (!(val instanceof byte[])) {
            throw new HibernateException("Database column '" + name + "' is not compatable with FileMetaData!");
        }

        byte[] bytes = (byte[]) val;

        return getFileMetaDataFromBytes(bytes);
    }

    public static FileMetaData getFileMetaDataFromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length <= 1) {
            return null;
        }

        byte fileTypeByte = bytes[0];
        FileType fileType = EnumRegistry.getForId(FileType.class, fileTypeByte);
        if (fileType == null) {
            throw UnexpectedError.getRuntimeException("Failed reading file metadata bytes.  Unknown type from byte: " + fileTypeByte, true);
        }
        Class<? extends FileMetaData> fileMetaDataClass = fileType.getFileMetaDataClass();
        if (fileMetaDataClass == null) {
            return null;
        }

        FileMetaData ret;
        try {
            ret = fileMetaDataClass.newInstance();
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed creating instance of FileMetaData class.  Must have default constructor specified in class: " + fileMetaDataClass, t, true);
        }

        byte[] fileTypeSpecificBytes = new byte[bytes.length - 1];
        System.arraycopy(bytes, 1, fileTypeSpecificBytes, 0, fileTypeSpecificBytes.length);
        ret.setMetaData(fileTypeSpecificBytes);
        return ret;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }

        st.setBytes(index, getMetadataBytes((FileMetaData) value));
    }

    public static byte[] getMetadataBytes(FileMetaData fmd) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(fmd.getFileType().getId());
            byte[] fileMetaDataBytes = fmd.serialize();
            for (byte b : fileMetaDataBytes) {
                dos.writeByte(b);
            }
        } catch (IOException ioe) {
            throw UnexpectedError.getRuntimeException("Failed writing out file meta data for field in database!", ioe, true);
        }

        return baos.toByteArray();
    }

    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        FileMetaData fmd = (FileMetaData) value;
        FileMetaData ret;
        try {
            ret = fmd.getClass().newInstance();
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed creating new instance of FileMetaData class: " + fmd.getClass(), t, true);
        }
        ret.setMetaData(fmd.serialize());
        return ret;
    }

    public boolean isMutable() {
        return true;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        if (value instanceof Serializable) {
            return (Serializable) value;
        } else {
            return null;
        }
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
