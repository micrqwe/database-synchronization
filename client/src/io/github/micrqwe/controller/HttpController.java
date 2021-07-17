package io.github.micrqwe.controller;

import io.github.micrqwe.logic.TableCustomSycLogic;
import io.github.micrqwe.service.CanalClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 9:48
 */
@RestController
@Deprecated
public class HttpController {
//    @Autowired
//    private TableCustomSycLogic threadCurrentService;
    @Autowired
    private CanalClientService testSignCanalClientExample;
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private AtomicBoolean canal = new AtomicBoolean(false);

    @RequestMapping("synchronizationDatabase")
    public String synchronizationDatabase(String table, String shardingTable, String key, String shardingKey,int shardingSize, int size, int sleep) {
        if (atomicBoolean.get()) {
            return "禁止多次执行";
        }
        atomicBoolean.set(true);
        new Thread(()->{
//            threadCurrentService.synchronization(table, shardingTable, key, shardingKey,shardingSize, size, sleep);
        }).start();
        return "ok";
    }

    @RequestMapping("canal")
    public String canal(String table, String shardingTable, String key, String shardingKey, int size, int sleep) {
        if (canal.get()) {
            return "禁止多次执行";
        }
        canal.set(true);
        new Thread(()->{
            testSignCanalClientExample.synchronization(table, shardingTable, key, shardingKey, size, sleep, 0);
        }).start();
        return "ok";
    }

    @RequestMapping("testClearCanal")
    public String testClearCanal(String table, String shardingTable, String key, String shardingKey, int size, int sleep) {
        if (canal.get()) {
            return "禁止多次执行";
        }
        canal.set(true);
        new Thread(()->{
            testSignCanalClientExample.synchronizationAuto(table, shardingTable, key, shardingKey, size, sleep, 1);
        }).start();
        return "ok";
    }
}
