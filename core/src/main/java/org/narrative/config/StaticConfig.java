package org.narrative.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;

@Configuration
public class StaticConfig implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Do part 1 of static initialization here.  This gives us a bean we can @DependsOn to force ordered start up
     * when using non-Spring managed resources.
     */
    @Bean("servletConfigInitializer")
    public Boolean getServletConfigInitialized(ServletContext servletContext) {
        org.narrative.common.util.Configuration.createServletConfiguration(servletContext);
        return true;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static <T> T getBean(String beanName, @Nullable Class<T> beanType){
        return context.getBean(beanName, beanType);
    }

    public static Resource getApplicationContextResource(String resourceLocation) {
        return context.getResource(resourceLocation);
    }
}
