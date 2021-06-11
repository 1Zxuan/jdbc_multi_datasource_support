package com.louzx.jdbc_multi_datasource;

import com.louzx.jdbc_multi_datasource.config.MultiDataSourceConfig;
import com.louzx.jdbc_multi_datasource.utils.SpringBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({MultiDataSourceConfig.class, SpringBean.class})
@SpringBootApplication
public class JdbcMultiDatasourceSupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdbcMultiDatasourceSupportApplication.class, args);
    }
}
