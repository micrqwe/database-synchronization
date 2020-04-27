package io.github.micrqwe.model;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 18:06
 */
public class SourceTableVO {
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
}
