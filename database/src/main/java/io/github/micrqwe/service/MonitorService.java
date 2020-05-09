package io.github.micrqwe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 记录同步过程中 突然的请求
 *
 * @author shaowenxing@cnstrong.cn
 * @since 19:52
 */
@Deprecated
public class MonitorService {
    // 报警规则，如果5秒内超过5000条数据， 立马打印
    private List<List<String>> list = new LinkedList<>();
    private AtomicLong atomicLong = new AtomicLong();
    private Logger logger = LoggerFactory.getLogger(MonitorService.class);
    /**
     * 使用线程池进行判断
     */
    private ExecutorService cachedThreadPool = Executors.newSingleThreadExecutor();

    public void monitor(List<String> sql) {
        cachedThreadPool.execute(()->{
            // 记录总SQL
            list.add(sql);
            long time = System.currentTimeMillis()-atomicLong.get();
            int size = list.size();
            // 符合第一规则，没有满5秒。但是数据超过5000条。开始打印
            if(time<5000 && size>5000){
                logger.info("5秒内更新超过5000条,ids:{}",list);
                list.clear();
                // 重置时间
                atomicLong.set(System.currentTimeMillis());
                return;
            }
            // 5秒没到。而且没满5000条。跳过
            if(time<5000){
                return;
            }
            // 重置时间
            atomicLong.set(System.currentTimeMillis());
            // 5秒到了。但是没有更新超过5000条
            if(size<5000){
                list.clear();
                return;
            }
            // 5秒到了。更新超过5000条数据
            logger.info("5秒内更新超过5000条,ids:{}",list);
        });
    }
}
