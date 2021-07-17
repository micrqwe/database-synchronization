package io.github.micrqwe.dao;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 11:43
 */
public abstract class DatabaseInsertLogic {
    /**
     * 建立对应的数据源
     */
    protected List<DataSource> dataSourceList;

    /**
     * 执行方法
     * @param sql
     * @return
     */
    public abstract boolean execute(List<String> sql) ;

}
