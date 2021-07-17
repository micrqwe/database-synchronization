package io.github.micrqwe.config;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 19:13
 */
public class DataSourceConfig {
    /**
     * 线程池数量，同时也对应数据库线程池数量
     */
//    @Value("${thread.maxpool}")
//    private Integer threadPool;

    public  static  DataSource dataSource(String url, String username, String password,int threadPool) {
        HikariDataSource hikari = new HikariDataSource();
        hikari.setJdbcUrl("jdbc:mysql://" + url + "?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false");
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikari.setPoolName("Paper_HikariCP");
        hikari.setMaximumPoolSize(threadPool);
        hikari.setMinimumIdle(threadPool);
        hikari.setIdleTimeout(100000);
        hikari.setMaxLifetime(300000);
        hikari.setConnectionTimeout(15000);
//        hikari.setConnectionTestQuery("SELECT 1");
//        hikari.setValidationTimeout(3000);
//        hikari.setAutoCommit(true);
        hikari.setInitializationFailTimeout(5000);
        return hikari;
    }
}
