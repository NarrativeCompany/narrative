package org.narrative.network.shared.interceptors;

import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.common.core.services.interceptors.SubPropertySettable;
import org.narrative.common.util.IPBeanUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.StrutsUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.common.web.struts.AfterPrepare;
import org.narrative.network.shared.baseactions.NetworkAction;
import com.opensymphony.xwork2.ActionContext;

import javax.persistence.Id;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Feb 8, 2006
 * Time: 9:48:49 AM
 *
 * @author Brian
 */
public class NetworkPreParametersInterceptor extends NetworkBaseParametersInterceptor {

    private static final String POST_PARAMETERS_CONTEXT_KEY = NetworkPreParametersInterceptor.class.getName() + ".PostParametersMap";
    private static final Map<Class<?>, SubPropertySettableDetails> s_actionClassToSubPropertyDetails = new ConcurrentHashMap<Class<?>, SubPropertySettableDetails>();

    @Override
    protected Map<String, Object> retrieveParameters(ActionContext context) {
        Object action = context.getActionInvocation().getAction();
        Class<?> actionClass = action.getClass();

        if (action instanceof PreParametersPreparable) {
            ((PreParametersPreparable) action).preParametersPrepare();
        }

        // make sure we've read all of the annotations off of this action already.
        initActionClassDefinitionsIfNecessary(actionClass);

        final Map<String, Object> actualParameters = context.getParameters();

        // split up the parameters into two separate maps:
        // 1) parameters to set prior to prepare()
        // 2) parameters to set after calling prepare()
        Map<String, Object> prePrepareParamMap = newLinkedHashMap();
        Map<String, Object> postPrepareParamMap = newLinkedHashMap();

        {
            SubPropertySettableDetails subPropertyDetails = s_actionClassToSubPropertyDetails.get(actionClass);

            for (Map.Entry<String, ?> entry : actualParameters.entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();
                ParamDetails paramDetails = getPropertyDetails(paramName, subPropertyDetails);
                // make sure the sub property should be set
                if (paramDetails.isValid) {
                    if (paramValue != null) {
                        if (paramValue instanceof String[]) {
                            // bl: edit the parameters inline in the String[].  this will make the parameters
                            // take effect in the actual parameters map, too, in case we're accessing
                            // parameters from the ActionContext parameters map anywhere in our code.
                            String[] paramValues = (String[]) paramValue;
                            for (int i = 0; i < paramValues.length; i++) {
                                // is getSafeHTMLString overkill?  use HtmlTextMassager.disableHtml() instead?
                                // bl: yes it is.  simply disabling the HTML is sufficient.
                                //paramValues[i] = IPHTMLUtil.getSafeHTMLString(paramValues[i]);
                                paramValues[i] = formatStringParam(paramName, paramValues[i], paramDetails);
                            }
                        } else if (paramValue instanceof String) {
                            paramValue = formatStringParam(paramName, (String) paramValue, paramDetails);
                        } else if (paramDetails.isDisableHtml) {
                            // bl: note that there is a possibility of the ActionContext parameters map containing values
                            // that are not strings.  for instance, if this is a file upload process, it can contain
                            // an array of File objects from the FileUploadInterceptor.  note, however, that we will
                            // only ever get to this code if the parameter is for a String setter.
                            throw UnexpectedError.getRuntimeException("Found an unrecognized parameter type for a string setter! param/" + paramValue + " type/" + paramValue.getClass() + " className/" + paramValue.getClass().getName(), true);
                        }
                    }
                    // populate the post prepare map if this action is preparable and this is a post prepare param.
                    // bl: changed so that we'll always honor the post prepare parameter.  this is because
                    // certain other interceptors are executed in between the pre/post parameters interceptors
                    // (e.g. CompositionActionInterceptor) and we shouldn't require actions implement Preparable
                    // just to take advantage of that functionality.
                    if (paramDetails.isPostPrepare) {
                        postPrepareParamMap.put(paramName, paramValue);
                    } else {
                        prePrepareParamMap.put(paramName, paramValue);
                    }
                }
            }
        }

        // add the post prepare param map if it has any parameters
        if (!postPrepareParamMap.isEmpty()) {
            context.put(POST_PARAMETERS_CONTEXT_KEY, postPrepareParamMap);
        }

        if (!prePrepareParamMap.isEmpty()) {
            return prePrepareParamMap;
        }
        return Collections.emptyMap();
    }

    private String formatStringParam(String paramName, String paramValue, ParamDetails paramDetails) {
        if (paramName.equals(NetworkAction.REDIRECT_PARAM) && !networkContext().getRequestType().isRedirectUrlAllowed(paramValue)) {
            return "";
        } else {
            if (paramDetails.isDisableHtml) {
                return HtmlTextMassager.disableAndTrimHtml(paramValue);
            } else {
                return IPStringUtil.removeZeroWidthSpaces(paramValue);
            }
        }
    }

    /**
     * this is a primitive ognl parser to make sure that certain properties are settable
     *
     * @param paramName          the parameter name to test
     * @param subPropertyDetails the map of sub property settings to determine if this param name should be set
     * @return true if the param name should be set for this request.  false if it should not.
     */
    private static ParamDetails getPropertyDetails(String paramName, SubPropertySettableDetails subPropertyDetails) {
        List<String> propertyNamesBeingAccessed = StrutsUtil.getSubPropertyListForParameterName(paramName);
        SubPropertySettableDetails currentSubPropertyDetails = subPropertyDetails;
        boolean isValid = true;
        boolean isPostPrepare = false;
        Boolean isDisableHtml = null;
        // walk through the properties in the property list.  ensure that each property we find
        // is found in the map of properties.
        for (int i = 0; i < propertyNamesBeingAccessed.size(); i++) {
            String propertyName = propertyNamesBeingAccessed.get(i);

            // if we find a post-prepare parameter at any point in the tree, then this should be a post-prepare parameter.
            if (currentSubPropertyDetails.postPrepareParams.contains(propertyName)) {
                isPostPrepare = true;
            }

            // if, at any level, we find a read only property, then this isn't a valid
            // parameter, so break out.
            if (currentSubPropertyDetails.readOnlyProperties.contains(propertyName)) {
                isValid = false;
                break;
            }

            boolean isLastProperty = i == (propertyNamesBeingAccessed.size() - 1);

            // bl: only check for the disable html flag on the final property in the hierarchy.
            if (isLastProperty) {
                isDisableHtml = currentSubPropertyDetails.stringPropertiesToDisableHtml.get(propertyName);
            }

            SubPropertySettableDetails subPropertySettableDetails = currentSubPropertyDetails.subPropertySettableDetails.get(propertyName);
            // didn't find the property in the map?  then this property is not valid.
            if (subPropertySettableDetails == null && !isLastProperty) {
                // no sub-property details for this property?  then this is not a valid parameter.
                isValid = false;
                break;
            }
            // ok, we found it, so now we need to update the map to search in
            currentSubPropertyDetails = subPropertySettableDetails;
        }

        // got through?  then it is valid.
        return new ParamDetails(isValid, isPostPrepare, isDisableHtml != null && isDisableHtml);
    }

    private static class ParamDetails {
        private final boolean isValid;
        private final boolean isPostPrepare;
        private final boolean isDisableHtml;

        public ParamDetails(boolean valid, boolean postPrepare, boolean disableHtml) {
            isValid = valid;
            isPostPrepare = postPrepare;
            isDisableHtml = disableHtml;
        }
    }

    private static void initActionClassDefinitionsIfNecessary(Class<?> actionClass) {
        // already initialized?
        if (s_actionClassToSubPropertyDetails.containsKey(actionClass)) {
            return;
        }

        synchronized (actionClass) {
            // already been set?
            if (s_actionClassToSubPropertyDetails.containsKey(actionClass)) {
                return;
            }

            createActionClassDefinitions(actionClass);
        }
    }

    private static void createActionClassDefinitions(Class<?> actionClass) {
        SubPropertySettableDetails subPropertyDetails = new SubPropertySettableDetails();
        populateSettableSubProperties(actionClass, subPropertyDetails, -1, null);
        // set the settable sub-property parameter prefixes first.  we'll check the
        // post prepare param prefixes to determine if initialization has completed.
        s_actionClassToSubPropertyDetails.put(actionClass, subPropertyDetails);
    }

    private static void populateSettableSubProperties(Class<?> clzz, SubPropertySettableDetails currentSubPropertySettableDetails, final int remainingDepthFromParent, Class<?> onlyMethodsBelongingToSubclassesOfThisClass) {
        for (Method method : clzz.getMethods()) {
            // make sure this method is a getter.  must have:
            // 1) no args
            // 2) a return type (not void)
            // 3) not be static
            // 4) not a bridge method
            // 5) not a synthetic method
            // for more info on #4 and #5, see:
            // http://www.angelikalanger.com/GenericsFAQ/FAQSections/TechnicalDetails.html#What%20is%20a%20bridge%20method?
            if (Modifier.isStatic(method.getModifiers()) || method.isBridge() || method.isSynthetic()) {
                continue;
            }

            if (onlyMethodsBelongingToSubclassesOfThisClass != null) {
                if (method.getDeclaringClass().isAssignableFrom(onlyMethodsBelongingToSubclassesOfThisClass)) {
                    // bl: skip methods for classes that should have already been covered.
                    continue;
                }
            }

            if (IPBeanUtil.isGetter(method)) {
                // this method is a getter

                SubPropertySettable subPropertySettableAnnotation = method.getAnnotation(SubPropertySettable.class);
                AfterPrepare afterPrepareAnnotation = method.getAnnotation(AfterPrepare.class);
                Id idAnnotation = method.getAnnotation(Id.class);
                String propertyName = IPBeanUtil.getPropertyNameFromGetter(method);
                if (IPStringUtil.isEmpty(propertyName)) {
                    continue;
                }

                if (idAnnotation != null) {
                    currentSubPropertySettableDetails.readOnlyProperties.add(propertyName);
                }

                if (afterPrepareAnnotation != null) {
                    currentSubPropertySettableDetails.postPrepareParams.add(propertyName);
                }

                Class<?> returnType = extractReturnType(clzz, method);

                if (subPropertySettableAnnotation != null) {
                    int depthForThisAnnotation = subPropertySettableAnnotation.depth();
                    assert depthForThisAnnotation > 0 : "Must specify a depth greater than zero for any method annotated with SubPropertySettable! method/" + method;

                    int remainingDepth;
                    if (remainingDepthFromParent < 0) {
                        // for the first iteration, the remaining depth from the parent
                        remainingDepth = depthForThisAnnotation;
                    } else {
                        // take the smaller of the remaining depth minus 1 (based on the parent) and the depth for this annotation.
                        remainingDepth = Math.min(remainingDepthFromParent - 1, depthForThisAnnotation);
                    }

                    // no further recursion allowed based on the parent?  then
                    // just continue on to the next method to check.
                    if (remainingDepth <= 0) {
                        continue;
                    }

                    SubPropertySettableDetails details = new SubPropertySettableDetails();
                    currentSubPropertySettableDetails.subPropertySettableDetails.put(propertyName, details);

                    if (returnType == null) {
                        continue;
                    }

                    // bl: need to support disabling HTML in:
                    // String[]
                    // Collection<String>
                    // Map<?,String>
                    // luckily, the returnType here accounts  for each of these scenarios 
                    if (String.class.equals(returnType)) {
                        setDisableHtmlForField(currentSubPropertySettableDetails, propertyName, clzz, method);
                    }

                    // bl: removing this restriction.  need to be able to use sub property settable on maps sometimes!
                    //assert !returnType.getName().startsWith("java") : "Shouldn't set the @SubPropertySettable annotation on properties whose class is part of the JDK! method/" + method;

                    // recursively call this method to populate the SubPropertySettableDetails nested for this property.
                    populateSettableSubProperties(returnType, details, remainingDepth, null);

                    Class[] alternateSubclasses = subPropertySettableAnnotation.subclasses();
                    if (alternateSubclasses != null) {
                        for (Class alternateSubclass : alternateSubclasses) {
                            // bl: just the declared 
                            populateSettableSubProperties(alternateSubclass, details, remainingDepth, returnType);
                        }
                    }
                } else if (String.class.equals(returnType)) {
                    Class<?> rawReturnType = IPBeanUtil.extractTypeClass(clzz, method.getGenericReturnType(), method.getName());
                    // bl: we still have to handle the scenario where a Collection or Map has a value type of String, in which
                    // case we need to make sure that we properly disable HTML for those parameters!
                    if (Collection.class.isAssignableFrom(rawReturnType) || Map.class.isAssignableFrom(rawReturnType)) {
                        setDisableHtmlForField(currentSubPropertySettableDetails, propertyName, clzz, method);
                    }
                }
            } else if (IPBeanUtil.isSetter(method)) {
                // this method is a setter

                // bl: need to check for setters for:
                // String
                // String[]
                // Collection<String>
                // Map<?,String>

                Class<?> cls = extractType(clzz, method.getGenericParameterTypes()[0], method.getName());
                if (cls.equals(String.class)) {
                    String propertyName = IPBeanUtil.getPropertyNameFromSetter(method);
                    if (IPStringUtil.isEmpty(propertyName)) {
                        continue;
                    }

                    setDisableHtmlForField(currentSubPropertySettableDetails, propertyName, clzz, method);
                }
            }
        }
    }

    private static void setDisableHtmlForField(SubPropertySettableDetails currentSubPropertySettableDetails, String propertyName, Class<?> clzz, Method method) {
        // bl: if found the BypassHtmlDisable annotation, then set it in the map, regardless of what
        // the current value in the map is.  if it doesn't exist, then we need to insert the disable html
        // true flag in the map.  otherwise, just leave it at whatever it is at.  the reason we need
        // to do this is that a single field may have both a getter and a setter and the BypassHtmlDisable
        // annotation may exist on either.  if it exists on either, then we want to force it to be disabled
        // regardless of whether it is on both the getter and the setter or not.
        // note: the reason we have to test the setter for Strings is that for some Maps, Collections,
        // and arrays, we may not have a setter.  thus, the only place that the BypassHtmlDisable
        // annotation could exist is on the getter.

        // bl: special handling for file fields.  don't ever want to disable html in them.  see FileUploadInterceptor.
        boolean forceBypassHtmlDisableForFileField = false;
        if (propertyName.endsWith("ContentType") || propertyName.endsWith("FileName")) {
            StringBuilder setterName = new StringBuilder("set");
            String fileFieldName;
            if (propertyName.endsWith("ContentType")) {
                fileFieldName = IPStringUtil.getStringAfterStripFromEnd(propertyName, "ContentType");
            } else {
                fileFieldName = IPStringUtil.getStringAfterStripFromEnd(propertyName, "FileName");
            }
            setterName.append(fileFieldName.substring(0, 1).toUpperCase());
            setterName.append(fileFieldName.substring(1));
            try {
                Method setterMethod = clzz.getMethod(setterName.toString(), File.class);
                // bl: only force bypass html disable if this is a valid setter.
                // must be:
                // - public
                // - non-static
                // - non-bridge
                // - non-synthetic
                // - void return type
                forceBypassHtmlDisableForFileField = Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !method.isBridge() && !method.isSynthetic() && void.class.equals(setterMethod.getReturnType());
            } catch (NoSuchMethodException e) {
                // ignore.  no method, so don't force bypass html disable
            }
        }
        BypassHtmlDisable bypassHtmlDisableAnnotation = method.getAnnotation(BypassHtmlDisable.class);
        if (bypassHtmlDisableAnnotation != null || forceBypassHtmlDisableForFileField) {
            currentSubPropertySettableDetails.stringPropertiesToDisableHtml.put(propertyName, Boolean.FALSE);
        } else if (!currentSubPropertySettableDetails.stringPropertiesToDisableHtml.containsKey(propertyName)) {
            currentSubPropertySettableDetails.stringPropertiesToDisableHtml.put(propertyName, Boolean.TRUE);
        }
    }

    private static Class<?> extractReturnType(Class<?> rootEntity, Method method) {
        return extractType(rootEntity, method.getGenericReturnType(), method.getName());
    }

    private static Class<?> extractType(Class<?> rootEntity, Type t, String debugName) {
        Class<?> returnType = IPBeanUtil.extractTypeClass(rootEntity, t, debugName);
        // for arrays, return the "componentType", which is the class of the
        // object itself.
        if (returnType.isArray()) {
            return returnType.getComponentType();
        }

        // if this is a Map or a Collection, then extract the type of that collection.
        if (Collection.class.isAssignableFrom(returnType)) {
            Class collectionClass = IPBeanUtil.extractCollectionType(rootEntity, t);
            if (collectionClass != null) {
                return collectionClass;
            }
        }

        if (Map.class.isAssignableFrom(returnType)) {
            Class collectionClass = IPBeanUtil.extractMapValueType(rootEntity, t);
            if (collectionClass != null) {
                return collectionClass;
            }
        }

        return returnType;
    }

    /**
     * the purpose of this class is just to wrap a recursive map of properties.  the keys in the map
     * actually contain the data that we care about.
     */
    private static class SubPropertySettableDetails {
        private final Map<String, SubPropertySettableDetails> subPropertySettableDetails = new HashMap<String, SubPropertySettableDetails>();
        private final Collection<String> postPrepareParams = new HashSet<String>();
        private final Collection<String> readOnlyProperties = new HashSet<String>();
        private final Map<String, Boolean> stringPropertiesToDisableHtml = new HashMap<String, Boolean>();
    }

    public static Map<String, Object> getPostParametersMap(ActionContext context) {
        return (Map<String, Object>) context.get(POST_PARAMETERS_CONTEXT_KEY);
    }
}
