package com.sherlocky.qiniusyncnas.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author: zhangcx
 * @date: 2019/6/14 13:54
 */
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
     * @param filePath
     * @return String
     */
    public static String getFilePath(String filePath) {
        return FilenameUtils.normalizeNoEndSeparator(FileUtils.getLocationPath() + "/" + filePath);
    }

    /**
     * 基于 nas location 路径获取文件对象
     * @param filePath
     * @return File
     */
    public static File getFile(String filePath) {
        return new File(FileUtils.getFilePath(filePath));
    }
}
