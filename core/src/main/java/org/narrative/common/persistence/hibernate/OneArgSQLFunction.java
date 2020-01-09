package org.narrative.common.persistence.hibernate;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 24, 2006
 * Time: 11:32:51 AM
 */
public class OneArgSQLFunction extends VarArgsSQLFunction implements SQLFunction {
    private String begin;
    private String end;

    public OneArgSQLFunction(Type type, String begin, String end) {
        super(type, begin, "", end);
        this.begin = begin;
        this.end = end;
    }

    public OneArgSQLFunction(String begin, String end) {
        super(begin, "", end);
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) {
        if (args.size() != 1) {
            throw new IllegalArgumentException("the function must be passed 1 argument");
        }
        return new StringBuffer(begin).append(args.get(0).toString()).append(end).toString();
    }
}