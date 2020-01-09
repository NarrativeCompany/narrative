package org.narrative.config;

import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.impl.common.ValidationContextImpl;
import org.narrative.network.customizations.narrative.util.ValidationHelper;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;

@Configuration
public class ValidationConfig {
    /**
     * Customize a validator to use the application resource bundle instead of the default "Validation" bundle.
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource);
        return validatorFactoryBean;
    }

    /**
     * Bean factory for ValidationContext instances
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ValidationContext serviceTaskValidationContext(MessageSource messageSource, WebMvcProperties webMvcProperties) {
        //This is the default locale but will be forced for all back end requests via configuration setting
        // spring.mvc.locale-resolver=fixed
        return new ValidationContextImpl(messageSource, webMvcProperties.getLocale());
    }

    /**
     * Friendly wrapper for message source that can be injected into services
     */
    @Bean
    public MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
        return new MessageSourceAccessor(messageSource);
    }

    /**
     * Java validation helper
     */
    @Bean
    public ValidationHelper validationHelper(ObjectFactory<ValidationContext> validationContextObjectFactory, Validator validator) {
        return new ValidationHelper(validationContextObjectFactory, validator);
    }
}
