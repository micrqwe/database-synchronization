package io.github.micrqwe.util;

import io.github.micrqwe.model.ColumnModel;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 14:51
 */
public class DataBaseUtils {
    private static Logger logger = LoggerFactory.getLogger(DataBaseUtils.class);

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
            pkInfo.next();
            key = pkInfo.getString("COLUMN_NAME");
            int count = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= count; i++) {
                ColumnModel column = new ColumnModel();
                column.setName(resultSetMetaData.getColumnName(i));
                column.setType(resultSetMetaData.getColumnType(i));
                column.setKey(0);
                if (column.getName().equalsIgnoreCase(key)) {
                    column.setKey(1);
                }
                columns.add(column);
            }
        } catch (Exception e) {
            logger.error(table + "获取表的源信息失败");
        } finally {
            ColumnStringUtils.close(connection);
        }
        return Pair.of(columns, key);
    }

}
