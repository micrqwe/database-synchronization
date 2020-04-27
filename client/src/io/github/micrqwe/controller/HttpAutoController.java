package io.github.micrqwe.controller;

import io.github.micrqwe.logic.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class HttpAutoController {
    private AtomicBoolean canal = new AtomicBoolean(false);
    private Logger logger = LoggerFactory.getLogger(HttpAutoController.class);
    @Autowired
    private AutoService autoService;
    @RequestMapping("auto")
    public String auto(String table, String shardingTable, String key, String shardingKey,int shardingSize, int size, int sleep) {
        if(canal.get()){
            return "禁止多次执行";
        }
        canal.set(true);
        new Thread(()->{
//            autoService.synchronizationAuto(table, shardingTable, key, shardingKey,shardingSize, size, sleep);
        }).start();
        return "ok";
    }
    @RequestMapping("test")
    public String test(){

//        logger.error("test");
//        logger.warn("test");
//        logger.info("test");
        logger.debug("test");
        return "ok";
    }
}
