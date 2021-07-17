package io.github.micrqwe.dao;

import io.github.micrqwe.service.CanalClientService;
import io.github.micrqwe.util.ColumnStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 19:16
 */
public class MysqlExecuteSqlLogic extends DatabaseInsertLogic {


    private Logger logger = LoggerFactory.getLogger(CanalClientService.class);


    /**
     * 初始化数据源
     *
     * @param dataSourceList
     */
    public MysqlExecuteSqlLogic(DataSource dataSourceList) {
        List<DataSource> dataSources = new ArrayList<>(1);
        dataSources.add(dataSourceList);
        super.dataSourceList = dataSources;
    }

    /**
     * 初始化数据源
     *
     * @param dataSourceList
     */
    public MysqlExecuteSqlLogic(List<DataSource> dataSourceList) {
        super.dataSourceList = dataSourceList;

    }

    /**
     * 执行拼装好的SQL数据
     *
     * @param sql
     */
    public boolean execute(List<String> sql) {
        if (dataSourceList.size() == 1) {
            return excuteBatch(sql, dataSourceList.get(0));
        }
        for (DataSource dataSource : dataSourceList) {
            excuteBatch(sql, dataSource);
        }
        return true;
    }

    private boolean excuteBatch(List<String> sql, DataSource dataSource) {
        boolean boo = true;
        Connection connection = null;
        try {
            long time = System.currentTimeMillis();
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            int size = sql.size();
            for (int i = 0; i < size; i++) {
                statement.addBatch(sql.get(i));
            }
            statement.executeBatch();
            logger.debug("sql进行执行,sql数量:{}，总执行时间:{}", sql.size(), System.currentTimeMillis() - time);
        } catch (Exception e) {
            boo = false;
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("'PRIMARY'")) {
                logger.error("插入异常转换,插入数据主键重复，{}", e.getMessage());
            } else {
                logger.error("e", e);
            }
        } finally {
            ColumnStringUtils.close(connection);
        }
        return boo;
    }


}
