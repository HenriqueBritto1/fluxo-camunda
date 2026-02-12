//package com.br.fluxo.config;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DatabaseConfig {
//
//    @Primary
//    @Bean(name = "dataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.app")
//    public DataSource appDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "camundaBpmDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.camunda")
//    public DataSource camundaDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//}
