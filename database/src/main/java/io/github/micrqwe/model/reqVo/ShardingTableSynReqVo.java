package io.github.micrqwe.model.reqVo;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 18:02
 * 可选同步表
 */
public class ShardingTableSynReqVo extends CustomTableSynReqVo {
    private String shardingTable;

    private String shardingKey;
    // 是否有分片，没有填0
    private int shardingSize;

    public String getShardingTable() {
        return shardingTable;
    }

    public void setShardingTable(String shardingTable) {
        this.shardingTable = shardingTable;
    }

    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }

    public int getShardingSize() {
        return shardingSize;
    }

    public void setShardingSize(int shardingSize) {
        this.shardingSize = shardingSize;
    }
}
