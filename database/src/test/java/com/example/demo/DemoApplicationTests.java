package com.example.demo;


import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DemoApplicationTests {

    @Test
    public void test() {
        for (int i = 0; i <32 ; i++) {
            String s = "CREATE TABLE `pap_res_tag_"+i+"` (\n" +
                    "  `res_tag_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源标签关联ID',\n" +
                    "  `res_id` bigint(20) NOT NULL COMMENT '资源ID',\n" +
                    "  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',\n" +
                    "  `res_type` tinyint(2) NOT NULL COMMENT '资源类型',\n" +
                    "  PRIMARY KEY (`res_tag_id`),\n" +
                    "  KEY `tag_res_inx` (`tag_id`,`res_id`,`res_type`),\n" +
                    "  KEY `res_tag_inx` (`res_id`,`tag_id`,`res_type`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=43434975 DEFAULT CHARSET=utf8;";
            System.out.println(s);
        }
    }
    @Test
    public void testd() {
        for (int i = 0; i <100 ; i++) {
            String s = "TRUNCATE HW_HOMEWORK_DTL_STUDENT_"+i+";";
            System.out.println(s);
        }
    }
}
