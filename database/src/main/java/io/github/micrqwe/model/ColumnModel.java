package io.github.micrqwe.model;

/**
 * 映射表的所有字段 以及字段的类型
 * @author shaowenxing@cnstrong.cn
 * @since 9:55
 */
public class ColumnModel {
    private String name;
    // jdbcType
    private int type;
    // 是否是主键。主键为1，其他为0
    private int key;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
