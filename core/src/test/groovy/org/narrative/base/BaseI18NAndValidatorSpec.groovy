package org.narrative.base

import org.narrative.network.customizations.narrative.service.api.ValidationContext
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper
import org.narrative.network.customizations.narrative.service.impl.common.ValidationContextImpl
import org.narrative.network.customizations.narrative.service.impl.common.ValidationExceptionFactory
import org.narrative.network.customizations.narrative.util.ValidationHelper
import org.springframework.beans.BeansException
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.MessageSource
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.Validator

abstract class BaseI18NAndValidatorSpec extends Specification {
    @Shared
    Locale locale = Locale.getDefault()
    @Shared
    Validator validator
    @Shared
    LocalValidatorFactoryBean validatorFactoryBean
    @Shared
    ValidationExceptionFactory validationExceptionFactory
    @Shared
    ObjectFactory<ValidationContext> objectFactory
    @Shared
    MessageSource messageSource
    @Shared
    MessageSourceAccessor messageSourceAccessor
    @Shared
    LogSuppressor logSuppressor = new LogSuppressor();

    StaticMethodWrapper staticMethodWrapper;
    ValidationHelper validationHelper

    def setupSpec() {
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames('narrative','narrative_niches','global')

        messageSourceAccessor = new MessageSourceAccessor(messageSource, locale)

        validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource)
        validatorFactoryBean.afterPropertiesSet()
        validator = new SpringValidatorAdapter(validatorFactoryBean.validator)

        objectFactory = new ObjectFactory<ValidationContext>() {
            @Override
            ValidationContext getObject() throws BeansException {
                return new ValidationContextImpl(messageSource, locale)
            }
        }

        validationExceptionFactory = new ValidationExceptionFactory(objectFactory)
    }

    def setup() {
        staticMethodWrapper = Mock(StaticMethodWrapper)
        validationHelper = (ValidationHelper) Spy(ValidationHelper, constructorArgs: [objectFactory, validator])
    }
}