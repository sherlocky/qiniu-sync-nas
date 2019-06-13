package com.sherlocky.qiniusyncnas.service.impl;

import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.sherlocky.qiniusyncnas.qiniu.config.QiNiuProperties;
import com.sherlocky.qiniusyncnas.service.IQiniuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * service实现类
 */

@Service
public class QiniuServiceImpl implements IQiniuService {
    private static final Logger logger = LoggerFactory.getLogger(QiniuServiceImpl.class);

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
            System.out.println("使用文件上传");
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
        logger.info("qiNiuProperties: {}", JSON.toJSONString(qiNiuProperties));
    }
}
