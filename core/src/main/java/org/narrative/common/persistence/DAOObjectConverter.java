package org.narrative.common.persistence;

import ognl.DefaultTypeConverter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jan 5, 2006
 * Time: 5:51:58 PM
 */
public class DAOObjectConverter extends DefaultTypeConverter {
    @Nullable
    public Object convertValue(Map map, Object object, Class toType) {

        if (toType == String.class) {
            if (object instanceof DAOObject) {
                OID oid = ((DAOObject) object).getOid();
                if (oid != null) {
                    return oid.toString();
                }
            } else {
                return null;
            }
        } else {
            String strVal;
            if (object instanceof String[]) {
                strVal = ((String[]) object)[0];
            } else {
                strVal = object.toString();
            }
            OID oid = OID.getOIDFromString(strVal);
            //make sure we got an oid for the object
            if (oid == null) {
                return null;
            }

            // bl: special hack to exclude the DUMMY_OID if it's ever used
            if (oid.equals(OID.DUMMY_OID)) {
                return null;
            }

            //make sure we got a dao object class
            if (!DAOObject.class.isAssignableFrom(toType)) {
                return null;
            }

            //get the network dao impl for the class
            DAO dao = DAOImpl.getDAOIncludingDescendents(toType);
            if (dao == null) {
                return null;
            }

            // todo: register the objects that we get somewhere so that we can catch
            // LazyInitializationExceptions and ObjectNotFoundExceptions to determine
            // which parameter was invalid?  similar to what we did in ParameterParser2
            // in GC 1.2
            //get the class
            return dao.get(oid);
        }

        return null;
    }
}