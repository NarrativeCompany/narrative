package org.narrative.common.util.processes;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.StrutsStatics;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 15, 2005
 * Time: 11:20:36 AM
 */
public class ActionProcess extends RequestProcessBase implements StrutsStatics {

    private static final NarrativeLogger logger = new NarrativeLogger(ActionProcess.class);

    private static final String ACTION_PROCESS_OBJECT_KEY = "processObject";
    private OID roleOid;

    public static ActionProcess getActionProcess() {
        return getActionProcess(actionContext());
    }

    public static ActionProcess getActionProcess(ActionContext actionContext) {
        return (ActionProcess) actionContext.get(ActionProcess.ACTION_PROCESS_OBJECT_KEY);
    }

    protected final ActionInvocation invocation;
    protected String areaName = "{unknown}";
    protected String owner = "{unknown}";

    public ActionProcess(ActionInvocation ai) {
        this(ai, null);
    }

    public ActionProcess(ActionInvocation ai, Thread thread) {
        super(buildActionName(ai), thread);
        invocation = ai;
        invocation.getInvocationContext().put(ACTION_PROCESS_OBJECT_KEY, this);
    }

    private static String buildActionName(ActionInvocation ai) {
        StringBuilder name = new StringBuilder();
        if (!isEmpty(ai.getProxy().getNamespace())) {
            name.append(ai.getProxy().getNamespace()).append('/');
        }
        name.append(ai.getProxy().getActionName());
        if (!isEmpty(ai.getProxy().getMethod())) {
            name.append('!').append(ai.getProxy().getMethod());
        }
        return name.toString();
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public OID getRoleOid() {
        return roleOid;
    }

    public void setRoleOid(OID roleOid) {
        this.roleOid = roleOid;
    }

    public ActionInvocation getInvocation() {
        return invocation;
    }

    public Action getAction() {
        return (Action) invocation.getAction();
    }

    public HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) invocation.getInvocationContext().get(HTTP_REQUEST);
    }

    public HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);
    }

    @Override
    public NarrativeLogger getLogger() {
        return logger;
    }

    @Override
    public Map getParameters() {
        return invocation.getInvocationContext().getParameters();
    }

}
