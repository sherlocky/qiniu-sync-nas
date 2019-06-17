package com.sherlocky.qiniusyncnas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QiniuSyncNasApplication {

    public static void main(String[] args) {
        SpringApplication.run(QiniuSyncNasApplication.class, args);
    }

    /**
     * 默认情况下 TaskScheduler 的 poolSize = 1
     * <p>如果是多个任务的情况下，容易出现竞争情况，可以手动修改线程池大小</p>
     * @return 线程池
     */
    /**
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        return taskScheduler;
    }
    */
}
