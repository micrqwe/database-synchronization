package io.github.micrqwe.model;

/**
 * 记录当前表同步到的id
 * @author shaowenxing@cnstrong.cn
 * @since 11:10
 */
public class TableKey {
    private String table;
    private long id;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
