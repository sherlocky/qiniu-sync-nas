package com.sherlocky.qiniusyncnas.qiniu;

import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.sherlocky.qiniusyncnas.qiniu.condition.QiNiuCondition;
import com.sherlocky.qiniusyncnas.qiniu.config.QiNiuProperties;
import com.sherlocky.qiniusyncnas.service.IQiniuService;
import com.sherlocky.qiniusyncnas.service.impl.QiniuServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 七牛云属性配置
 */

// 装配配置属性
@Configuration
// 自动装配这个properties类，读取yaml自定义内容
@EnableConfigurationProperties(QiNiuProperties.class)
// service类，@ConditionalOnClass某个 Class 位于类路径上，才会实例化一个Bean。也就是说，当classpath下发现该类的情况下进行实例化。
@ConditionalOnClass(IQiniuService.class)
// 校验类
@Conditional(QiNiuCondition.class)
// 当配置文件中 qiniu 的值为 true 时，实例化此类。可以不填
@ConditionalOnProperty(prefix = "qiniu", value = "true", matchIfMissing = true)
public class QiNiuYunServiceAutoConfiguration {
    @Autowired
    private QiNiuProperties qiNiuYunProperties;

    /// 指定实例化接口的类
    @Bean
    @ConditionalOnMissingBean(QiniuServiceImpl.class)
    public IQiniuService qiNiuYunService() {
        return new QiniuServiceImpl();
    }

    // 认证信息实例
    @Bean
    public Auth auth() {
        return Auth.create(qiNiuYunProperties.getAccessKey(), qiNiuYunProperties.getSecretKey());
    }

    @Bean
    public com.qiniu.storage.Configuration cfg() {
        return new com.qiniu.storage.Configuration(Zone.autoZone());
    }

    // 构建一个七牛上传工具实例
    @Bean
    public UploadManager uploadManager() {
        ///////////////////////指定上传的Zone的信息//////////////////
        //第一种方式: 指定具体的要上传的zone
        //注：该具体指定的方式和以下自动识别的方式选择其一即可
        //要上传的空间(bucket)的存储区域为华东时
        // Zone z = Zone.zone0();
        //华北
        // Zone z = Zone.zone1();
        //华南
        // Zone z = Zone.zone2();
        //第二种方式: 自动识别要上传的空间(bucket)的存储区域是华东、华北、华南。
        return new UploadManager(cfg());
    }

    // 构建七牛空间管理实例
    @Bean
    public BucketManager bucketManager() {
        return new BucketManager(auth(), cfg());
    }
}
