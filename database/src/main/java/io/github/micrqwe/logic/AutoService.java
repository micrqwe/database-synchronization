package io.github.micrqwe.logic;

import io.github.micrqwe.service.CanalClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 14:33
 */
@Service
@Deprecated
public class AutoService {
    private Logger logger = LoggerFactory.getLogger(AutoService.class);
    @Autowired
    private CanalClientService canalClientService;
    @Autowired
    private TableCustomSycLogic threadCurrentService;

   /* public void synchronizationAuto(String table, String shardingTable, String key, String shardingKey,int shardingSize, int size, int sleep) {
        logger.info("正在开始清理canal数据");
        boolean boo = canalClientService.synchronizationAuto(table, shardingTable, key, shardingKey, size, sleep, 1);
        if (!boo) {
            logger.error("清空数据失败，详见日志");
            return;
        }
        logger.info("canal订阅数据已经清空，开始进行跑分表同步数据");
        boo = threadCurrentService.synchronization(table, shardingTable, key, shardingKey,shardingSize, size, sleep);
        if (!boo) {
            logger.error("分表同步数据错误。详见日志");
            return;
        }
        logger.info("分表数据已经同步完成。开始进行订阅增量数据");
        canalClientService.synchronization(table, shardingTable, key, shardingKey, size, sleep, 0);
    }*/
}
