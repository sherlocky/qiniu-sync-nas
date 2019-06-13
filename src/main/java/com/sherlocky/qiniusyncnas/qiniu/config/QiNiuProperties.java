package com.sherlocky.qiniusyncnas.qiniu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 七牛云属性配置
 */

/**
 * <p>默认情况下，如果只使用 @ConfigurationProperties 注解，是没有把当前类注册成为一个 Bean 的</p>
 * <p>此时可以配合使用 @Component 注解 直接进行注入</p>
 * <p>或者在使用了 @Configuration 注解 的类上添加 @EnableConfigurationProperties 注解</p>
 */
@ConfigurationProperties(prefix = "qiniu")
public class QiNiuProperties {
    /**
     * 七牛云的密钥
     */
    private String accessKey = "access-key";
    private String secretKey = "secret-key";
    /**
     * 存储空间名字
     */
    private String bucketName = "bucket-name";
    /**
     * 一般设置为cdn
     */
    private String cdnPrefix = "cdn";

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getCdnPrefix() {
        return cdnPrefix;
    }

    public void setCdnPrefix(String cdnPrefix) {
        this.cdnPrefix = cdnPrefix;
    }
}
