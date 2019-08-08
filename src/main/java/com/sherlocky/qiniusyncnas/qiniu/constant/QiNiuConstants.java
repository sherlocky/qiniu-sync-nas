package com.sherlocky.qiniusyncnas.qiniu.constant;

/**
 * 七牛云相关配置常量
 * @author: zhangcx
 * @date: 2019/6/15 21:08
 */
public abstract class QiNiuConstants {
    /** 默认协议 */
    public static final String DEFAULT_SCHEMA = "https";
    /** 协议分隔符 */
    public static final String SCHEMA_SEPARATOR = "://";
    /** bucket空间是否私有标识(0:公开 1:私有)*/
    public static final int BUCKET_PUBLIC = 0;
    public static final int BUCKET_PRIVATE = 1;
}
