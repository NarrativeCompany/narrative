package org.narrative.config;

import org.narrative.config.properties.NarrativeProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    private final NarrativeProperties narrativeProperties;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setMaxPageSize(narrativeProperties.getSpring().getMvc().getMaxPageSize());
        argumentResolvers.add(resolver);
    }

    @Autowired
    public MvcConfiguration(NarrativeProperties narrativeProperties) {this.narrativeProperties = narrativeProperties;}
}