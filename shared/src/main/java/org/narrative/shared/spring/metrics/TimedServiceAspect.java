package org.narrative.shared.spring.metrics;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * A clone of {@link TimedAspect} that respects a single class level annotation to time all public methods in the
 * annotated class.
 */
@Aspect
public class TimedServiceAspect {
    private final MeterRegistry registry;

    public TimedServiceAspect(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * All methods in a class annotated with {@link TimedService} that do not have {@link TimedService} annotations at
     * the method level.
     */
    @Pointcut("within(@org.narrative.shared.spring.metrics.TimedService *) && execution(public * *(..)) && !execution(@org.narrative.shared.spring.metrics.TimedService * * (..))")
    public void nonAnnotatedTimedServiceMethod() {}

    /**
     * All methods annotated with {@link TimedService} regardless of whether the parent class is annotated with
     * {@link TimedService}
     */
    @Pointcut("execution(@org.narrative.shared.spring.metrics.TimedService * * (..))")
    public void annotatedTimedServiceMethod() {}

    @Around("nonAnnotatedTimedServiceMethod()")
    public Object nonAnnotatedTimedServiceMethod(ProceedingJoinPoint pjp) throws Throwable {
        Class clazz = pjp.getSignature().getDeclaringType();
        TimedService timedService = (TimedService) clazz.getAnnotation(TimedService.class);
        return timeMethod(pjp, clazz, timedService);
    }

    @Around("annotatedTimedServiceMethod() && @annotation(timedService)")
    public Object annotatedTimedServiceMethod(ProceedingJoinPoint pjp, TimedService timedService) throws Throwable {
        Class clazz = pjp.getSignature().getDeclaringType();
        return timeMethod(pjp, clazz, timedService);
    }

    private Object timeMethod(ProceedingJoinPoint pjp, Class clazz, TimedService timedService) throws Throwable {
        String className = clazz.getSimpleName();
        String methodName = ((MethodSignature) pjp.getSignature()).getMethod().getName();

        String description = className + "." + methodName;

        Timer.Sample sample = Timer.start(registry);
        try {
            return pjp.proceed();
        } finally {
            sample.stop(Timer.builder(timedService.value())
                    .tag("class", className)
                    .tag("method", methodName)
                    .description(description)
                    .tags(timedService.extraTags())
                    .publishPercentileHistogram(timedService.histogram())
                    .publishPercentiles(timedService.percentiles().length == 0 ? null : timedService.percentiles())
                    .register(registry));
        }
    }
}
