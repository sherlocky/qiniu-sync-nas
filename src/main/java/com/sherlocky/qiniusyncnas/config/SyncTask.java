package com.sherlocky.qiniusyncnas.config;

import com.sherlocky.qiniusyncnas.entity.SyncResult;
import com.sherlocky.qiniusyncnas.service.QiniuSyncNasService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SyncTask implements SchedulingConfigurer {
    /** 默认每天凌晨4点执行 */
    private static String cron;
    @Autowired
    private QiniuSyncNasService syncService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                //1.添加任务内容(Runnable)
                () -> {
                    task();
                },
                //2.设置执行周期(Trigger)
                triggerContext -> {
                    //2.1 合法性校验.
                    if (!CronSequenceGenerator.isValidExpression(cron)) {
                        log.error("$$$$$$ Cron 表达式：" + cron + " 格式错误！");
                    }
                    //2.2 返回执行周期(Date)
                    return new CronTrigger(cron).nextExecutionTime(triggerContext);
                }
        );
    }

    private void task() {
        if (log.isInfoEnabled()) {
            log.info("###### 七牛文件同步任务开始", LocalDateTime.now());
        }
        SyncResult r = syncService.sync();
        log.error("###### 同步结果：" + r);
        if (log.isInfoEnabled()) {
            log.info("###### 七牛文件同步任务结束~", LocalDateTime.now());
        }
    }

    /**
     * 更新 cron 表达式
     * @param newCron
     * @return
     */
    public static boolean updateCron(String newCron) {
        if (CronSequenceGenerator.isValidExpression(newCron)) {
            cron = newCron;
            return true;
        }
        return false;
    }

    public static String getCron() {
        return cron;
    }

    @Value("${sync.cron:0 0 4 * * ?}")
    public void initCron(String cron) {
        System.out.println("111111111111111111111111111111111");
        SyncTask.cron = cron;
    }
}