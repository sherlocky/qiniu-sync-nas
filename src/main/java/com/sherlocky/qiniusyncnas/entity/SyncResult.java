package com.sherlocky.qiniusyncnas.entity;

/**
 * @author: zhangcx
 * @date: 2019/6/14 22:55
 */
public class SyncResult {
    /** 文件总个数 */
    private long totalCount;
    /** 同步成功文件个数 */
    private long successCount;
    /** 是否成功 */
    private boolean success;
    /** 同步结果信息 */
    private String message;
    /** 同步完成时间:Unix 时间戳，单位毫秒 */
    private long dateline;

    public SyncResult() {
        super();
        dateline();
    }

    public SyncResult(String message) {
        this.success = false;
        this.message = message;
        dateline();
    }

    public SyncResult(long totalCount, long successCount) {
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.success = totalCount > 0 && successCount > 0;
        dateline();
    }

    private void dateline() {
        // 当前系统时间 作为同步完成时间
        this.dateline = System.currentTimeMillis();
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDateline() {
        return dateline;
    }

    public void setDateline(long dateline) {
        this.dateline = dateline;
    }
}
