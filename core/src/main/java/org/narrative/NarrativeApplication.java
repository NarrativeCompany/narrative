package org.narrative;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        SolrAutoConfiguration.class
})
@ServletComponentScan
@ComponentScan(excludeFilters = @ComponentScan.Filter(type= FilterType.REGEX, pattern = "org.narrative.common.persistence.hibernate.SchemaGenerator"))
@EnableCaching
public class NarrativeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NarrativeApplication.class, args);
    }
}

