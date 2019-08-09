package com.sherlocky.qiniusyncnas.qiniu.service.impl;

import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.BucketInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
import com.qiniu.util.Base64;
import com.sherlocky.qiniusyncnas.qiniu.config.QiNiuProperties;
import com.sherlocky.qiniusyncnas.qiniu.constant.QiNiuConstants;
import com.sherlocky.qiniusyncnas.qiniu.constant.QiniuOrigin;
import com.sherlocky.qiniusyncnas.qiniu.service.IQiniuService;
import com.sherlocky.qiniusyncnas.util.QiniuFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * service实现类
 */
@Slf4j
@Service
public class QiniuServiceImpl implements IQiniuService {
    @Autowired
    private UploadManager uploadManager;
    @Autowired
    private BucketManager bucketManager;
    @Autowired
    private Auth auth;
    @Autowired
    private QiNiuProperties qiNiuProperties;

    @Override
    public Response uploadFile(File file, String key, boolean existed) throws QiniuException {
        Response response;
        // 覆盖上传
        if (existed) {
            response = this.uploadManager.put(file, key, getUploadToken(key));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("### 使用文件上传");
            }
            response = this.uploadManager.put(file, key, getUploadToken());
            int retry = 0;
            while (response.needRetry() && retry < 3) {
                response = this.uploadManager.put(file, key, getUploadToken());
                retry++;
            }
        }

        return response;
    }

    @Override
    public Response uploadFile(String filePath, String key, boolean existed) throws QiniuException {
        Response response;
        // 覆盖上传
        if (existed) {
            response = this.uploadManager.put(filePath, key, getUploadToken(key));
        } else {
            response = this.uploadManager.put(filePath, key, getUploadToken());
            int retry = 0;
            while (response.needRetry() && retry < 3) {
                response = this.uploadManager.put(filePath, key, getUploadToken());
                retry++;
            }
        }

        return response;
    }

    @Override
    public void deleteFile(String key) throws QiniuException {
        bucketManager.delete(qiNiuProperties.getBucketName(), key);
    }

    @Override
    public FileListing listFile(String marker, int limit) throws QiniuException {
        return listFile(null, marker, limit, null);
    }

    @Override
    public FileListing listFile(String prefix, String marker, int limit, String delimiter) throws QiniuException {
        return bucketManager.listFilesV2(qiNiuProperties.getBucketName(), prefix, marker, limit, delimiter);
    }

    @Override
    public String getDomain() {
        String[] domains = null;
        try {
            // 不带协议头
            domains = bucketManager.domainList(qiNiuProperties.getBucketName());
        } catch (QiniuException e) {
            log.error("$$$$$$ 获取域名列表失败！", e);
        }
        if (ArrayUtils.isEmpty(domains)) {
            return null;
        }
        return domains[0];
    }

    @Override
    public String getDownloadUrl(String fileKey) {
        String domain = this.getDomain();
        Assert.notNull(domain, "$$$ 外链域名不能为 null！");
        Assert.notNull(fileKey, "$$$ 文件 key 不能为 null！");
        String encodedFileKey = fileKey;
        try {
            encodedFileKey = URLEncoder.encode(fileKey, "utf-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error(String.format("$$$ 文件key: %s转换编码错误!", fileKey), e);
        }
        // 添加时间戳参数，覆盖缓存，取最新的文件
        String publicUrl = String.format("%s/%s", domain, encodedFileKey);
        BucketInfo bi = null;
        try {
            bi = bucketManager.getBucketInfo(qiNiuProperties.getBucketName());
        } catch (QiniuException e) {
            log.error("$$$ 获取七牛Bucket信息失败！", e);
        }
        if (bi == null) {
            return null;
        }
        // 公开空间且未开启原图保护
        if (QiNiuConstants.BUCKET_PUBLIC == bi.getPrivate() && QiNiuConstants.BUCKET_PUBLIC == bi.getProtected()) {
            /**
             * 如果是空间开启了图片瘦身，需要从七牛的下载源站代理下载才能得到原图
             */
            if (qiNiuProperties.isCdnPhotoSlim() && QiniuFileUtils.isSlimType(fileKey)) {
                return getSlimDownloadUrl(bi, domain, encodedFileKey);
            }
            return publicUrl;
        }
        /**
         * 对于私有空间文件或者开启了原图保护的公开空间文件，
         * 首先需要按照公开空间的文件访问方式构建对应的公开空间访问链接，然后再对这个链接进行私有授权签名
         */
        // 自定义链接过期时间: 5分钟（300秒）
        return auth.privateDownloadUrl(publicUrl, 300);
    }

    /**
     * 获取瘦身后的图片类型
     * @param fileKey
     * @return
     */
    private String getSlimDownloadUrl(BucketInfo bi, String cdnDomain, String fileKey) {
        /**
         * 如果是空间开启了图片瘦身，需要从七牛的下载源站代理下载才能得到原图（图片瘦身在cdn的客户->cdn边缘节点->中间源->源站中是由中间源做的）。
         * 上传下载域名参考：
         * https://developer.qiniu.com/kodo/kb/5869/store-uploads-and-downloads-the-domain-name
         * 下载方式（额外收费-源站访问拉取收费标准0.29元/GB）：
         * http://源站域名/文件名称 -H ’host:cdn域名‘
         */
        QiniuOrigin origin = QiniuOrigin.get(bi.getZone());
        if (origin == null) {
            log.error("$$$ 找不到对应的七牛源站——{}！", bi.getZone());
            return null;
        }
        String originDomain = origin.getDownloadDomain();
        if (log.isDebugEnabled()) {
            log.debug("### {} 对应的七牛源站下载域名：{}", bi.getZone(), originDomain);
        }
        // 去源站下载需要添加一个header，内容为：无协议头的cdn域名
        return String.format("%s/%s -H host:%s", originDomain, fileKey, cdnDomain);
    }

    public static void main(String[] args) {
        String dest = "cWluaXVwaG90b3M6Z29nb3BoZXIuanBn";
        String ss = new QiniuServiceImpl().urlsafeBase64Encode("qiniuphotos:gogopher.jpg");
        System.out.println("["+ss+"]");
        System.out.println("["+dest+"]");
        System.out.println(dest.equalsIgnoreCase(ss));
        Assert.isTrue(dest.equalsIgnoreCase(ss), "$$$ 七牛urlsafe base64编码校验失败！");
    }

    private String urlsafeBase64Encode(String original) {
        try {
            return StringUtils.trim(Base64.encodeToString(new String(original).getBytes("UTF-8"), Base64.URL_SAFE));
        } catch (UnsupportedEncodingException e) {
            log.error("$$$ 转换编码失败！", e);
        }
        return null;
    }

    /**
     * 获取上传凭证，普通上传
     */
    @Override
    public String getUploadToken() {
        return this.auth.uploadToken(qiNiuProperties.getBucketName());
    }

    /**
     * 获取上传凭证，覆盖上传
     */
    private String getUploadToken(String fileName) {
        return this.auth.uploadToken(qiNiuProperties.getBucketName(), fileName);
    }

    /**
     * 这个注解在这里没实际用处，就是为了方便在该类构造完成后打印日志，看看配置信息是否加载到配置类中了
     */
    @PostConstruct
    public void init() {
        log.info("qiNiuProperties: {}", JSON.toJSONString(qiNiuProperties));
    }
}
