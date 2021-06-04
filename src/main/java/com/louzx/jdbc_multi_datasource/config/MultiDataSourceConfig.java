package com.louzx.jdbc_multi_datasource.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

public class MultiDataSourceConfig {

    @Bean("localDB")
    @ConfigurationProperties(prefix = "spring.datasource.local")
    public DataSource localDB (){
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("yunDB")
    @ConfigurationProperties(prefix = "spring.datasource.yun")
    public DataSource yunDB () {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("jdbcTemplateLocal")
    public JdbcTemplate jdbcTemplateLocal(@Qualifier("localDB") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("jdbcTemplateYun")
    public JdbcTemplate jdbcTemplateYun(@Qualifier("yunDB") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Primary
    @Bean("namedParameterJdbcTemplateLocal")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplateLocal(@Qualifier("localDB") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean("namedParameterJdbcTemplateYun")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplateYun(@Qualifier("yunDB") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

}
