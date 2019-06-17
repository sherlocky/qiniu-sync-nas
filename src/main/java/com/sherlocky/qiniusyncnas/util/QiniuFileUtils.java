package com.sherlocky.qiniusyncnas.util;

import com.sherlocky.qiniusyncnas.constant.QiniuSyncNasConstants;
import com.sherlocky.qiniusyncnas.constant.QiniuSyncNasConstants.CheckResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author: zhangcx
 * @date: 2019/6/14 13:54
 */
@Slf4j
@Component
public class QiniuFileUtils {
    private static String location;

    /**
     * 静态变量可以这样注入配置值
     * @param location
     */
    @Value("${sync.nas.location}")
    public void setLocation(String location) {
        QiniuFileUtils.location = FilenameUtils.normalizeNoEndSeparator(location);
    }

    /**
     * 获取nas location 路径
     * @return String
     */
    public static String getLocationPath() {
        return location;
    }

    /**
     * 获取nas location 文件对象
     * @return File
     */
    public static File getLocation() {
        return new File(QiniuFileUtils.getLocationPath());
    }

    /**
     * 基于 nas location 路径获取文件路径
     * @param relFilePath
     * @return String
     */
    public static String getFilePath(String relFilePath) {
        Assert.notNull(relFilePath, "$$$ relFilePath 不能为 null！");
        return FilenameUtils.normalizeNoEndSeparator(QiniuFileUtils.getLocationPath() + "/" + relFilePath);
    }

    /**
     * 基于 nas location 路径获取文件对象
     * @param relFilePath
     * @return File
     */
    public static File getFile(String relFilePath) {
        return new File(QiniuFileUtils.getFilePath(relFilePath));
    }

    /**
     * 下载文件
     * <p>如果文件路径是多层级目录结构，需要事先创建完各级目录</p>
     * @param downloadUrl 下载地址
     * @param relFilePath 下载目标相对路径
     * @param fileSize 文件大小，单位：字节(作为下载后完整度简单校验)
     * @param filePutTime 文件上传时间，Unix时间戳格式，单位：纳秒
     * @return
     */
    public static boolean downloadFile(String downloadUrl, String relFilePath, long fileSize, long filePutTime) {
        Assert.notNull(downloadUrl, "$$$ downloadUrl 不能为 null！");
        String destFilePath = QiniuFileUtils.getFilePath(relFilePath);
        // 下载后校验
        CheckResult result = QiniuFileUtils.checkFile(relFilePath, fileSize, filePutTime);
        if (CheckResult.EXISTS == result) {
            if (log.isInfoEnabled()) {
                log.info("@@@ 文件已存在，无需下载~ " + destFilePath);
            }
            return true;
        }

        File destFile = new File(destFilePath);
        // 过期或者下载失败的空文件 先删除掉
        if (CheckResult.EMPTY == result || CheckResult.EXPIRED == result) {
            boolean deleted = org.apache.commons.io.FileUtils.deleteQuietly(destFile);
            // 如删除失败，退出
            if (!deleted) {
                log.error("$$$ 文件下载失败，已存在同名文件，且无法删除！" + destFilePath);
                return false;
            }
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("@@@@@@ 开始下载：");
            }
            // 设置 readTimeout 为 10 分钟
            org.apache.commons.io.FileUtils.copyURLToFile(new URL(downloadUrl), destFile, 60 * 1000, 600 * 1000);
            if (log.isDebugEnabled()) {
                log.debug("@@@@@@ 下载完成：");
            }
        } catch (IOException e) {
            log.error("$$$ 下载七牛文件失败，下载地址 -> " + downloadUrl + " 路径：" + destFilePath, e);
        }
        // 下载后校验
        result = QiniuFileUtils.checkFile(relFilePath, fileSize, filePutTime);
        if (CheckResult.EXISTS == result) {
            if (log.isInfoEnabled()) {
                log.info("$$$ 下载成功！" + destFilePath);
            }
            return true;
        }
        log.error("$$$ 下载完成，但文件校验失败！校验结果：【" + result + "】 " + destFilePath);
        return false;
    }

    private static CheckResult checkFile(String relFilePath, long fileSize, long filePutTime) {
        String destFilePath = QiniuFileUtils.getFilePath(relFilePath);
        File destFile = new File(destFilePath);
        // 文件已存在
        if (!destFile.exists()) {
            return CheckResult.NON_EXISTS;
        }
        // 文件已存在
        if (destFile.length() == 0) {
            return CheckResult.EMPTY;
        }
        String ext = FilenameUtils.getExtension(relFilePath);
        boolean needCheckSize = true;
        if (StringUtils.containsIgnoreCase(QiniuSyncNasConstants.NO_CHECK_FILE_TYPES, ext)) {
            needCheckSize = false;
        }
        // 需要校验大小的，大小相等即为相同文件，不等即为过期文件
        if (needCheckSize) {
            return destFile.length() == fileSize ? CheckResult.EXISTS : CheckResult.EXPIRED;
        }
        // filePutTime 单位为纳秒
        boolean isExpired = destFile.lastModified() * 1000 < (filePutTime / 10000);
        // 不需要校验大小的（七牛图片瘦身功能，图片会无损压缩，大小会变化），磁盘文件写入时间晚于/等于七牛文件上传时间即为相同文件，否则为过期文件
        return isExpired ? CheckResult.EXPIRED : CheckResult.EXISTS;
    }
}