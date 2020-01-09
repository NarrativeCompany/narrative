package org.narrative.common.util;

import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.common.util.processes.TraceProcessHistory;
import org.narrative.common.util.trace.TraceItem;
import org.narrative.common.util.trace.TraceManager;
import org.narrative.config.StaticConfig;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.ValidationException;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Sep 8, 2004
 * Time: 8:35:00 AM
 */
public abstract class Task<T> implements TaskInterface<T> {

    private static Map<String, String> monitorIgnoreList = null;

    private final boolean isForceWritable;
    private final AtomicReference<ValidationContext> serviceTaskValidationContextRef = new AtomicReference<>();

    protected ValidationHandler validationHandler;
    protected Consumer<ValidationContext> validationFunction;
    private boolean validated = false;
    private boolean validatedViaFunction = false;

    private GenericProcess process;

    public Task() {
        this(true);
    }

    public Task(boolean forceWritable) {
        this(forceWritable, new TaskValidationHandler());
    }

    public Task(ValidationHandler validationHandler) {
        this(true, validationHandler);
    }

    public Task(boolean forceWritable, ValidationHandler validationHandler) {
        isForceWritable = forceWritable;
        this.validationHandler = validationHandler;
    }

    public Task(boolean isForceWritable, Consumer<ValidationContext> validationFunction) {
        this.isForceWritable = isForceWritable;
        this.validationFunction = validationFunction;
    }

    static {
        initMonitorIgnoreList();
    }

    private static synchronized void initMonitorIgnoreList() {
        if (monitorIgnoreList != null) {
            return;
        }
        Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("monitorIgnoreList.properties"));
        } catch (Exception exception) {
            throw UnexpectedError.getRuntimeException("Error loading the monitor ignore list", exception, true);
        }
        // bl: use a ConcurrentHashMap so that we don't have to do synchronization on each get!
        monitorIgnoreList = newConcurrentHashMap();
        monitorIgnoreList.putAll((Hashtable) properties);
    }

    protected GenericProcess createProcess() {
        return new GenericProcess(getMonitoredClassName());
    }

    public GenericProcess getProcess() {
        return process;
    }

    protected Class getMonitoredClass() {
        return this.getClass();
    }

    protected String getMonitoredClassName() {
        // bl: for anonymous classes, let's display the $1 after the name.  thus, can't use any
        // standard method on Class.  instead, just strip everything off before the last '.' in the class name.
        return IPStringUtil.getStringAfterLastIndexOf(getMonitoredClass().getName(), ".");
    }

    protected boolean isMonitoring() {
        Class clazz = getMonitoredClass();
        // for anonymous classes, resolve the class to its enclosing class.
        // can have nested anonymous classes, so loop until we get a named,
        // non-anonymous class.
        while (clazz != null && clazz.isAnonymousClass()) {
            clazz = clazz.getEnclosingClass();
        }
        return !monitorIgnoreList.containsKey(clazz.getName());
    }

    public final T doTask() {
        boolean isMonitoring = isMonitoring();
        if (isMonitoring) {
            process = createProcess();
            ProcessManager.getInstance().pushProcess(process);
        }
        try {
            TraceItem traceItem = null;
            if (TraceManager.isTracing()) {
                traceItem = TraceManager.startTrace(new TraceProcessHistory(this.getClass().getName()));
            }

            try {
                if (validationHandler != null) {
                    validate();
                }

                if (hasValidationContext()) {
                    //Handle the case where the task has a validation context set before task execution
                    processValidationContext();
                } else {
                    // bl: let's always support validation with ValidationContext. if a custom validationFunction was
                    // not supplied, then we'll default to the Task.validate(ValidationContext) method, which can
                    // be overridden easily by subclasses.
                    if(validationFunction==null) {
                        validationFunction = Task.this::validate;
                    }
                    validateViaFunction();
                }

                return doMonitoredTask();

                // jw: rethrow the TaskValidationException dont wrap in a UnexpectedError
            } catch (TaskValidationException | ValidationException e) {
                throw e;

            } catch (Exception e) {
                throw UnexpectedError.getRuntimeException("Failed executing Task", e);

            } finally {
                if (traceItem != null) {
                    TraceManager.endTrace(traceItem);
                }
            }
        } finally {
            if (isMonitoring) {
                ProcessManager.getInstance().popProcess();
            }
        }
    }

    /**
     * does this task have to be run in the context of a writable session?
     * returns true if so.  false if not.
     * default behavior is to return true, thus if a subclass of Task wishes
     * to be runnable from a "read-only" context, that subclass will need to
     * override this method to return false or supply the isForceWritable
     * boolean in the appropriate constructor.
     *
     * @return true if this task must be run in a writable (non-read-only) context.
     * false if this task CAN be run in a read-only context.  note that this
     * does not guarantee that it will be run in a read-only context, e.g.
     * if the task is being run in a pre-existing session, the pre-existing
     * session's settings will take precedence.
     */
    public boolean isForceWritable() {
        return isForceWritable;
    }

    /**
     * Forces that the task be called without isolation.  Usefull when you put object args in a task
     * that need to be referenced during the task execution and you want to be sure that they are from
     * the same session that you are currently in.
     *
     * @return
     */
    public boolean isForceNoIsolation() {
        return false;
    }

    protected abstract T doMonitoredTask();

    private void validate() {
        if (validated) {
            return;
        }

        validate(validationHandler);

        this.validated = true;

        if (validationHandler.isThrowApplicationErrorOnValidationError() && validationHandler.hasErrors()) {
            throw new TaskValidationException(validationHandler.getValidationErrors());
        }
    }

    private void validateViaFunction() {
        if (validatedViaFunction) {
            return;
        }

        ValidationContext validationContext = getValidationContext();
        validationFunction.accept(validationContext);

        this.validatedViaFunction = true;

        processValidationContext();
    }

    private void processValidationContext() {
        ValidationContext validationContext = getValidationContext();
        if (validationContext.hasErrors()){
            throw new ValidationException("Validation errors encountered during task execution", validationContext);
        }
    }

    public void setValidationHandlerAndValidate(ValidationHandler validationHandler) {
        this.validationHandler = validationHandler;
        validate();
    }

    protected void validate(ValidationHandler validationHandler) {
    }

    public void doSetValidated(boolean validated) {
        this.validated = validated;
    }

    protected void validate(ValidationContext validationContext) {
    }

    /**
     * Make a validation context available to services
     */
    public ValidationContext getValidationContext() {
        return serviceTaskValidationContextRef.updateAndGet(value -> {
            if (value == null) {
               return StaticConfig.getBean(ValidationContext.class);
            } else {
                return value;
            }
        });
    }

    private boolean hasValidationContext(){
        return serviceTaskValidationContextRef.get() != null;
    }

    public void setValidationFunction(Consumer<ValidationContext> validationFunction) {
        this.validationFunction = validationFunction;
    }
}
