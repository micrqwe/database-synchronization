package io.github.micrqwe.model.reqVo;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 18:06
 */
public class TableSynReqVo {
    /**
     * 来源数据库URL
     */
    private String sourceUrl;
    /**
     * 数据库账号
     */
    private String sourceName;
    /**
     * 数据库密码
     */
    private String sourcePassword;
    /**
     * 目标数据库URL
     */
    private String targetUrl;
    /**
     * 数据库账号
     */
    private String targetName;
    /**
     * 数据库密码
     */
    private String targetPassword;

    /**
     * 数据库查询线程数
     */
    private Integer queryPool;
    /**
     * 插入数据线程数量
     */
    private Integer insertPool;
    /**
     * 每次数量
     */
    private int size = 5000;
    /**
     * 是否每次是否休眠
     */
    private int sleep = 0;


    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourcePassword() {
        return sourcePassword;
    }

    public void setSourcePassword(String sourcePassword) {
        this.sourcePassword = sourcePassword;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetPassword() {
        return targetPassword;
    }

    public void setTargetPassword(String targetPassword) {
        this.targetPassword = targetPassword;
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
}
