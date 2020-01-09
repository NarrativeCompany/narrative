package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.trace.TraceItem;
import org.narrative.common.util.trace.TraceManager;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Oct 27, 2006
 * Time: 4:07:05 PM
 */
public class GPreparedStatement extends GStatement implements PreparedStatement {
    private final PreparedStatement preparedStatement;
    private final String sql;
    private ArrayList<Object> params;

    GPreparedStatement(String sql, PreparedStatement preparedStatement) {
        super(preparedStatement);
        this.sql = sql;
        this.preparedStatement = preparedStatement;
    }

    public String getSql() {
        return sql;
    }

    public List getParams() {
        return params;
    }

    public ResultSet executeQuery() throws SQLException {
        TraceItem traceItem = null;
        if (trace) {
            traceItem = TraceManager.startTrace(new GQueryStats(sql, params));
        }
        try {
            return preparedStatement.executeQuery();
        } finally {
            if (trace) {
                TraceManager.endTrace(traceItem);
            }
        }
    }

    public int executeUpdate() throws SQLException {
        TraceItem traceItem = null;
        if (trace) {
            traceItem = TraceManager.startTrace(new GQueryStats(sql, params));
        }
        try {
            return preparedStatement.executeUpdate();
        } finally {
            if (trace) {
                TraceManager.endTrace(traceItem);
            }
        }

    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        preparedStatement.setNull(parameterIndex, sqlType);
        logParam(parameterIndex, null);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        preparedStatement.setBoolean(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        preparedStatement.setByte(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        preparedStatement.setShort(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        preparedStatement.setInt(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        preparedStatement.setLong(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        preparedStatement.setFloat(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        preparedStatement.setDouble(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        preparedStatement.setBigDecimal(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        preparedStatement.setString(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        preparedStatement.setBytes(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        preparedStatement.setDate(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        preparedStatement.setTime(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        preparedStatement.setTimestamp(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setAsciiStream(parameterIndex, x, length);
        logParam(parameterIndex, x);
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setUnicodeStream(parameterIndex, x, length);
        logParam(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setBinaryStream(parameterIndex, x, length);
        logParam(parameterIndex, x);
    }

    public void clearParameters() throws SQLException {
        preparedStatement.clearParameters();
        if (params != null) {
            params = null;
        }

    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scale);
        logParam(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
        logParam(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        preparedStatement.setObject(parameterIndex, x);
        logParam(parameterIndex, x);
    }

    public boolean execute() throws SQLException {
        TraceItem traceItem = null;
        if (trace) {
            traceItem = TraceManager.startTrace(new GQueryStats(sql, params));
        }
        try {
            return preparedStatement.execute();
        } finally {
            if (trace) {
                TraceManager.endTrace(traceItem);
            }
        }

    }

    protected void logParam(int pos, Object param) {
        if (params == null) {
            params = new ArrayList<>();
        }
        params.ensureCapacity(pos);
        while (params.size() < pos) {
            params.add(null);
        }
        params.set(pos - 1, param);
    }

    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    public void setRef(int i, Ref x) throws SQLException {
        preparedStatement.setRef(i, x);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        preparedStatement.setBlob(i, x);
    }

    public void setClob(int i, Clob x) throws SQLException {
        preparedStatement.setClob(i, x);
    }

    public void setArray(int i, Array x) throws SQLException {
        preparedStatement.setArray(i, x);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedStatement.getMetaData();
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        preparedStatement.setDate(parameterIndex, x, cal);
        logParam(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        preparedStatement.setTime(parameterIndex, x, cal);
        logParam(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        preparedStatement.setTimestamp(parameterIndex, x, cal);
        logParam(parameterIndex, x);
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        preparedStatement.setNull(paramIndex, sqlType, typeName);
        logParam(paramIndex, null);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        preparedStatement.setURL(parameterIndex, x);
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedStatement.getParameterMetaData();
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        preparedStatement.setRowId(parameterIndex, x);
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        preparedStatement.setNString(parameterIndex, value);
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        preparedStatement.setNCharacterStream(parameterIndex, value, length);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        preparedStatement.setNClob(parameterIndex, value);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatement.setClob(parameterIndex, reader, length);
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        preparedStatement.setBlob(parameterIndex, inputStream, length);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatement.setNClob(parameterIndex, reader, length);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        preparedStatement.setSQLXML(parameterIndex, xmlObject);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        preparedStatement.setAsciiStream(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        preparedStatement.setBinaryStream(parameterIndex, x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        preparedStatement.setCharacterStream(parameterIndex, reader);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        preparedStatement.setNCharacterStream(parameterIndex, value);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        preparedStatement.setClob(parameterIndex, reader);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        preparedStatement.setBlob(parameterIndex, inputStream);
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        preparedStatement.setNClob(parameterIndex, reader);
    }
}
