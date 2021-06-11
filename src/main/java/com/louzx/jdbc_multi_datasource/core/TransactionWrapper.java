package com.louzx.jdbc_multi_datasource.core;

import com.louzx.jdbc_multi_datasource.config.MultiDataSourceConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.*;

/**
 * @author louzx
 * @date 2021/6/4
 */
public class TransactionWrapper {

    private final Deque<AbstractTransactionUnit> transactionOperates = new ArrayDeque<>();

    public void execute() {
        transactionOperates.forEach(AbstractTransactionUnit::execute);
    }

    public void offerNamedParameterJdbcTemplate (String dbId, String sql, Map<String, ?> sqlParams) throws Exception {
        offerNamedParameterJdbcTemplate(dbId, Collections.singletonList(sql), sqlParams);
    }

    public void offerNamedParameterJdbcTemplate (String dbId, List<String> sqlList, Map<String, ?> sqlParams) throws Exception {
        offerNamedParameterJdbcTemplate(dbId, null, sqlList, sqlParams);
    }

    public void offerNamedParameterJdbcTemplate(String dbId, TransactionDefinition transactionDefinition, List<String> sqlList, Map<String, ?> sqlParams) throws Exception {
        offer(MultiDataSourceConfig.namedParameterJdbcTemplate(dbId), MultiDataSourceConfig.platformTransactionManager(dbId), transactionDefinition, sqlList, sqlParams);
    }

    public void offerJdbcTemplate (String dbId, String sql, Object... sqlParams) throws Exception {
        offerJdbcTemplate(dbId, Collections.singletonList(sql), sqlParams);
    }

    public void offerJdbcTemplate (String dbId, List<String> sqlList, Object... sqlParams) throws Exception {
        offerJdbcTemplate(dbId, null, sqlList, sqlParams);
    }

    public void offerJdbcTemplate (String dbId, TransactionDefinition transactionDefinition, List<String> sqlList, Object... sqlParams) throws Exception {
        offer(MultiDataSourceConfig.jdbcTemplate(dbId), MultiDataSourceConfig.platformTransactionManager(dbId), transactionDefinition, sqlList, sqlParams);
    }

    public void offer (JdbcTemplate template,
                       PlatformTransactionManager transactionManager,
                       TransactionDefinition transactionDefinition,
                       List<String> sqlList,
                       Object... sqlParams) throws Exception {
        DataSource dataSource = null;
        if (null == template && null == transactionManager) {
            throw new Exception("TransactionManager and JdbcTemplate can't be empty at the same time.");
        }
        if (null == transactionManager) {
            dataSource = template.getDataSource();
        }
        AbstractTransactionUnit transactionUnit = new JdbcTemplateUnit(transactionManager, sqlList, transactionDefinition, dataSource, template, sqlParams);
        transactionUnit.begin();
        transactionOperates.offer(transactionUnit);
    }

    public void offer(NamedParameterJdbcTemplate template,
                       PlatformTransactionManager transactionManager,
                       TransactionDefinition transactionDefinition,
                       List<String> sqlList,
                       Map<String, ?> sqlParams) throws Exception {
        DataSource dataSource = null;
        if (null == transactionManager && null == template) {
            throw new Exception("TransactionManager and NamedParameterJdbcTemplate can't be empty at the same time.");
        }
        if (null == transactionManager) {
            dataSource = template.getJdbcTemplate().getDataSource();
        }
        AbstractTransactionUnit transactionUnit = new NamedParameterJdbcTemplateUnit(transactionManager, sqlList, transactionDefinition, dataSource, template, sqlParams);
        transactionUnit.begin();
        transactionOperates.offer(transactionUnit);
    }

    public void commit() {
        commit(true);
    }

    public void rollBack() {
        commit(false);
    }

    public void executeAndCommit() throws SQLException {
        try {
            execute();
            commit();
        } catch (Exception e) {
            rollBack();
            throw new SQLException(e);
        }
    }

    public void commit(boolean commit) {
        while (!transactionOperates.isEmpty()) {
            AbstractTransactionUnit transactionUnit = transactionOperates.pollLast();
            if (null != transactionUnit) {
                transactionUnit.commit(commit);
            }
        }
    }

    private final class JdbcTemplateUnit extends  AbstractTransactionUnit {

        private final JdbcTemplate jdbcTemplate;
        private Object[] sqlParams;
        private boolean executed;

        private JdbcTemplateUnit(PlatformTransactionManager platformTransactionManager, List<String> sql, TransactionDefinition transactionDefinition, DataSource dataSource, JdbcTemplate jdbcTemplate, Object[] sqlParams) {
            super(platformTransactionManager, sql, transactionDefinition, dataSource);
            this.jdbcTemplate = jdbcTemplate;
            this.sqlParams = sqlParams;
            executed = false;
        }

        @Override
        void execute() {
            if (!executed && null != sql && sql.size() > 0) {
                for (String s : sql) {
                    if (null != sqlParams && sqlParams.length > 0) {
                        jdbcTemplate.update(s, sqlParams);
                    } else {
                        jdbcTemplate.execute(s);
                    }
                }
            }
            executed = true;
        }
    }

    private final class NamedParameterJdbcTemplateUnit extends  AbstractTransactionUnit {

        private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
        private final Map<String, ?> sqlParams;
        private boolean executed;

        private NamedParameterJdbcTemplateUnit(PlatformTransactionManager platformTransactionManager, List<String> sql, TransactionDefinition transactionDefinition, DataSource dataSource, NamedParameterJdbcTemplate namedParameterJdbcTemplate, Map<String, ?> sqlParams) {
            super(platformTransactionManager, sql, transactionDefinition, dataSource);
            this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
            this.sqlParams = sqlParams;
            this.executed = false;
        }

        @Override
        void execute(){
            if (!executed && null != sql && sql.size() > 0) {
                for (String s : sql) {
                    namedParameterJdbcTemplate.update(s, sqlParams);
                }
            }
            executed = true;
        }
    }
}
