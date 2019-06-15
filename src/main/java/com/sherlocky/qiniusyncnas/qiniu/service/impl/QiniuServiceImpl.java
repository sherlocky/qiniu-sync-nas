package com.sherlocky.qiniusyncnas.qiniu.service.impl;

import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
import com.sherlocky.qiniusyncnas.qiniu.config.QiNiuProperties;
import com.sherlocky.qiniusyncnas.qiniu.constant.QiNiuConstants;
import com.sherlocky.qiniusyncnas.qiniu.service.IQiniuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;

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
            domains = bucketManager.domainList(qiNiuProperties.getBucketName());
        } catch (QiniuException e) {
            log.error("$$$$$$ 获取域名列表失败！", e);
        }
        if (ArrayUtils.isEmpty(domains)) {
            return null;
        }
        return String.format("%s%s%s", qiNiuProperties.getCdnSchema(), QiNiuConstants.SCHEMA_SEPARATOR, domains[0]);
    }

    @Override
    public String getDownloadUrl(String fileKey) {
        String domain = this.getDomain();
        Assert.notNull(domain, "$$$ 外链域名不能为 null！");
        Assert.notNull(fileKey, "$$$ 文件 key 不能为 null！");
        // 添加时间戳参数，覆盖缓存，取最新的文件
        return String.format("%s/%s", domain, fileKey);
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
