package com.sherlocky.qiniusyncnas.qiniu.constant;

/**
 * 七牛云相关配置常量
 * @author: zhangcx
 * @date: 2019/6/15 21:08
 */
public abstract class QiNiuConstants {
    /** 默认协议 */
    public static final String DEFAULT_SCHEMA = "http";
    /** 协议分隔符 */
    public static final String SCHEMA_SEPARATOR = "://";
    /** 七牛cdn图片瘦身默认支持的图片类型 */
    public static final String[] CDN_PHOTO_SLIM_TYPES = new String[]{"jpg", "jpeg", "png"};
    /** 七牛cdn图片瘦身默认支持的图片mime类型 */
    public static final String[] CDN_PHOTO_SLIM_MIME_TYPES = new String[]{"image/jpeg", "image/png"};

    /** bucket空间是否私有标识(0:公开 1:私有)*/
    public static final int BUCKET_PUBLIC = 0;
    public static final int BUCKET_PRIVATE = 1;
    /** 开启原图保护 */
    public static final int BUCKET_PROTECTED = 1;
}
