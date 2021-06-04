package com.louzx.jdbc_multi_datasource.utils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.SQLException;
import java.util.*;

/**
 * @author louzx
 * @date 2021/6/4
 */
public class TransactionWrapper {

    private final Deque<AbstractTransactionUnit> transactionOperates = new ArrayDeque<>();

    public void push (JdbcTemplate template, String sql) throws SQLException {
        push(template, Collections.singletonList(sql), null);
    }

    public void push (JdbcTemplate template, String sql, Object... sqlParams) throws SQLException {
        push(template, Collections.singletonList(sql), sqlParams);
    }

    public void push (JdbcTemplate template, List<String> sql, Object... sqlParams) throws SQLException {
        AbstractTransactionUnit transactionOperate = new JdbcTemplateUnit(template, sql, sqlParams);
        transactionOperate.begin();
        transactionOperates.push(transactionOperate);
    }
    
    public void push (NamedParameterJdbcTemplate template, String sql, Map<String, ?> sqlParams) throws SQLException {
        push(template, Collections.singletonList(sql), sqlParams);
    }

    public void push (NamedParameterJdbcTemplate template, Map<String, ?> sqlParams, String ... sql) throws SQLException {
        push(template, Arrays.asList(sql), sqlParams);
    }
    
    public void push (NamedParameterJdbcTemplate template, List<String> sql, Map<String, ?> sqlParams) throws SQLException {
        AbstractTransactionUnit transactionOperate = new NamedParameterJdbcTemplateUnit(template, sql, sqlParams);
        transactionOperate.begin();
        transactionOperates.push(transactionOperate);
    }
    
    public void execute() {
        transactionOperates.forEach(AbstractTransactionUnit::execute);
    }

    public void commit() {
        commit(true);
    }

    private void commit(boolean commit) {
        while (!transactionOperates.isEmpty()) {
            AbstractTransactionUnit transactionUnit = transactionOperates.pop();
            if (!transactionUnit.transactionStatus.isCompleted()) {
                if (commit) {
                    transactionUnit.platformTransactionManager.commit(transactionUnit.transactionStatus);
                } else {
                    transactionUnit.platformTransactionManager.rollback(transactionUnit.transactionStatus);
                }
            }
        }
    }

    public void rollBack() {
        commit(false);
    }


    private final class JdbcTemplateUnit extends  AbstractTransactionUnit {

        private final JdbcTemplate jdbcTemplate;
        private Object[] sqlParams;

        JdbcTemplateUnit(JdbcTemplate jdbcTemplate, List<String> sql, Object... sqlParams) {
            super(sql, jdbcTemplate.getDataSource());
            this.jdbcTemplate = jdbcTemplate;
            this.sqlParams = sqlParams;
        }

        @Override
        void execute(){
            if (null != sql && sql.size() > 0) {
                for (String s : sql) {
                    if (null != sqlParams && sqlParams.length > 0) {
                        jdbcTemplate.update(s, sqlParams);
                    } else {
                        jdbcTemplate.execute(s);
                    }
                }
            }
        }
    }

    private final class NamedParameterJdbcTemplateUnit extends  AbstractTransactionUnit {

        private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
        private final Map<String, ?> sqlParams;

        NamedParameterJdbcTemplateUnit(NamedParameterJdbcTemplate namedParameterJdbcTemplate, List<String> sql, Map<String, ?> sqlParams) {
            super(sql, namedParameterJdbcTemplate.getJdbcTemplate().getDataSource());
            this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
            this.sqlParams = sqlParams;
        }

        @Override
        void execute(){
            if (null != sql && sql.size() > 0) {
                for (String s : sql) {
                    namedParameterJdbcTemplate.update(s, sqlParams);
                }
            }
        }
    }
}
