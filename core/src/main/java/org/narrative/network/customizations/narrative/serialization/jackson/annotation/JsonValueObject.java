package org.narrative.network.customizations.narrative.serialization.jackson.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import org.narrative.network.customizations.narrative.serialization.jackson.JacksonConst;
import org.narrative.network.customizations.narrative.serialization.jackson.view.View;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for DTOs to include a field indicating the underlying type during serialization plus default view.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@JacksonAnnotationsInside
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = JacksonConst.SER_TYPE_FIELD)
@JsonView(View.Summary.class)
public @interface JsonValueObject {}
