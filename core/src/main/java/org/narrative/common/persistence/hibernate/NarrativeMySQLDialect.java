package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.OIDType;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.util.IPDateUtil;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.ObjectType;
import org.hibernate.type.StandardBasicTypes;

import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 23, 2006
 * Time: 4:46:08 PM
 */
public class NarrativeMySQLDialect extends MySQL57Dialect {
    public NarrativeMySQLDialect() {
        super();
        // bl: Hibernate's Dialect doesn't have this defined by default, so adding it here. started using this
        // when we started exporting Reply data by joining through a temp table. needed for the body.
        registerHibernateType(Types.LONGVARCHAR, StandardBasicTypes.STRING.getName());
        // bl: for Reply.moderationStatus, which is a String enum in MySQL, MySQL's JDBC ResultSet metadata
        // reports it as Types.CHAR with a length of 16. by default, Hibernate only supports single
        // Character chars. so, if it's longer than 1 character, let's use a String instead.
        registerHibernateType(Types.CHAR, 1, StandardBasicTypes.CHARACTER.getName());
        registerHibernateType(Types.CHAR, Integer.MAX_VALUE, StandardBasicTypes.STRING.getName());
        registerFunction("bitwise_and", new MySQLBitwiseAndFunction("bitwise_and", StandardBasicTypes.LONG));
        registerFunction("bitwise_or", new MySQLBitwiseOrFunction("bitwise_or", StandardBasicTypes.LONG));
        registerFunction("bitwise_inversion", new OneArgSQLFunction(StandardBasicTypes.LONG, "~", ""));
        registerFunction("cast_as_signed", new CastAsSignedFunction("cast_as_signed", StandardBasicTypes.LONG));
        registerFunction("lower", new OneArgSQLFunction("lower(", ")"));
        registerFunction("if", new VarArgsSQLFunction("if(", ",", ")"));
        registerFunction("ifint", new VarArgsSQLFunction(StandardBasicTypes.INTEGER, "if(", ",", ")"));
        registerFunction("iflong", new VarArgsSQLFunction(StandardBasicTypes.LONG, "if(", ",", ")"));
        registerFunction("ifbigdecimal", new VarArgsSQLFunction(StandardBasicTypes.BIG_DECIMAL, "if(", ",", ")"));
        registerFunction("ifoid", new VarArgsSQLFunction(OIDType.INSTANCE, "if(", ",", ")"));
        registerFunction("ifstring", new VarArgsSQLFunction(StandardBasicTypes.STRING, "if(", ",", ")"));
        registerFunction("ifdate", new VarArgsSQLFunction(StandardBasicTypes.TIMESTAMP, "if(", ",", ")"));
        registerFunction("ifbool", new VarArgsSQLFunction(StandardBasicTypes.BOOLEAN, "if(", ",", ")"));
        registerFunction("andvars", new VarArgsSQLFunction("", " and ", ""));
        registerFunction("orvars", new VarArgsSQLFunction("", " or ", ""));
        registerFunction("gt", new TwoArgSQLFunction(StandardBasicTypes.BOOLEAN, "", ">", ""));
        registerFunction("lt", new TwoArgSQLFunction(StandardBasicTypes.BOOLEAN, "", "<", ""));
        registerFunction("gte", new TwoArgSQLFunction(StandardBasicTypes.BOOLEAN, "", ">=", ""));
        registerFunction("lte", new TwoArgSQLFunction(StandardBasicTypes.BOOLEAN, "", "<=", ""));
        registerFunction("eq", new TwoArgSQLFunction(StandardBasicTypes.BOOLEAN, "", "=", ""));
        registerFunction("ne", new TwoArgSQLFunction(StandardBasicTypes.BOOLEAN, "", "<>", ""));
        registerFunction("isnull", new VarArgsSQLFunction(StandardBasicTypes.BOOLEAN, "", "", " is null "));
        registerFunction("isnotnull", new VarArgsSQLFunction(StandardBasicTypes.BOOLEAN, "", "", " is not null "));
        registerFunction("date", new OneArgSQLFunction(StandardBasicTypes.DATE, "DATE(", ")"));
        registerFunction("dateformat", new TwoArgSQLFunction(StandardBasicTypes.STRING, "DATE_FORMAT(", ",", ")"));
        registerFunction("dateadd", new TwoArgSQLFunction(StandardBasicTypes.STRING, "ADDDATE(", ",", ")"));
        registerFunction("dateadd_day", new TwoArgSQLFunction(StandardBasicTypes.STRING, "ADDDATE(", ", INTERVAL ", " DAY)"));
        registerFunction("dateadd_month", new TwoArgSQLFunction(StandardBasicTypes.STRING, "ADDDATE(", ", INTERVAL ", " MONTH)"));
        registerFunction("datediff", new TwoArgSQLFunction(StandardBasicTypes.INTEGER, "DATEDIFF(", ",", ")"));
        registerFunction("bitwise_and_aggr", new OneArgSQLFunction("BIT_AND(", ")"));
        registerFunction("bitwise_or_aggr", new OneArgSQLFunction("BIT_OR(", ")"));
        registerFunction("timestamp_as_hour_long", new OneArgSQLFunction(StandardBasicTypes.LONG, "floor(unix_timestamp(", ")/60/60) * " + IPDateUtil.HOUR_IN_MS));
        registerFunction("date_year", new OneArgSQLFunction(StandardBasicTypes.INTEGER, "YEAR(", ")"));
        registerFunction("date_month", new OneArgSQLFunction(StandardBasicTypes.INTEGER, "MONTH(", ")"));
        registerFunction("date_day", new OneArgSQLFunction(StandardBasicTypes.INTEGER, "DAYOFMONTH(", ")"));
        registerFunction("date_convert_tz", new TwoArgSQLFunction(StandardBasicTypes.TIMESTAMP, "CONVERT_TZ(", ", '" + PersistenceUtil.MySQLUtils.DEFAULT_TIMEZONE + "', ", ")"));
        registerFunction("date_convert_from_member_tz", new TwoArgSQLFunction(StandardBasicTypes.TIMESTAMP, "CONVERT_TZ(", ", ", ", '" + PersistenceUtil.MySQLUtils.DEFAULT_TIMEZONE + "')"));
        registerFunction("st_geomfromtext", new OneArgSQLFunction(ObjectType.INSTANCE, "ST_GeomFromText(", ")"));
        registerFunction("st_contains", new TwoArgSQLFunction(StandardBasicTypes.BOOLEAN, "ST_Contains(", ",", ")"));
    }
}
