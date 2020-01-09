package org.narrative.config;

import org.narrative.network.customizations.narrative.service.mapper.NicheMapper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal Spring config to bootstrap mappers for testing
 */
@Configuration
@ComponentScan(basePackageClasses = {NicheMapper.class})
public class MapperConfig {}
