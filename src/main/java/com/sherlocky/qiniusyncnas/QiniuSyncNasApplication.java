package com.sherlocky.qiniusyncnas;

import com.sherlocky.qiniusyncnas.entity.SyncResult;
import com.sherlocky.qiniusyncnas.service.QiniuSyncNasService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class QiniuSyncNasApplication implements CommandLineRunner {
    @Autowired
    private QiniuSyncNasService service;

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

    @Override
    public void run(String... args) throws Exception {
        log.error("启动后默认执行一次同步任务：");
        log.error("###### 七牛文件同步任务开始", LocalDateTime.now());
        SyncResult r = service.sync();
        log.error("###### 同步结果：" + r);
        log.info("###### 七牛文件同步任务结束~", LocalDateTime.now());
    }
}
