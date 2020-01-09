package org.narrative.common.persistence.hibernate;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 22, 2006
 * Time: 2:13:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class MySQLBitwiseOrFunction extends StandardSQLFunction implements SQLFunction {

    public MySQLBitwiseOrFunction(String name) {
        super(name);
    }

    public MySQLBitwiseOrFunction(String name, Type typeValue) {
        super(name, typeValue);
    }

    @Override
    public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) {
        if (args.size() != 2) {
            throw new IllegalArgumentException("the function must be passed 2 arguments");
        }
        StringBuffer buffer = new StringBuffer(args.get(0).toString());
        buffer.append(" | ").append(args.get(1));
        return buffer.toString();

    }
}
