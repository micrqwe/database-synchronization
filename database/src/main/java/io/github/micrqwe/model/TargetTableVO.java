package io.github.micrqwe.model;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 18:06
 */
public class TargetTableVO {

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
}
