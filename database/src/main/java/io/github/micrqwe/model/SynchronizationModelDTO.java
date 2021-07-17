package io.github.micrqwe.model;

import javax.sql.DataSource;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 15:04
 */
public class SynchronizationModelDTO {
//    List<ColumnModel> columns,String table, String shardingTable, String key, String shardingKey,DataSource targetSource,
//    javax.sql.DataSource dataSource, int size, int sleep
    private String table;
    private String key;
    private String shardingTable;
    private String shardingKey;
    private DataSource targetSource;
    private DataSource source;
    // 是否有分片，没有填0
    private int shardingSize;
    /**
     * 数据库查询线程数
     */
    private int queryPool=0;
    /**
     * 插入数据线程数量
     */
    private int insertPool=0;
    /**
     * 每次数量
     */
    private int size = 5000;
    /**
     * 是否每次是否休眠
     */
    private int sleep = 0;


    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getShardingTable() {
        return shardingTable;
    }

    public void setShardingTable(String shardingTable) {
        this.shardingTable = shardingTable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }

    public DataSource getTargetSource() {
        return targetSource;
    }

    public void setTargetSource(DataSource targetSource) {
        this.targetSource = targetSource;
    }

    public DataSource getSource() {
        return source;
    }

    public void setSource(DataSource source) {
        this.source = source;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getShardingSize() {
        return shardingSize;
    }

    public void setShardingSize(int shardingSize) {
        this.shardingSize = shardingSize;
    }

    public Integer getQueryPool() {
        return queryPool;
    }

    public void setQueryPool(Integer queryPool) {
        this.queryPool = queryPool;
    }

    public Integer getInsertPool() {
        return insertPool;
    }

    public void setInsertPool(Integer insertPool) {
        this.insertPool = insertPool;
    }
}
