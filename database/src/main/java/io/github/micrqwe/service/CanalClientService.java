package io.github.micrqwe.service;

import io.github.micrqwe.dao.MysqlExecuteSqlLogic;
import io.github.micrqwe.util.ColumnStringUtils;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.Message;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CanalClientService {

    private Logger logger = LoggerFactory.getLogger(CanalClientService.class);
    //    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    @Value("${u.jdbc.database}")
    private String database;
    private String table;
    private String shardingTable;
    private String key;
    private String shardingKey;
    private int sleep;
    private int size;
    @Value("${canal.zkServer}")
    private String zkServer;
    @Value("${canal.destination}")
    private String destination;
    private int clear;
    @Autowired
    private DataSource dataSource;
    @Value("${sharding.size}")
    private int shardingSize;

    // 是使用zk连接 还是普通连接
    @Value("${canal.sign:1}")
    private int canalSign;
    @Value("${canal.host:#{null}}")
    private String canalHost;
    @Value("${canal.port:11111}")
    private int canalPort;

    /**
     * 进行sql进行执行
     */
    private MysqlExecuteSqlLogic executeSqlLogic;

    public boolean synchronization(String table, String shardingTable, String key, String shardingKey, int size, int sleep, int clear) {
/*        if (atomicBoolean.get()) {
            return "当前只准处理一个";
        }
        atomicBoolean.set(true);*/
        return s(table, shardingTable, key, shardingKey, size, sleep, clear, 0);
    }

    public boolean synchronizationAuto(String table, String shardingTable, String key, String shardingKey, int size, int sleep, int clear) {
        return s(table, shardingTable, key, shardingKey, size, sleep, clear, 1);
    }

    private boolean s(String table, String shardingTable, String key, String shardingKey, int size, int sleep, int clear, int auto) {
        this.table = table;
        this.shardingTable = shardingTable;
        this.key = key;
        this.shardingKey = shardingKey;
        this.size = size;
        this.sleep = sleep;
        this.clear = clear;
        logger.info("正在进行开始连接.....");
        executeSqlLogic = new MysqlExecuteSqlLogic(dataSource);
        return init(1, auto);
    }

    /**
     * 开始进行订阅biglog数据
     *
     * @param i
     * @param auto
     * @return
     */
    private boolean init(int i, int auto) {
        // 创建链接
        CanalConnector connector = null;
        if (canalSign == 1) {
            connector = CanalConnectors.newClusterConnector(zkServer, destination, "", "");
        } else {
            connector = CanalConnectors.newSingleConnector(new InetSocketAddress(canalHost,
                    canalPort), destination, "", "");
        }
        // 初始化值
        if (sleep == 0) {
            sleep = 10;
        }
        // size过大无效。不会一次性拿取这么多。有mysql的日志限制
        if (size > 2000) {
            size = 2000;
        }
        if (clear == 1 && size > 500) {
            size = 500;
        }
        try {
            connector.connect();
            logger.info("正在开始订阅数据.....,{},{}", database, table);
            connector.subscribe(database + "." + table);
            logger.info("订阅成功，开始执行客户端处理;.....,{},{}", database, table);
            connector.rollback();
//            int totalEmptyCount = 1000;
            long l = System.currentTimeMillis();
            while (true) {
                Message message = connector.getWithoutAck(size); // 获取指定数量的数据
                long batchId = message.getId();
                List<Entry> entrys = message.getEntries();
                int size = entrys.size();
                // 没有拿到则休息一下
                if (batchId == -1 || size == 0) {
                    if (System.currentTimeMillis() - l > (1000 * 20)) {
                        logger.debug("data is sleep");
                        l = System.currentTimeMillis();
                    }
                    TimeUnit.MILLISECONDS.sleep(sleep);
                } else {
                    logger.debug("开始进行执行订阅的sql数据,当前执行数量:{}", size);
                    printEntry(entrys);
                }
                connector.ack(batchId); // 提交确认
                if (size < this.size && auto == 1) {
                    logger.info("以清空canal。重新订阅处理");
                    return true;
                }
                i = 1;
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }
        } catch (Exception e) {
            logger.error("canal Error", e);
            logger.info("正在进行重试，当前第{}次.最大重试5次", i);
            i++;
            try {
                TimeUnit.SECONDS.sleep(i * 5);
            } catch (InterruptedException t) {
                logger.error("休眠失败", t);
            }
            if (i == 6) {
                logger.info("重试终止。未能启动");
                return false;
            }
        } finally {
            connector.disconnect();
            logger.info("canal断开连接");
        }
        init(i, auto);
        return false;
    }

    private void printEntry(List<Entry> entrys) {
        try {
            List<String> sql = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            for (Entry entry : entrys) {
                if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                    continue;
                }
                if (!entry.getHeader().getTableName().equalsIgnoreCase(table)) {
                    continue;
                }
                if (!entry.getHeader().getSchemaName().equalsIgnoreCase(database)) {
                    continue;
                }
                // 开始进行拼装数据
                RowChange rowChage = RowChange.parseFrom(entry.getStoreValue());
                int action = rowChage.getEventType().getNumber();
                for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                    rowDataExcute(action, rowData, sql, ids);
                }
            }
            if (CollectionUtils.isEmpty(sql)) {
                return;
            }
            if (clear == 1) {
                if (sql.size() > 0) {
                    logger.info("第一条SQL:{}", sql.get(0));
                }
                logger.info("本次丢弃SQL数量:" + sql.size());
            } else {
//                logger.debug("执行sql数量:{},组装时间:{}", sql.size(), System.currentTimeMillis() - time);
               executeSqlLogic.execute(sql);
                // 有必要的监控处理
//                monitorService.monitor(ids);
            }
        } catch (Exception e) {
            logger.error("p", e);
        }
    }


    /**
     * 进行拼装sql数据
     * @param action
     * @param rowData
     * @param sql
     * @param ids
     */
    public void rowDataExcute(int action, CanalEntry.RowData rowData, List<String> sql, List<String> ids) {
        if (action == CanalEntry.EventType.DELETE_VALUE) {
            Pair<String, String> pair = getColumn(rowData.getBeforeColumnsList());
            if (pair.getKey() == null || pair.getValue() == null) {
                return;
            }
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("DELETE FROM ");
            stringBuffer.append(table(pair.getRight()));
            stringBuffer.append(" where ");
            stringBuffer.append(key);
            stringBuffer.append("=");
            stringBuffer.append(pair.getLeft());
            sql.add(stringBuffer.toString());
            ids.add(pair.getLeft());
        } else if (action == CanalEntry.EventType.UPDATE_VALUE) {
            Pair<String, String> pair = getColumn(rowData.getBeforeColumnsList());
            if (pair.getKey() == null || pair.getValue() == null) {
                return;
            }
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("UPDATE  ");
            stringBuffer.append(table(pair.getRight()));
            stringBuffer.append(" set ");
            column(rowData.getAfterColumnsList(), stringBuffer);
            stringBuffer.append(" where ");
            stringBuffer.append(key);
            stringBuffer.append("=");
            stringBuffer.append(pair.getLeft());
            sql.add(stringBuffer.toString());
            ids.add(pair.getLeft());
        } else {
            Pair<String, String> pair = getColumn(rowData.getAfterColumnsList());
            if (pair.getKey() == null || pair.getValue() == null) {
                return;
            }
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("INSERT INTO ");
            stringBuffer.append(table(pair.getRight()));
            stringBuffer.append(" value (");
            insertColumn(rowData.getAfterColumnsList(), stringBuffer);
            sql.add(stringBuffer.toString());
            ids.add(pair.getLeft());
        }
    }

    /**
     * 分表处理的数据
     *
     * @param keyId 分片键
     * @return
     */
    public String table(String keyId) {
        Long l = Long.valueOf(keyId);
        long shardingNum = (l % shardingSize + shardingSize) % shardingSize;
        return shardingTable + "_" + shardingNum;
    }


    private void insertColumn(List<Column> columns, StringBuilder stringBuffer) {
        int size = columns.size();
        int i = 0;
        for (Column column : columns) {
            ColumnStringUtils.columeAppend(stringBuffer, column.getSqlType(), column.getValue());
            if (size - 1 != i) {
                stringBuffer.append(",");
            } else {
                stringBuffer.append(")");
            }
            i++;
        }
    }

    private void column(List<Column> columns, StringBuilder stringBuffer) {
        int size = columns.size();
        int i = 0;
        for (Column column : columns) {
            stringBuffer.append(column.getName());
            stringBuffer.append("=");
            ColumnStringUtils.columeAppend(stringBuffer, column.getSqlType(), column.getValue());
            if (size - 1 != i) {
                stringBuffer.append(",");
            }
            i++;
        }
    }

    /**
     * @param columns
     * @return 左边是id  右边是分片键的value
     */
    private Pair<String, String> getColumn(List<Column> columns) {
        String id = null;
        String key = null;
        for (Column column : columns) {
            if (column.getName().equalsIgnoreCase(this.key)) {
                id = column.getValue();
            } else if (column.getName().equalsIgnoreCase(shardingKey)) {
                key = column.getValue();
            }
        }
        return Pair.of(id, key);
    }
}