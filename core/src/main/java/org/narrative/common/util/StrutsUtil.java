package org.narrative.common.util;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.RuntimeConfiguration;
import org.apache.struts2.dispatcher.Dispatcher;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: Dec 22, 2005
 * Time: 11:09:19 PM
 *
 * @author Brian
 */
public class StrutsUtil {

    private static final NarrativeLogger logger = new NarrativeLogger(StrutsUtil.class);

    /**
     * get a parameter from the ActionContext's parameters
     *
     * @param parameterName the name of the request parameter to get
     * @return the request parameter from the value stack
     */
    public static String getParameter(ActionContext actionContext, String parameterName) {
        Map parameters = actionContext.getParameters();
        return getParameterValue(parameters.get(parameterName));
    }

    public static String getParameterValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String[]) {
            String[] vals = (String[]) value;
            if (vals.length == 0) {
                return null;
            }
            return vals[0];
        }
        return value.toString();
    }

    private static final String PROPERTY_SEPARATOR = ".";
    private static final Pattern MAP_PATTERN = Pattern.compile("\\[[\\\"\\']?[\\w \\.\\-\\_]*[\\\"\\']?\\]");

    /**
     * determine if a given parameter name contains a parameter separator.
     * parameter separators are either dots "." or open brackets "["
     *
     * @param paramName the param name to check
     * @return true if the parameter contains a parameter separator.  false if it does not.
     */
    public static boolean doesParameterContainPropertySeparator(String paramName) {
        return paramName.contains(PROPERTY_SEPARATOR);
    }

    /**
     * given a parameter name, figure out what its "base" parameter name is.
     * searches for the string that appears before a dot "." or an open bracket "[".
     * e.g.
     * "param" -> "param"
     * "param[0]" -> "param"
     * "param.name" -> "param"
     * "param[0].name" -> "param"
     *
     * @param paramName the parameter to get the base parameter name for
     * @return the base parameter name for this parameter.  if this parameter does not
     * contain a "." or a "[", then the paramName argument is returned unchanged.
     */
    public static String getBaseParameterName(String paramName) {
        // now, we only care about the part of the parameter name prior to the dot.
        // if there is no dot, then it is not a post prepare param.
        // also need to check for open brackets in the event that an item
        // is being looked up in a map/collection/array/etc.
        List<String> propertyList = getSubPropertyListForParameterName(paramName);
        return (propertyList == null || propertyList.isEmpty()) ? paramName : propertyList.get(0);
    }

    /**
     * bl: this is a primitive ognl parser to split up parameters for a request.
     *
     * @param paramName the param name to check
     * @return a List of the properties being accessed in the specified ognl expression.
     */
    public static List<String> getSubPropertyListForParameterName(String paramName) {
        List<String> ret = new LinkedList<String>();
        paramName = stripOutCollectionMapNames(paramName);
        StringTokenizer st = new StringTokenizer(paramName, PROPERTY_SEPARATOR);
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            ret.add(token);
        }
        return ret;
    }

    // jw: if the map key contains a '.' in it the above simple parser will fail, because of that lets try and be more
    //     intelligent about how we strip those out when determining the property names.
    // example: imageTypeToImageFields['icon.18.notBlockingMember'].remove will become imageTypeToImageFields.remove
    private static String stripOutCollectionMapNames(String paramName) {
        Matcher matcher = MAP_PATTERN.matcher(paramName);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result, "");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static String getDefaultActionNameForNamespace(String namespace) {
        // bl: if using the default action (empty string action name ""), we can't readily determine the default
        // action name to use.  so, i'm using some fancy reflection to dig into the bowels of Struts/XWork
        // in order to extract the default action name for a given namespace.
        RuntimeConfiguration runtimeConfiguration = Dispatcher.getInstance().getConfigurationManager().getConfiguration().getRuntimeConfiguration();
        try {
            Field namespaceConfigsField = runtimeConfiguration.getClass().getDeclaredField("namespaceConfigs");
            namespaceConfigsField.setAccessible(true);
            Map<String, String> namespaceConfigs = (Map<String, String>) namespaceConfigsField.get(runtimeConfiguration);
            return namespaceConfigs.get(namespace);
        } catch (Throwable t) {
            logger.error("Failed getting RuntimeConfiguration for default action name!", t);
            return null;
        }
    }
}
