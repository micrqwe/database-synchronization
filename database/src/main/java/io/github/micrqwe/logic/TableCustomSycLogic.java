package io.github.micrqwe.logic;

import io.github.micrqwe.config.DataSourceConfig;
import io.github.micrqwe.dao.DatabaseInsertLogic;
import io.github.micrqwe.dao.MysqlExecuteSqlLogic;
import io.github.micrqwe.model.ColumnModel;
import io.github.micrqwe.model.SourceTableVO;
import io.github.micrqwe.model.SynchronizationModelDTO;
import io.github.micrqwe.service.ThreadCurrentService;
import io.github.micrqwe.util.DataBaseUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * 自定义表之间同步数据
 *
 * @author shaowenxing@cnstrong.cn
 * @since 17:53
 */
@Deprecated
public class TableCustomSycLogic {
    private Logger logger = LoggerFactory.getLogger(TableCustomSycLogic.class);

    private ThreadCurrentService threadCurrentService;
    @Autowired
    private DataSourceConfig dataSourceConfig;

    @Value("${thread.maxpool}")
    private Integer threadPool;
    @Value("${thread.pool}")
    private int poolSize;
    @Value("${thread.query}")
    private int queryThreadSize;
    @Value("${thread.insert}")
    private int insertThreadSize;
    /**
     * 同源同步
     *
     * @param table
     * @param shardingTable
     * @param key
     * @param shardingKey
     * @param shardingSize
     * @param size
     * @param sleep
     * @return
     */
   /* public boolean synchronization(SourceTableVO sourceTableVO, String table, String shardingTable, String key, String shardingKey, int shardingSize, int size, int sleep) {
        // 初始化数据源
        DataSource initDataSource = dataSourceConfig.dataSource(sourceTableVO.getSourceUrl(), sourceTableVO.getSourceName(), sourceTableVO.getSourcePassword(), threadPool);
        // 定义插入的数据源
        DatabaseInsertLogic databaseInsertLogic = new MysqlExecuteSqlLogic(initDataSource);
        threadCurrentService = new ThreadCurrentService(databaseInsertLogic);

        Pair<List<ColumnModel>, String> columns = DataBaseUtils.getDatabaseTable(initDataSource, table);
        if (CollectionUtils.isEmpty(columns.getKey())) {
            logger.info(table + "当前表中没有数据");
            return false;
        }
        SynchronizationModelDTO synchronizationModel = new SynchronizationModelDTO();
        synchronizationModel.setSource(initDataSource);
        synchronizationModel.setTargetSource(initDataSource);
        synchronizationModel.setTable(table);
        synchronizationModel.setShardingTable(shardingTable);
        synchronizationModel.setKey(key);
        synchronizationModel.setShardingKey(shardingKey);
        synchronizationModel.setSize(size);
        synchronizationModel.setSleep(sleep);
        synchronizationModel.setShardingSize(shardingSize);
        synchronizationModel.setQueryPool(queryThreadSize);
        synchronizationModel.setInsertPool(insertThreadSize);
        return threadCurrentService.synchronizationTable(columns.getKey(), synchronizationModel);
    }*/

}
