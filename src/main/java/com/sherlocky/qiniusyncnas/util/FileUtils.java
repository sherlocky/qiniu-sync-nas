package com.sherlocky.qiniusyncnas.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
public class FileUtils {
    private static String location;

    /**
     * 静态变量可以这样注入配置值
     * @param location
     */
    @Value("${sync.nas.location}")
    public void setLocation(String location) {
        FileUtils.location = FilenameUtils.normalizeNoEndSeparator(location);
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
        return new File(FileUtils.getLocationPath());
    }

    /**
     * 基于 nas location 路径获取文件路径
     * @param relFilePath
     * @return String
     */
    public static String getFilePath(String relFilePath) {
        Assert.notNull(relFilePath, "$$$ relFilePath 不能为 null！");
        return FilenameUtils.normalizeNoEndSeparator(FileUtils.getLocationPath() + "/" + relFilePath);
    }

    /**
     * 基于 nas location 路径获取文件对象
     * @param relFilePath
     * @return File
     */
    public static File getFile(String relFilePath) {
        return new File(FileUtils.getFilePath(relFilePath));
    }

    /**
     * 下载文件
     * <p>如果文件路径是多层级目录结构，需要事先创建完各级目录</p>
     * @param downloadUrl 下载地址
     * @param relFilePath 下载目标相对路径
     * @param fileSize 文件大小，单位：字节(作为下载后完整度简单校验)
     * @return
     */
    public static boolean downloadFile(String downloadUrl, String relFilePath, long fileSize) {
        Assert.notNull(downloadUrl, "$$$ downloadUrl 不能为 null！");
        String destFilePath = FileUtils.getFilePath(relFilePath);
        File destFile = new File(destFilePath);
        if (destFile.exists()) {
            // 已存在，且大小相等，可认为是相同文件
            if (destFile.exists() && destFile.length() == fileSize) {
                if (log.isInfoEnabled()) {
                    log.info("@@@ 文件已存在，无需下载~ " + destFilePath);
                }
                return true;
            }
            // 已存在，且大小不等，尝试删除
            boolean deleted = org.apache.commons.io.FileUtils.deleteQuietly(destFile);
            // 如删除失败，退出
            if (!deleted) {
                log.error("$$$ 文件下载失败，已存在同名文件，且无法删除！" + destFilePath);
                return false;
            }
        }
        try {
            // 设置 readTimeout 为 10 分钟
            org.apache.commons.io.FileUtils.copyURLToFile(new URL(downloadUrl), destFile, 60 * 1000, 600 * 1000);
        } catch (IOException e) {
            log.error("$$$ 下载七牛文件失败，下载地址 -> " + downloadUrl + " 路径：" + destFilePath, e);
        }
        if (destFile.exists() && destFile.length() == fileSize) {
            if (log.isInfoEnabled()) {
                log.info("$$$ 下载成功！" + destFilePath);
            }
            return true;
        }
        log.error("$$$ 下载完成，但文件校验失败！" + destFilePath);
        return false;
    }
}