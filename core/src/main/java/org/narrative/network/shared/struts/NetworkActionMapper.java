package org.narrative.network.shared.struts;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.shared.context.NetworkContextInternal;
import org.narrative.network.shared.context.RequestType;

import javax.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 8, 2005
 * Time: 11:19:17 PM
 */
public class NetworkActionMapper implements ActionMapper {

    public static final String INVALID_ACTION_CONTEXT_PARAM = NetworkActionMapper.class.getName() + "-InvalidActionName";
    public static final String DEFAULT_PARAMETERS_PARAM = NetworkActionMapper.class.getName() + "-DefaultParameterParam";

    private static final Map<String, ObjectPair<Boolean, Integer>> ACTION_NAME_TO_IS_SUPPORTS_DEFAULT_PARAMS = newConcurrentHashMap();

    private static final String CLUSTER_CP_NAMESPACE_BASE = "/cluster-cp";
    private static final String KYC_QUEUE_NAMESPACE_BASE = "/kyc-queue";

    private static final Map<String, String> INTERNAL_CONTENT_ACTION_MAPPINGS;

    static {
        Map<String, String> contentActionMappings = newHashMap();

        INTERNAL_CONTENT_ACTION_MAPPINGS = Collections.unmodifiableMap(contentActionMappings);
    }

    public static void init(ConfigurationManager configurationManager) {
        Map<String, Map<String, ActionConfig>> actionConfigs = getActionConfigs(configurationManager);
        // bl: this is needed so that IPHTMLUtil knows how to generate URLs with path parameters correctly.
        for (String namespace : actionConfigs.keySet()) {
            // /master/personal
            namespace = IPStringUtil.getStringAfterStripFromStart(namespace, "/");
            // master/personal
            int slashIndex = namespace.indexOf("/");
            if (slashIndex > 0) {
                namespace = namespace.substring(slashIndex + 1);
                // /personal
                IPHTMLUtil.registerSubNamespace(namespace);
            }
        }
    }

    @Override
    public ActionMapping getMappingFromActionName(String actionName) {
        throw UnexpectedError.getRuntimeException("Can't call getMapingFromActionName! Don't support it. How did this happen?", true);
    }

    @Override
    public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configurationManager) {
        // bl: first look the mapping up by the base namespace
        ActionMapping mapping = setupActionMappingForRequest(request, getBaseNamespace(), configurationManager);

        if (!isEmpty(mapping.getName())) {
            String actionName = mapping.getName();

            if (getActionConfigs(configurationManager).get(mapping.getNamespace()).get(actionName) == null) {
                // bl: ignore the dummy action "x" since we use that in programmatically generated URLs sometimes.
                if (!isEqual(IPHTMLUtil.DUMMY_ACTION, actionName)) {
                    // no action config?  then we'd be using the default action.  probably not intended and
                    // will result in bugs, so on dev an QA servers, raise an error here.
                    ((NetworkContextInternal) networkContext()).setContextData(INVALID_ACTION_CONTEXT_PARAM, "Supplied an invalid action (" + mapping.getName() + ") for namespace " + mapping.getNamespace() + ".  Probably indicates an out-of-date action mapping or an incorrect URL.");
                }
            }
        }

        // bl: changing the default method to be input!
        if (IPStringUtil.isEmpty(mapping.getMethod())) {
            // bl: also support method values explicitly specified in the XML.
            Map<String, Map<String, ActionConfig>> actionConfigs = getActionConfigs(configurationManager);
            if (actionConfigs.containsKey(mapping.getNamespace())) {
                ActionConfig actionConfig = actionConfigs.get(mapping.getNamespace()).get(mapping.getName());
                if (actionConfig != null) {
                    mapping.setMethod(actionConfig.getMethodName());
                }
            }
            if (IPStringUtil.isEmpty(mapping.getMethod())) {
                mapping.setMethod(Action.INPUT);
            }
        }

        return mapping;
    }

    private ActionMapping setupActionMappingForRequest(HttpServletRequest request, String baseNamespace, ConfigurationManager configurationManager) {
        // bl: intentionally using new ActionMapping on each pass since we will be re-processing the request
        ActionMapping mapping = new ActionMapping();
        final Map<String, Object> pathParamsForContext = new HashMap<>();

        mapping.setNamespace(baseNamespace);

        String path = request.getRequestURI();   //todo:If we ever use a non root context, the context path will need to be removed

        // only going to look for the namespace candidate and action if the path is specified.
        String[] tokens = null;
        int curToken = 0;
        if (path != null) {
            // look for the first item in the url
            if (isEmpty(path)) {
                tokens = new String[]{};
            } else {
                tokens = path.substring(1).split("/");
            }

            if (tokens.length > 0) {
                // jw: some content actions are exposed through the same action name as the list action, through the
                //     prettyUrlString after the action name.  To support this we need to check the first parameter to
                //     see if its one of these actions, and then if it is the second parameter to see if its a prettyUrl
                //     content of the appropriate type.  This code handles that!
                if (tokens.length > 1) {
                    // bl: need to also support action names for these.
                    String actionName = tokens[0];
                    ObjectPair<String, String> actionAndMethod = getActionAndMethod(actionName);
                    if (actionAndMethod != null) {
                        actionName = actionAndMethod.getOne();
                    }
                    String newAction = INTERNAL_CONTENT_ACTION_MAPPINGS.get(actionName);
                    if (!isEmpty(newAction)) {
                        // bl: if there is a method defined, then we need to preserve it
                        if (actionAndMethod != null) {
                            tokens[0] = newAction + "!" + actionAndMethod.getTwo();
                        } else {
                            tokens[0] = newAction;
                        }
                    }
                }

                {
                    Map<String, Map<String, ActionConfig>> actionConfigs = getActionConfigs(configurationManager);

                    String namespaceCandidate = tokens[curToken++];
                    // bl: in order to properly support sub-namespaces, we must set the
                    // namespace to the master namespace for namespace lookup purposes
                    final boolean foundSubNamespace = actionConfigs.containsKey(getNamespace(mapping.getNamespace(), namespaceCandidate));
                    if (foundSubNamespace) {
                        while (actionConfigs.containsKey(getNamespace(mapping.getNamespace(), namespaceCandidate))) {
                            mapping.setNamespace(getNamespace(mapping.getNamespace(), namespaceCandidate));
                            if (tokens.length <= curToken) {
                                // no action to use - just a namespace being used
                                mapping.setName(null);
                                break;
                            }
                            // set the action equal to the namespace candidate each time.  that way, once the namespace
                            // no longer matches, the action will already be set properly.
                            mapping.setName(namespaceCandidate = tokens[curToken++]);
                        }
                    }

                    if (!foundSubNamespace) {
                        // if no sub-namespace is found, then just use the current namespace and set the action to use
                        mapping.setName(namespaceCandidate);
                    }
                }
            }
        }

        // make sure the action isn't null
        if (isEmpty(mapping.getName())) {
            // the empty string here will lead to the default-action-ref supplied in the xwork.xml being used.
            mapping.setName("");
        } else {
            // check if the action has a method specified in it
            ObjectPair<String, String> actionAndMethod = getActionAndMethod(mapping.getName());
            if (actionAndMethod != null) {
                mapping.setName(actionAndMethod.getOne());
                mapping.setMethod(actionAndMethod.getTwo());
            }
        }

        if (path != null) {
            // bl: altered to support multiple parameters with the same name
            Map<String, Collection<String>> pathParams = new HashMap<String, Collection<String>>();
            //if there are some items left on the path, then parse the params

            if (!isEmpty(mapping.getName())) {
                Map<String, ActionConfig> namespaceMappings = getActionConfigs(configurationManager).get(mapping.getNamespace());
                if (namespaceMappings != null) {
                    ActionConfig actionConfig = namespaceMappings.get(mapping.getName());
                    if (actionConfig != null) {
                        String className = actionConfig.getClassName();
                        boolean supportPathAfterDefaultParameters = false;
                        int defaultParamCount = 0;
                        if (!ACTION_NAME_TO_IS_SUPPORTS_DEFAULT_PARAMS.containsKey(className)) {
                            ACTION_NAME_TO_IS_SUPPORTS_DEFAULT_PARAMS.put(className, newObjectPair(supportPathAfterDefaultParameters, defaultParamCount));
                        } else {
                            ObjectPair<Boolean, Integer> cache = ACTION_NAME_TO_IS_SUPPORTS_DEFAULT_PARAMS.get(className);
                            supportPathAfterDefaultParameters = cache.getOne();
                            defaultParamCount = cache.getTwo();
                        }
                        if (defaultParamCount > 0) {
                            // jw: OK, lets only process the default parameters if there are not more remaining tokens
                            //     than default parameters. We should never provide path parameters with default params
                            //     so likely if there are more tokens than default params then likely this is a legacy
                            //     URL that was specifying path parameters.  Ex: /displayForum/forum/123 -> /forum/forum/123 -> /forum/123
                            //     by not processing these as default params it will allow it to be processed as path params.
                            int remainingTokens = tokens.length - curToken;
                            if (defaultParamCount >= remainingTokens || supportPathAfterDefaultParameters) {
                                List<String> defaultParameters = newLinkedList();
                                for (int i = curToken; i < tokens.length && defaultParameters.size() < defaultParamCount; i++) {
                                    defaultParameters.add(tokens[i]);
                                    // bl: skip this path element since we are now consuming it as a default parameter
                                    // bl: instead of just doing this all of the time (and potentially breaking backward compatibility
                                    // of some URLs that formerly did not use default parameters, but now do), i'm
                                    // going to make this specific to the UBB.threads redirect action for now (which must have this behavior).
                                    // jw: We are no longer worried about those legacy URLs Brian mentioned in the above comment.
                                    //     its been long enough now that default parameters should almost never be referenced
                                    //     using path parameters any longer.
                                    /*if(UbbThreadsLegacyRedirectAction.ACTION_NAME.equals(mapping.getName())) {
                                        curToken++;
                                    }*/
                                    curToken++;
                                }
                                ((NetworkContextInternal) networkContext()).setContextData(DEFAULT_PARAMETERS_PARAM, defaultParameters);
                            }
                        }
                    }
                }
            }
            //finally add items in the path as name value pairs to the param map
            while (tokens.length > curToken) {
                // bl: in general, parameter names aren't encoded
                String name = tokens[curToken];

                if (tokens.length > curToken + 1) {
                    // parameter values definitely can be encoded (e.g. redirect urls)
                    String value = IPHTMLUtil.getURLDecodedString(tokens[curToken + 1]);
                    Collection<String> values = pathParams.get(name);
                    if (values == null) {
                        pathParams.put(name, values = new LinkedList<String>());
                    }
                    values.add(value);
                }
                curToken += 2;
            }

            for (Map.Entry<String, Collection<String>> entry : pathParams.entrySet()) {
                pathParamsForContext.put(entry.getKey(), entry.getValue().toArray(new String[]{}));
            }
        }

        if (!pathParamsForContext.isEmpty()) {
            // bl: Struts 2 no longer automatically includes all ActionMapping params in the context.
            // so, we should include them directly in the ActionContext parameters here.
            ActionContext.getContext().getParameters().putAll(pathParamsForContext);
        }

        return mapping;
    }

    private static ObjectPair<String, String> getActionAndMethod(String action) {
        int methPos = action.indexOf("!");
        int skipChars = 1;
        // bl: if there is no "!", check for an encoded "!" which is "%21".  some browsers seem to be encoding it unnecessarily.
        if (methPos < 0) {
            methPos = action.indexOf("%21");
            skipChars = 3;
        }
        if (methPos > -1) {
            return new ObjectPair<String, String>(action.substring(0, methPos), action.substring(methPos + skipChars));
        }
        // bl: null indicates there is no method to use
        return null;
    }

    private static String getBaseNamespace() {
        RequestType requestType = networkContext().getRequestType();

        return getNamespaceBaseForRequestType(requestType);
    }

    private static String getNamespaceBaseForRequestType(RequestType requestType) {
        switch (requestType) {
            case CLUSTER_CP:
                return CLUSTER_CP_NAMESPACE_BASE;
            case KYC_QUEUE:
                return KYC_QUEUE_NAMESPACE_BASE;
            default:
                assert false : "Found an unsupported request type! Shouldn't be possible.";
                return null;
        }
    }

    public String getUriFromActionMapping(ActionMapping mapping) {

        // bl: this method is only used in 3 places:
        // 1. getting the uri to use for forms (ww:form)
        // 2. ServletActionRedirectResult (result="redirect-action")
        // 3. getting the uri to use for the ww:url tags
        // since we aren't using any of those anymore, I'm changing this implemention to just throw an Exception in the
        // event of an error
        throw UnexpectedError.getRuntimeException("Can't call getUriFromActionMapping anymore. We shouldn't use redirect-action result type, ww:form, or ww:url.", true);
    }

    private static String getNamespace(String baseNamespace, String subNamespace) {
        return baseNamespace + "/" + subNamespace;
    }

    private static Map<String, Map<String, ActionConfig>> getActionConfigs(ConfigurationManager configurationManager) {
        return configurationManager.getConfiguration().getRuntimeConfiguration().getActionConfigs();
    }
}
