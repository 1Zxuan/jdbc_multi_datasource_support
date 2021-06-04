package com.louzx.jdbc_multi_datasource;

import com.louzx.jdbc_multi_datasource.config.MultiDataSourceConfig;
import com.louzx.jdbc_multi_datasource.utils.TransactionWrapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;


@Import(MultiDataSourceConfig.class)
@SpringBootApplication
public class JdbcMultiDatasourceSupportApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(JdbcMultiDatasourceSupportApplication.class, args);
    }

    @Resource
    private JdbcTemplate jdbcTemplateLocal;
    @Resource
    private JdbcTemplate jdbcTemplateYun;

    @Override
    public void run(String... args) throws Exception {
        TransactionWrapper transactionWrapper = new TransactionWrapper();
        transactionWrapper.push(jdbcTemplateLocal, "UPDATE sys_user SET username = ? WHERE id = '110'", "0");
        transactionWrapper.push(jdbcTemplateYun, "UPDATE sys_user1 SET username = ? WHERE id = '110'", "0");
        try {
            transactionWrapper.execute();
        } catch (Exception e) {
            e.printStackTrace();
            transactionWrapper.rollBack();
        } finally {
            transactionWrapper.commit();
        }
    }
}
