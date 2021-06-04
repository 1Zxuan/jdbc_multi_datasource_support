package com.louzx.jdbc_multi_datasource.utils;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * @author louzx
 * @date 2021/6/4
 */
abstract class AbstractTransactionUnit {

    protected PlatformTransactionManager platformTransactionManager;
    protected TransactionStatus transactionStatus;
    List<String> sql;
    private DataSource dataSource;

    AbstractTransactionUnit(List<String> sql, DataSource dataSource) {
        this.sql = sql;
        this.dataSource = dataSource;
    }

    void begin() throws SQLException {
        if (null == dataSource) {
            throw new SQLException("数据源为空");
        }
        if (null == platformTransactionManager) {
            platformTransactionManager = new DataSourceTransactionManager(dataSource);
        }
        transactionStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

    }

    abstract void execute();

    void commit(boolean commit) {
        if (null != platformTransactionManager && null != transactionStatus && !transactionStatus.isCompleted()) {
            if (commit) {
                platformTransactionManager.commit(transactionStatus);
            } else {
                platformTransactionManager.rollback(transactionStatus);
            }
        }
        platformTransactionManager = null;
        transactionStatus = null;
    };
}
