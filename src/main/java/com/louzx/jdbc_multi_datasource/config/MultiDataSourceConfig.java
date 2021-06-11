package com.louzx.jdbc_multi_datasource.config;

import com.louzx.jdbc_multi_datasource.constants.CommonConstants;
import com.louzx.jdbc_multi_datasource.utils.SpringBean;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public class MultiDataSourceConfig {

    @Primary
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

    @Bean("platformTransactionManagerLocal")
    public PlatformTransactionManager platformTransactionManagerLocal (@Qualifier("localDB") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("platformTransactionManagerYun")
    public PlatformTransactionManager platformTransactionManagerYun (@Qualifier("yunDB") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    public static PlatformTransactionManager platformTransactionManager (String dbId) {
        switch (dbId) {
            case CommonConstants.DB_ID.DB_LOCAL: return SpringBean.getBean("platformTransactionManagerLocal", DataSourceTransactionManager.class);
            case CommonConstants.DB_ID.DB_YUN: return SpringBean.getBean("platformTransactionManagerYun", DataSourceTransactionManager.class);
            default:
                return null;
        }
    }

    public static JdbcTemplate jdbcTemplate (String dbId) {
        switch (dbId) {
            case CommonConstants.DB_ID.DB_LOCAL: return SpringBean.getBean("jdbcTemplateLocal", JdbcTemplate.class);
            case CommonConstants.DB_ID.DB_YUN: return SpringBean.getBean("jdbcTemplateYum", JdbcTemplate.class);
            default:
                return null;
        }
    }

    public static NamedParameterJdbcTemplate namedParameterJdbcTemplate (String dbId) {
        switch (dbId) {
            case CommonConstants.DB_ID.DB_LOCAL: return SpringBean.getBean("namedParameterJdbcTemplateLocal", NamedParameterJdbcTemplate.class);
            case CommonConstants.DB_ID.DB_YUN: return SpringBean.getBean("namedParameterJdbcTemplateYun", NamedParameterJdbcTemplate.class);
            default:
                return null;
        }
    }
}
