package io.github.micrqwe.model;

/**
 * 数据库当前节点回调
 * @author shaowenxing@cnstrong.cn
 * @since 14:02
 */
public class SyncCallback {
    private String table;
    private Long id;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
