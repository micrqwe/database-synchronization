package io.github.micrqwe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 18:12
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
public class SynchronizationApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(SynchronizationApplication.class, args);
    }

}
