package com.sherlocky.qiniusyncnas.util;

import com.sherlocky.qiniusyncnas.constant.QiniuSyncNasConstants.CheckResult;
import com.sherlocky.qiniusyncnas.qiniu.config.QiNiuProperties;
import com.sherlocky.qiniusyncnas.qiniu.constant.QiNiuConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: zhangcx
 * @date: 2019/6/14 13:54
 */
@Slf4j
@Component
public class QiniuFileUtils {
    @Autowired
    private QiNiuProperties qiNiuProperties;
    private static String location;

    /**
     * 静态变量可以这样注入配置值
     * @param location
     */
    @Value("${sync.nas.location}")
    public void setLocation(String location) {
        QiniuFileUtils.location = FilenameUtils.normalizeNoEndSeparator(location) + "/" + qiNiuProperties.getBucketName();
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
        /**
         * 校验下载链接，如果下载链接不包含协议头，默认加一个,否则无法下载
         */
        if (!downloadUrl.contains(QiNiuConstants.SCHEMA_SEPARATOR)) {
            downloadUrl = String.format("%s%s%s", QiNiuConstants.DEFAULT_SCHEMA, QiNiuConstants.SCHEMA_SEPARATOR, downloadUrl);
        }
        String destFilePath = QiniuFileUtils.getFilePath(relFilePath);
        // 下载前校验
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
        copyURLToFile(downloadUrl, destFile);
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

    /**
     * 从链接下载到文件
     * @param downloadUrl
     * @param destFile
     */
    private static void copyURLToFile(String downloadUrl, File destFile) {
        if (log.isDebugEnabled()) {
            log.debug("@@@@@@ 开始下载：");
        }
        /**
         * 解析出链接中可能存在的Header参数
         * 形如：http://iovip.qbox.me/0/90/73d58da3592f5ada5d5f690061145.jpg -H host:ghost.oss.sherlocky.com,xx:yyy
         * {@link com.sherlocky.qiniusyncnas.qiniu.service.impl.QiniuServiceImpl#getSlimDownloadUrl}
         */
        String trueDownloadUrl = downloadUrl;
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.containsIgnoreCase(trueDownloadUrl, " -H ")) {
            trueDownloadUrl = StringUtils.substringBefore(downloadUrl, " -H ");
            String headerStr = StringUtils.substringAfterLast(downloadUrl, " -H ");
            // 多个header字符串之间以逗号分隔
            String[] headerStrArr = StringUtils.split(headerStr, ",");
            for (String h : headerStrArr) {
                if (StringUtils.isBlank(h)) {
                    continue;
                }
                // 每个header键值对以冒号分隔
                String[] headerDataArr = StringUtils.split(h, ":");
                if (headerDataArr == null || headerDataArr.length != 2 || StringUtils.isBlank(headerDataArr[1])) {
                    continue;
                }
                headers.put(headerDataArr[0], headerDataArr[1]);
            }
        }
        /**
         * FileUtils.copyURLToFile 必须带协议头，且不支持http->https的协议自动重定向，弃用
         */
        copyURLToFileByHttpClient(trueDownloadUrl, headers, destFile);
        if (log.isDebugEnabled()) {
            log.debug("@@@@@@ 下载完成：");
        }
    }

    private static void copyURLToFileByHttpClient(String downloadUrl, Map<String, String> headers, File destFile) {
        CloseableHttpClient httpClient = null;
        InputStream in = null;
        FileOutputStream out = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(downloadUrl);
            // 设置header
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpget.setHeader(entry.getKey(), entry.getValue());
            }
            response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    in = entity.getContent();
                    out = new FileOutputStream(destFile);
                    IOUtils.copy(in, out);
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    log.error("$$$ 下载文件流出错！", e);
                } finally {
                    IOUtils.closeQuietly(out);
                    IOUtils.closeQuietly(in);
                }
            }
            log.info("### 状态码：{}", response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            log.error("$$$ 请求七牛文件下载失败，下载地址 -> " + downloadUrl + " 路径：" + destFile.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(httpClient);
        }
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
        return destFile.length() == fileSize ? CheckResult.EXISTS : CheckResult.EXPIRED;
    }

    /**
     * @param relFilePath
     * @param fileSize
     * @param filePutTime
     *
     * @deprecated 旧版本不支持图片瘦身的情况，废弃
     * @return
     */
    @Deprecated
    private static CheckResult checkFileOld(String relFilePath, long fileSize, long filePutTime) {
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
         // 非图片瘦身类型才需要校验大小
         boolean needCheckSize = !isSlimType(relFilePath);
         // 需要校验大小的，大小相等即为相同文件，不等即为过期文件
         if (needCheckSize) {
         return destFile.length() == fileSize ? CheckResult.EXISTS : CheckResult.EXPIRED;
         }
         // filePutTime 单位为纳秒
         boolean isExpired = destFile.lastModified() * 1000 < (filePutTime / 10000);
         // 不需要校验大小的（七牛图片瘦身功能，图片会无损压缩，大小会变化），磁盘文件写入时间晚于/等于七牛文件上传时间即为相同文件，否则为过期文件
         return isExpired ? CheckResult.EXPIRED : CheckResult.EXISTS;
    }

    /**
     * 文件是否属于图片瘦身类型
     * @param fileKey
     * @return
     */
    public static boolean isSlimType(String fileKey) {
        Assert.notNull(fileKey, "$$$ 文件 key 不能为 null！");
        String ext = FilenameUtils.getExtension(fileKey);
        return ArrayUtils.contains(QiNiuConstants.CDN_PHOTO_SLIM_TYPES, ext.toLowerCase());
    }
}