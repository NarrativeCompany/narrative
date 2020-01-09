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
public class CastAsSignedFunction extends StandardSQLFunction implements SQLFunction {

    public CastAsSignedFunction(String name, Type typeValue) {
        super(name, typeValue);
    }

    @Override
    public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) {
        if (args.size() != 1) {
            throw new IllegalArgumentException("the function must be passed 1 arguments");
        }
        StringBuilder sb = new StringBuilder("cast((");
        sb.append(args.get(0).toString());
        sb.append(") as signed)");
        return sb.toString();

    }
}
