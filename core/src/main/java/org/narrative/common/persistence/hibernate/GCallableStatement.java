package org.narrative.common.persistence.hibernate;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Oct 27, 2006
 * Time: 4:13:55 PM
 */
public class GCallableStatement extends GPreparedStatement implements CallableStatement {
    private CallableStatement callableStatement;

    public GCallableStatement(String sql, CallableStatement callableStatement) {
        super(sql, callableStatement);
        this.callableStatement = callableStatement;
    }

    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType, scale);
    }

    public boolean wasNull() throws SQLException {
        return callableStatement.wasNull();
    }

    public String getString(int parameterIndex) throws SQLException {
        return callableStatement.getString(parameterIndex);
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return callableStatement.getBoolean(parameterIndex);
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return callableStatement.getByte(parameterIndex);
    }

    public short getShort(int parameterIndex) throws SQLException {
        return callableStatement.getShort(parameterIndex);
    }

    public int getInt(int parameterIndex) throws SQLException {
        return callableStatement.getInt(parameterIndex);
    }

    public long getLong(int parameterIndex) throws SQLException {
        return callableStatement.getLong(parameterIndex);
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return callableStatement.getFloat(parameterIndex);
    }

    public double getDouble(int parameterIndex) throws SQLException {
        return callableStatement.getDouble(parameterIndex);
    }

    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        return callableStatement.getBigDecimal(parameterIndex, scale);
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return callableStatement.getBytes(parameterIndex);
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return callableStatement.getDate(parameterIndex);
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return callableStatement.getTime(parameterIndex);
    }

    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return callableStatement.getTimestamp(parameterIndex);
    }

    public Object getObject(int parameterIndex) throws SQLException {
        return callableStatement.getObject(parameterIndex);
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return callableStatement.getBigDecimal(parameterIndex);
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return callableStatement.getObject(i, map);
    }

    public Ref getRef(int i) throws SQLException {
        return callableStatement.getRef(i);
    }

    public Blob getBlob(int i) throws SQLException {
        return callableStatement.getBlob(i);
    }

    public Clob getClob(int i) throws SQLException {
        return callableStatement.getClob(i);
    }

    public Array getArray(int i) throws SQLException {
        return callableStatement.getArray(i);
    }

    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        return callableStatement.getDate(parameterIndex, cal);
    }

    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        return callableStatement.getTime(parameterIndex, cal);
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        return callableStatement.getTimestamp(parameterIndex, cal);
    }

    public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
        callableStatement.registerOutParameter(paramIndex, sqlType, typeName);
    }

    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType);
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType, scale);
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType, typeName);
    }

    public URL getURL(int parameterIndex) throws SQLException {
        return callableStatement.getURL(parameterIndex);
    }

    public void setURL(String parameterName, URL val) throws SQLException {
        callableStatement.setURL(parameterName, val);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        callableStatement.setNull(parameterName, sqlType);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        callableStatement.setBoolean(parameterName, x);
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        callableStatement.setByte(parameterName, x);
    }

    public void setShort(String parameterName, short x) throws SQLException {
        callableStatement.setShort(parameterName, x);
    }

    public void setInt(String parameterName, int x) throws SQLException {
        callableStatement.setInt(parameterName, x);
    }

    public void setLong(String parameterName, long x) throws SQLException {
        callableStatement.setLong(parameterName, x);
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        callableStatement.setFloat(parameterName, x);
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        callableStatement.setDouble(parameterName, x);
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        callableStatement.setBigDecimal(parameterName, x);
    }

    public void setString(String parameterName, String x) throws SQLException {
        callableStatement.setString(parameterName, x);
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {
        callableStatement.setBytes(parameterName, x);
    }

    public void setDate(String parameterName, Date x) throws SQLException {
        callableStatement.setDate(parameterName, x);
    }

    public void setTime(String parameterName, Time x) throws SQLException {
        callableStatement.setTime(parameterName, x);
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        callableStatement.setTimestamp(parameterName, x);
    }

    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        callableStatement.setAsciiStream(parameterName, x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        callableStatement.setBinaryStream(parameterName, x, length);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        callableStatement.setObject(parameterName, x, targetSqlType, scale);
    }

    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        callableStatement.setObject(parameterName, x, targetSqlType);
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        callableStatement.setObject(parameterName, x);
    }

    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        callableStatement.setCharacterStream(parameterName, reader, length);
    }

    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        callableStatement.setDate(parameterName, x, cal);
    }

    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        callableStatement.setTime(parameterName, x, cal);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        callableStatement.setTimestamp(parameterName, x, cal);
    }

    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        callableStatement.setNull(parameterName, sqlType, typeName);
    }

    public String getString(String parameterName) throws SQLException {
        return callableStatement.getString(parameterName);
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        return callableStatement.getBoolean(parameterName);
    }

    public byte getByte(String parameterName) throws SQLException {
        return callableStatement.getByte(parameterName);
    }

    public short getShort(String parameterName) throws SQLException {
        return callableStatement.getShort(parameterName);
    }

    public int getInt(String parameterName) throws SQLException {
        return callableStatement.getInt(parameterName);
    }

    public long getLong(String parameterName) throws SQLException {
        return callableStatement.getLong(parameterName);
    }

    public float getFloat(String parameterName) throws SQLException {
        return callableStatement.getFloat(parameterName);
    }

    public double getDouble(String parameterName) throws SQLException {
        return callableStatement.getDouble(parameterName);
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return callableStatement.getBytes(parameterName);
    }

    public Date getDate(String parameterName) throws SQLException {
        return callableStatement.getDate(parameterName);
    }

    public Time getTime(String parameterName) throws SQLException {
        return callableStatement.getTime(parameterName);
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return callableStatement.getTimestamp(parameterName);
    }

    public Object getObject(String parameterName) throws SQLException {
        return callableStatement.getObject(parameterName);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return callableStatement.getBigDecimal(parameterName);
    }

    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        return callableStatement.getObject(parameterName, map);
    }

    public Ref getRef(String parameterName) throws SQLException {
        return callableStatement.getRef(parameterName);
    }

    public Blob getBlob(String parameterName) throws SQLException {
        return callableStatement.getBlob(parameterName);
    }

    public Clob getClob(String parameterName) throws SQLException {
        return callableStatement.getClob(parameterName);
    }

    public Array getArray(String parameterName) throws SQLException {
        return callableStatement.getArray(parameterName);
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return callableStatement.getDate(parameterName, cal);
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return callableStatement.getTime(parameterName, cal);
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        return callableStatement.getTimestamp(parameterName, cal);
    }

    public URL getURL(String parameterName) throws SQLException {
        return callableStatement.getURL(parameterName);
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        return callableStatement.getRowId(parameterIndex);
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return callableStatement.getRowId(parameterName);
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        callableStatement.setRowId(parameterName, x);
    }

    public void setNString(String parameterName, String value) throws SQLException {
        callableStatement.setNString(parameterName, value);
    }

    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        callableStatement.setNCharacterStream(parameterName, value, length);
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        callableStatement.setNClob(parameterName, value);
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        callableStatement.setClob(parameterName, reader, length);
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        callableStatement.setBlob(parameterName, inputStream, length);
    }

    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        callableStatement.setNClob(parameterName, reader, length);
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        return callableStatement.getNClob(parameterIndex);
    }

    public NClob getNClob(String parameterName) throws SQLException {
        return callableStatement.getNClob(parameterName);
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        callableStatement.setSQLXML(parameterName, xmlObject);
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return callableStatement.getSQLXML(parameterIndex);
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return callableStatement.getSQLXML(parameterName);
    }

    public String getNString(int parameterIndex) throws SQLException {
        return callableStatement.getNString(parameterIndex);
    }

    public String getNString(String parameterName) throws SQLException {
        return callableStatement.getNString(parameterName);
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return callableStatement.getNCharacterStream(parameterIndex);
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return callableStatement.getNCharacterStream(parameterName);
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return callableStatement.getCharacterStream(parameterIndex);
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        return callableStatement.getCharacterStream(parameterName);
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        callableStatement.setBlob(parameterName, x);
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        callableStatement.setClob(parameterName, x);
    }

    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        callableStatement.setAsciiStream(parameterName, x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        callableStatement.setBinaryStream(parameterName, x, length);
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        callableStatement.setCharacterStream(parameterName, reader, length);
    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        callableStatement.setAsciiStream(parameterName, x);
    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        callableStatement.setBinaryStream(parameterName, x);
    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        callableStatement.setCharacterStream(parameterName, reader);
    }

    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        callableStatement.setNCharacterStream(parameterName, value);
    }

    public void setClob(String parameterName, Reader reader) throws SQLException {
        callableStatement.setClob(parameterName, reader);
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        callableStatement.setBlob(parameterName, inputStream);
    }

    public void setNClob(String parameterName, Reader reader) throws SQLException {
        callableStatement.setNClob(parameterName, reader);
    }

    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return callableStatement.getObject(parameterIndex, type);
    }

    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return callableStatement.getObject(parameterName, type);
    }
}
