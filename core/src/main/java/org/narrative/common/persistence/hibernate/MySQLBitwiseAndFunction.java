package org.narrative.common.persistence.hibernate;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 23, 2006
 * Time: 4:47:21 PM
 */
public class MySQLBitwiseAndFunction extends StandardSQLFunction implements SQLFunction {

    public MySQLBitwiseAndFunction(String name) {
        super(name);
    }

    public MySQLBitwiseAndFunction(String name, Type typeValue) {
        super(name, typeValue);
    }

    @Override
    public String render(Type firstArgumentType, List args, SessionFactoryImplementor sessionFactory) {
        if (args.size() != 2) {
            throw new IllegalArgumentException("the function must be passed 2 arguments");
        }
        StringBuffer buffer = new StringBuffer(args.get(0).toString());
        buffer.append(" & ").append(args.get(1));
        return buffer.toString();
    }
}
