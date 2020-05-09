package io.github.micrqwe.logic;

import io.github.micrqwe.config.DataSourceConfig;
import io.github.micrqwe.dao.DatabaseInsertLogic;
import io.github.micrqwe.dao.MysqlExecuteSqlLogic;
import io.github.micrqwe.model.ColumnModel;
import io.github.micrqwe.model.SynchronizationModelDTO;
import io.github.micrqwe.model.reqVo.CustomTableSynReqVo;
import io.github.micrqwe.service.DatabaseCallback;
import io.github.micrqwe.service.ThreadCurrentService;
import io.github.micrqwe.util.ColumnStringUtils;
import io.github.micrqwe.util.DataBaseUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * 针对数据库全量复制处理
 *
 * @author shaowenxing@cnstrong.cn
 * @since 17:52
 */
public class TableTotalSynLogic {
    private Logger logger = LoggerFactory.getLogger(TableTotalSynLogic.class);

    /**
     * 不同数据源同步
     * tableSynReqVo  同步数据
     * observers 注册当前表数据同步的id
     *
     * @return
     */
    public boolean synchronizationDataSource(CustomTableSynReqVo tableSynReqVo, List<Observer> observers) {
        // 注册通知
        DatabaseCallback databaseCallback = null;
        if (!CollectionUtils.isEmpty(observers)) {
            databaseCallback = new DatabaseCallback();
            for (Observer observer : observers) {
                databaseCallback.addObserver(observer);
            }
        }
        String[] tables = null;
        // 初始化数据库 插入的目标数据库
        DataSource targetDataSource = DataSourceConfig.dataSource(tableSynReqVo.getTargetUrl(), tableSynReqVo.getTargetName(), tableSynReqVo.getTargetPassword(), tableSynReqVo.getInsertPool() );
        DataSource sourceDataSource = DataSourceConfig.dataSource(tableSynReqVo.getSourceUrl(), tableSynReqVo.getSourceName(), tableSynReqVo.getSourcePassword(),  tableSynReqVo.getQueryPool());
        // 判断是全量表还是部分表
        if (StringUtils.isBlank(tableSynReqVo.getTable())) {
            tables = connection(sourceDataSource);
        } else {
            tables = tableSynReqVo.getTable().split(",");
        }
        // 定义插入的数据源
        DatabaseInsertLogic databaseInsertLogic = new MysqlExecuteSqlLogic(targetDataSource);
        // 执行数据库
        ThreadCurrentService threadCurrentService = new ThreadCurrentService(databaseInsertLogic, databaseCallback);
        // 处理了表数据
        for (String t : tables) {
            Pair<List<ColumnModel>, String> columns = DataBaseUtils.getDatabaseTable(sourceDataSource, t);
            if (CollectionUtils.isEmpty(columns.getKey())) {
                logger.info(t + "当前表中没有数据");
                continue;
            }
            if (StringUtils.isBlank(columns.getValue())) {
                logger.info(t + "当前表无主键。跳过");
                continue;
            }
            SynchronizationModelDTO synchronizationModel = new SynchronizationModelDTO();
            synchronizationModel.setSource(sourceDataSource);
            synchronizationModel.setTargetSource(targetDataSource);
            synchronizationModel.setTable(t);
            synchronizationModel.setShardingTable(t);
            synchronizationModel.setKey(columns.getValue());
            synchronizationModel.setSize(tableSynReqVo.getSize());
            synchronizationModel.setSleep(tableSynReqVo.getSleep());
            synchronizationModel.setShardingSize(0);
            synchronizationModel.setQueryPool(tableSynReqVo.getQueryPool());
            synchronizationModel.setInsertPool(tableSynReqVo.getInsertPool());
            threadCurrentService.synchronizationTable(columns.getKey(), synchronizationModel);
        }
        return false;
    }

    /**
     * 查询当前库的表
     *
     * @param sourceDataSource
     * @return
     */
    public String[] connection(DataSource sourceDataSource) {
        List<String> str = new ArrayList<>();
        Connection connection = null;
        try {
            connection = sourceDataSource.getConnection();
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getTables(connection.getCatalog(), null, null,
                    new String[]{"TABLE"});
            while (rs.next()) {
                str.add(rs.getString(3));
//                System.out.println("表名：" + rs.getString(3));
//                System.out.println("表所属用户名：" + rs.getString(2));
//                System.out.println("------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ColumnStringUtils.close(connection);
        }
        return (String[]) str.toArray();
    }
}
