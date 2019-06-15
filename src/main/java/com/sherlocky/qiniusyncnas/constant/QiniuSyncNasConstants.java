package com.sherlocky.qiniusyncnas.constant;

/**
 * @author: zhangcx
 * @date: 2019/6/15 21:20
 */
public abstract class QiniuSyncNasConstants {
    /** 默认协议 */
    public static final String NO_CHECK_FILE_TYPES = "jpg,jpeg,png";
    /** 文件校验结果 */
    public enum CheckResult {
        /** 已存在 */
        EXISTS,
        /** 不存在 */
        NON_EXISTS,
        /** 已过期 */
        EXPIRED,
        /** 空文件 */
        EMPTY;
    }
}
