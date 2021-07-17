package io.github.micrqwe.util;

import io.github.micrqwe.model.ColumnModel;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 14:51
 */
public class DataBaseUtils {
    private static Logger logger = LoggerFactory.getLogger(DataBaseUtils.class);
    private static int PRIMARYKEY = 1;
    private static int ORDINARY = 0;

    /**
     * 目前只支持数据库表必须要一个自增Id
     *
     * @param dataSource
     * @param table
     * @return
     */
    public static Pair<List<ColumnModel>, String> getDatabaseTable(DataSource dataSource, String table) {
        List<ColumnModel> columns = new ArrayList<>();
        // 获取表名和字段类型
        Connection connection = null;
        String key = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select * from " + table + " limit 1";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            // 获取元信息
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            // 获取元信息
            DatabaseMetaData metaData = connection.getMetaData();
            // 获取主键信息
            ResultSet pkInfo = metaData.getPrimaryKeys(connection.getCatalog(), connection.getCatalog(), table);
//            pkInfo.next();
            List<String> keys = new ArrayList<>();
            // 表有主键  则会有2个.这里当前只用第一个
            if(pkInfo.next()){
                keys.add(pkInfo.getString("COLUMN_NAME"));
            }
            // 主键有多个，则使用主键类型为整数类型的
            Map<String, ColumnModel> columnModelMap = new HashMap<>();
            int count = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= count; i++) {
                ColumnModel column = new ColumnModel();
                column.setName(resultSetMetaData.getColumnName(i));
                column.setType(resultSetMetaData.getColumnType(i));
                column.setKey(ORDINARY);
                if (keys.contains(column.getName())) {
//                    column.setKey(1);
                    columnModelMap.put(column.getName(), column);
                }
                columns.add(column);
            }
            // 主键只有一个
            if (columnModelMap.size() == 1) {
                ColumnModel columnModel = columnModelMap.values().iterator().next();
                columnModel.setKey(PRIMARYKEY);
                key = columnModel.getName();
            }
        } catch (Exception e) {
            logger.error(table + "获取表的源信息失败");
        } finally {
            ColumnStringUtils.close(connection);
        }
        return Pair.of(columns, key);
    }

}
