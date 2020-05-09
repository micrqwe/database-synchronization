package io.github.micrqwe.service;

import io.github.micrqwe.dao.DatabaseInsertLogic;
import io.github.micrqwe.model.ColumnModel;
import io.github.micrqwe.model.SynchronizationModelDTO;
import io.github.micrqwe.util.ColumnStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 9:44
 */
public class ThreadCurrentService {
    private Logger logger = LoggerFactory.getLogger(ThreadCurrentService.class);

    //    @Autowired
    private ThreadPoolExecutor queryPoolTaskExecutor;
    //    @Autowired
    private ThreadPoolExecutor insertPoolTaskExecutor;
    /**
     * 插入线程控制
     */
    private AtomicInteger insertSize = new AtomicInteger(0);
    /**
     * 读取线程控制
     */
    private AtomicInteger querySize = new AtomicInteger(0);

    private DatabaseInsertLogic mysqlExecuteSqlLogic;

    /**
     * 回调数据
     */
    private DatabaseCallback databaseCallback;
    /**
     * 记录当前表的位置
     */
    private HashMap<String, AtomicLong> keySize = new HashMap<>();



    public ThreadCurrentService(DatabaseInsertLogic databaseInsertLogic, DatabaseCallback databaseCallback) {
        this.mysqlExecuteSqlLogic = databaseInsertLogic;
        this.databaseCallback = databaseCallback;
    }

    /**
     * 初始化查询线程数据
     *
     * @param model
     */
    private void initParam(SynchronizationModelDTO model) {
        // 创建线程池
        ArrayBlockingQueue<Runnable> arrayBlockingQueue = new ArrayBlockingQueue<>(model.getQueryPool());
        queryPoolTaskExecutor = new ThreadPoolExecutor(model.getQueryPool(), model.getQueryPool(),
                1, TimeUnit.MINUTES, arrayBlockingQueue, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "query_" + model.getTable() + "_synchronization");
            }
        }, new ThreadPoolExecutor.AbortPolicy());

        // 创建插入线程池
        ArrayBlockingQueue<Runnable> insertBlockingQueue = new ArrayBlockingQueue<>(model.getInsertPool());
        insertPoolTaskExecutor = new ThreadPoolExecutor(model.getInsertPool(), model.getInsertPool(),
                1, TimeUnit.MINUTES, insertBlockingQueue, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "insert_" + model.getTable() + "_synchronization");
            }
        }, new ThreadPoolExecutor.AbortPolicy());
        // 同步到对应的数据源，不一定是自己的数据源
//        mysqlExecuteSqlLogic = new MysqlExecuteSqlLogic(model.getTargetSource());
    }

    /**
     * 进行同步数据
     *
     * @param columns
     * @param model
     */
//    @Async
    public boolean synchronizationTable(List<ColumnModel> columns, SynchronizationModelDTO model) {
        // 初始化表
        if (keySize.get(model.getTable()) == null) {
            keySize.put(model.getTable(), new AtomicLong(0));
        }
    /*    if ((model.getQueryPool() + model.getInsertPool()) > poolSize) {
            logger.info("请合理设置查询线程和插入线程：查询线程数量：{}，插入线程数量:{},总数量:{}", queryThreadSize, insertThreadSize, poolSize);
            return false;
        }*/
        if (model.getQueryPool() < 1 || model.getInsertPool() < 1) {
            logger.info("请合理设置查询线程和插入线程：查询线程数量：{}，插入线程数量:{},总数量:{}", model.getQueryPool(), model.getInsertPool());
            return false;
        }
        // 参数初始化
        initParam(model);
        // 拼装SQL
        String idSql = querySqlId(model.getTable(), model.getKey(), model.getSize());
        // 当前位置
        String sql = querySql(columns, model.getTable(), model.getKey());
        logger.info("当前查询Id的sql：{}，查询数据SQL：{}", idSql, sql);
        // 打印一下其中表最大的SQL
        queryMaxId(model.getSource(), model.getTable(), model.getKey());
        int columnSize = columns.size();
        // 只查询id出来
        while (true) {
            Connection connection = null;
            try {
                querySize.incrementAndGet();
                insertSize.incrementAndGet();
                while (querySize.get() > model.getQueryPool()) {
                    logger.debug("查询等待:执行数量:{},插入的数量:{}", querySize.get(), insertSize.get());
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                while (insertSize.get() > model.getInsertPool()) {
                    logger.debug("插入等待等待:执行数量:{},插入的数量:{}", querySize.get(), insertSize.get());
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                logger.debug("当前查询的执行数量:{},插入的数量:{}", querySize.get(), insertSize.get());
                // 在变之前进行记录下
                connection = model.getSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(idSql);
                preparedStatement.setLong(1, keySize.get(model.getTable()).get());
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                Long currentId = resultSet.getLong(1);
                if (currentId == null || currentId == 0) {
                    logger.info("当前数据查询出来为空:{}", currentId);
                    break;
                }
                queryInsertSql(model, columns, sql, columnSize, keySize.get(model.getTable()).get(), currentId);
                // 置为下一个
                keySize.get(model.getTable()).set(currentId + 1);
                logger.debug("正在进行任务同步处理:处理{}表id到了:{}", model.getTable(), currentId);
                TimeUnit.MILLISECONDS.sleep(model.getSleep());
            } catch (Exception e) {
                logger.error("当前位置在这里出现了获取数据库连接地址错误,起始id：" + keySize.get(model.getTable()).get(), e);
            } finally {
                ColumnStringUtils.close(connection);
            }
        }
        try {
            while (querySize.get() != 1) {
                logger.info("正在等待最后的查询执行完毕:{}", querySize.get());

                TimeUnit.SECONDS.sleep(1);
            }
            while (insertSize.get() != 1) {
                logger.info("正在等待最后的插入执行完毕:{}", insertSize.get());
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        logger.info("{}:表数据已经同步完成=======================================", model.getTable());
        // 关闭本次同步任务队列线程池
        queryPoolTaskExecutor.shutdown();
        insertPoolTaskExecutor.shutdown();
        queryPoolTaskExecutor = null;
        insertPoolTaskExecutor = null;
        return true;
    }

    private void queryMaxId(DataSource dataSource, String table, String key) {
        String maxId = "select max(" + key + ") from " + table;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            Statement preparedStatement = connection.createStatement();
            ResultSet resultSet = preparedStatement.executeQuery(maxId);
            resultSet.next();
            logger.info("当前查询最大的表:{},其中最大的id：{}", table, resultSet.getLong(1));
        } catch (Exception e) {
            logger.error("查询最大id错误", e);
        } finally {
            ColumnStringUtils.close(connection);
        }
    }

    private void queryInsertSql(SynchronizationModelDTO model, List<ColumnModel> columns, String sql, int columnSize, Long currentId, Long nextId) {
        queryPoolTaskExecutor.execute(() -> {
            Connection connection = null;
            try {
                // 在变之前进行记录下
                connection = model.getSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setLong(1, currentId);
                preparedStatement.setLong(2, nextId);
                logger.debug("当前查询的id日志范围:{}~{}", currentId, nextId);
                // 插入SQL
                Map<String, List<String>> sqlMaps = new HashMap<>();
                long time = System.currentTimeMillis();
                ResultSet resultSet = preparedStatement.executeQuery();
//                logger.debug("{}:id查询数据花费时间:{}", currentId, System.currentTimeMillis() - time);
//                time = System.currentTimeMillis();
                int i = 0;
                while (resultSet.next()) {
                    // 获取数据，开始拼装SQL
                    getSQL(columnSize, model.getShardingSize(), columns, resultSet, model.getShardingTable(), model.getShardingKey(), sqlMaps);
                    i++;
                }
                logger.debug("{}:id总共拼装数据和查找数据花了:{}", currentId, System.currentTimeMillis() - time);
                if (i > 0) {
                    execute(sqlMaps, model.getTable(), currentId);
                } else {
                    logger.debug("{}:id当前未查出数据:{}", currentId, i);
                    // 插入要释放掉
                    insertSize.decrementAndGet();
                }
            } catch (Exception e) {
                logger.error("当前位置在这里出现了获取数据库连接地址错误,起始id：" + currentId, e);
            } finally {
                querySize.decrementAndGet();
                ColumnStringUtils.close(connection);
            }
        });
    }

    /**
     * 开始执行
     *
     * @param sqls    处理的sql语句
     * @param table
     * @param current
     */
    private void execute(Map<String, List<String>> sqls, String table, long current) {
        if (CollectionUtils.isEmpty(sqls)) {
            logger.debug("当前没有数据处理");
            // 插入要释放掉
            insertSize.decrementAndGet();
            return;
        }
        insertPoolTaskExecutor.execute(() -> {
            // 拼装sql
            List<String> strings = new ArrayList<>();
            for (Map.Entry<String, List<String>> map : sqls.entrySet()) {
                StringBuilder stringBuilder = new StringBuilder("INSERT INTO ");
                stringBuilder.append(map.getKey());
                stringBuilder.append(" VALUES ");
                List<String> sql = map.getValue();
                int size = map.getValue().size();
                for (int i = 0; i < size; i++) {
                    stringBuilder.append(sql.get(i));
                    if (i < size - 1) {
                        stringBuilder.append(",");
                    }
                }
                strings.add(stringBuilder.toString());
            }
            try {
                boolean boo = mysqlExecuteSqlLogic.execute(strings);
                if (boo) {
                    // 执行成功 修改记录id
                    saveTableId(table, current);
                }
            } catch (Exception e) {
                logger.error("记录错误点，id:" + current, e);
            } finally {
                insertSize.decrementAndGet();
            }
        });
    }

    private void getSQL(int columnSize, int shardingSize, List<ColumnModel> columns, ResultSet resultSet, String shardingTable, String shardingKey, Map<String, List<String>> preparedStatement) throws Exception {
        String newTable = shardingTable;
        StringBuilder insertSQL = new StringBuilder("(");
//        StringBuffer value = new StringBuffer();
        try {
            for (int i = 0; i < columnSize; i++) {
                ColumnModel column = columns.get(i);
                if (column.getName().equalsIgnoreCase(shardingKey) && shardingSize > 0) {
                    Long l = resultSet.getLong(column.getName());
                    long shardingNum = (l % shardingSize + shardingSize) % shardingSize;
                    newTable = shardingTable + "_" + shardingNum;
                }
//                value.append(column.getName());
                ColumnStringUtils.columeAppend(insertSQL, column.getType(), resultSet.getString(column.getName()));
                if (i < columnSize - 1) {
                    insertSQL.append(",");
//                    value.append(",");
                }
            }
            insertSQL.append(")");
        } catch (SQLException e) {
            logger.error("获取table数据出错", e);
        }
//        String sql = insertSQL.toString().replace("{0}", newTable);
//        preparedStatement.add(sql);
        preparedStatement.computeIfAbsent(newTable, k -> new ArrayList<>());
        preparedStatement.get(newTable).add(insertSQL.toString());
    }

    /**
     * @param columns
     * @param table   表名
     * @param key     主键
     * @return
     */
    private String querySql(List<ColumnModel> columns, String table, String key) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT ");
        for (int i = 0; i < columns.size(); i++) {
            stringBuffer.append(columns.get(i).getName());
            if (i < columns.size() - 1) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append(" from ");
        stringBuffer.append(table);
        stringBuffer.append(" where ");
        stringBuffer.append(key);
        stringBuffer.append(" BETWEEN ? and ?");
//        stringBuffer.append(size);
        return stringBuffer.toString();
    }

    /**
     * @param table 表名
     * @param key   主键
     * @param size  当前一次性获取数量
     * @return
     */
    private String querySqlId(String table, String key, int size) {
        // SELECT xx,xxx from table where id>l order by id asc limit 500;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT max(a.");
        stringBuffer.append(key);
        stringBuffer.append(") from ( SELECT ");
        stringBuffer.append(key);
        stringBuffer.append(" from ");
        stringBuffer.append(table);
        stringBuffer.append(" where ");
        stringBuffer.append(key);
        stringBuffer.append(">? order by ");
        stringBuffer.append(key);
        stringBuffer.append(" asc limit ");
        stringBuffer.append(size);
        stringBuffer.append(") a");
        return stringBuffer.toString();
    }

    private void saveTableId(String table, Long id) {
        if (databaseCallback != null) {
            databaseCallback.databasecallback(table, id);
        }
    }

    public HashMap<String, AtomicLong> getKeySize() {
        return keySize;
    }

}
