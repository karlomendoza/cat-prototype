package com.example.service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.example.persistence.config.PersistenceJPAConfig;

@Configuration
@Import(PersistenceJPAConfig.class)  //Import config class from spring-data
@ComponentScan(basePackages = "com.example.service")
public class ServiceContext {

}
