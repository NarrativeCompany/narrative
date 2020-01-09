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
public class TwoArgSQLFunction extends VarArgsSQLFunction implements SQLFunction {
    private String sep;
    private String begin;
    private String end;

    public TwoArgSQLFunction(Type type, String begin, String sep, String end) {
        super(type, begin, sep, end);
        this.sep = sep;
        this.begin = begin;
        this.end = end;
    }

    public TwoArgSQLFunction(String begin, String sep, String end) {
        super(begin, sep, end);
        this.sep = sep;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) {
        if (args.size() != 2) {
            throw new IllegalArgumentException("the function must be passed 2 arguments");
        }
        return new StringBuffer(begin).append(args.get(0).toString()).append(sep).append(args.get(1).toString()).append(end).toString();
    }
}
