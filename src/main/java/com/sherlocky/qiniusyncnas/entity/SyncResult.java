package com.sherlocky.qiniusyncnas.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: zhangcx
 * @date: 2019/6/14 22:55
 */
@Data
@AllArgsConstructor
public class SyncResult {
    /** 文件总个数 */
    private long totalCount;
    /** 同步成功文件个数 */
    private long successCount;

    public boolean isSuccess() {
        return totalCount > 0 && successCount > 0;
    }
}
