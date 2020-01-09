package org.narrative.config;

import org.narrative.config.localization.FixedLanguageHeaderLocaleResolver;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.customizations.narrative.converters.EnumConverter;
import org.narrative.network.customizations.narrative.interceptors.ReadOnlyInterceptor;
import org.narrative.network.customizations.narrative.interceptors.SpringProcessInterceptor;
import org.narrative.network.customizations.narrative.interceptors.UserStatusInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new EnumConverter());
    }

    /**
     * Configure a mapping handler that prefixes the matching URI with the specified prefix.  This enables a prefix
     * to be specified for all API controllers without hard-coding that prefix into the controller annotations or
     * forcing all controllers to subclass an abstract class.
     */
    @Bean
    WebMvcRegistrations restPrefixAppender(NarrativeProperties narrativeProperties) {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RequestMappingHandlerMapping() {
                    @Override
                    protected RequestMappingInfo getMappingForMethod(Method method, @NotNull Class<?> handlerType) {
                        RestController restControllerAnno = handlerType.getAnnotation(RestController.class);
                        RequestMappingInfo mappingForMethod = super.getMappingForMethod(method, handlerType);

                        //If this method is part of a REST controller, use the API prefix
                        if (mappingForMethod != null) {
                            if (restControllerAnno != null) {
                                WebhookController webhookControllerAnno = handlerType.getAnnotation(WebhookController.class);
                                String baseUri;
                                // if this is a webhook annotation, then use the webhooks base URI
                                if(webhookControllerAnno!=null) {
                                    baseUri = narrativeProperties.getSpring().getMvc().getWebhooksBaseUri();
                                } else {
                                    baseUri = narrativeProperties.getSpring().getMvc().getBaseUri();
                                }
                                return RequestMappingInfo.paths(baseUri).build().combine(mappingForMethod);
                            } else {
                                return mappingForMethod;
                            }
                        } else {
                            return null;
                        }
                    }
                };
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SpringProcessInterceptor()).order(Ordered.HIGHEST_PRECEDENCE);
        registry.addInterceptor(new ReadOnlyInterceptor()).order(Ordered.HIGHEST_PRECEDENCE+1);
        registry.addInterceptor(new UserStatusInterceptor()).order(Ordered.HIGHEST_PRECEDENCE+2);
    }

    /**
     * Override Spring provided {@link org.springframework.web.servlet.LocaleResolver}
     */
    @Bean
    public LocaleResolver localeResolver(WebMvcProperties webMvcProperties) {
        return new FixedLanguageHeaderLocaleResolver(webMvcProperties.getLocale());
    }

    @Bean
    public AcceptHeaderLocaleResolver acceptHeaderLocaleResolver() {
        return new AcceptHeaderLocaleResolver();
    }
}
