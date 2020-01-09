package org.narrative.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * Date: 9/6/18
 * Time: 8:11 PM
 *
 * @author brian
 */
@Configuration
public class MultipartConfig {
    @Bean
    public CommonsMultipartResolver filterMultipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        resolver.setMaxInMemorySize(0);
        // limit uploads to 100 MB
        resolver.setMaxUploadSize(100 * 1024 * 1024);
        return resolver;
    }
}
