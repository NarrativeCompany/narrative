package org.narrative.shared.spring.metrics;

import io.micrometer.core.annotation.Timed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking service classes and methods as candidates for metric weaving.
 * This is a clone of {@link Timed}
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TimedService {
    String value() default "";

    String[] extraTags() default {};

    boolean longTask() default false;

    double[] percentiles() default {};

    boolean histogram() default false;

    String description() default "";
}
