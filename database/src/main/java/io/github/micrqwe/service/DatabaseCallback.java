package io.github.micrqwe.service;

import io.github.micrqwe.model.SyncCallback;

import java.util.Observable;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 13:58
 */
public class DatabaseCallback extends Observable {

    /**
     * 数据库当前节点回调
     *
     * @param table
     * @param id
     */
    public void databasecallback(String table, Long id) {
        setChanged();
        SyncCallback syncCallback = new SyncCallback();
        syncCallback.setId(id);
        syncCallback.setTable(table);
        notifyObservers(syncCallback);
    }
}
