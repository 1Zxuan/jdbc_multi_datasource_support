package com.louzx.jdbc_multi_datasource.core;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
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

    PlatformTransactionManager platformTransactionManager;
    List<String> sql;
    private TransactionDefinition transactionDefinition;
    private TransactionStatus transactionStatus;
    private DataSource dataSource;

    public AbstractTransactionUnit(PlatformTransactionManager platformTransactionManager, List<String> sql, TransactionDefinition transactionDefinition, DataSource dataSource) {
        this.platformTransactionManager = platformTransactionManager;
        this.sql = sql;
        this.transactionDefinition = transactionDefinition;
        this.dataSource = dataSource;
    }

    void begin() throws SQLException {
        if (null == platformTransactionManager) {
            if (null == dataSource) {
                throw new SQLException("数据源为空");
            }
            platformTransactionManager = new DataSourceTransactionManager(dataSource);
        }

        transactionStatus = null == transactionDefinition ? platformTransactionManager.getTransaction(new DefaultTransactionDefinition()) : platformTransactionManager.getTransaction(new DefaultTransactionDefinition(transactionDefinition));

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
        if (null != dataSource) {
            platformTransactionManager = null;
        }
    }
}
