package com.sherlocky.qiniusyncnas;

import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.BucketInfo;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.sherlocky.qiniusyncnas.entity.SyncResult;
import com.sherlocky.qiniusyncnas.qiniu.config.QiNiuProperties;
import com.sherlocky.qiniusyncnas.qiniu.service.IQiniuService;
import com.sherlocky.qiniusyncnas.service.QiniuSyncNasService;
import com.sherlocky.qiniusyncnas.util.QiniuFileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("prod")
public class QiniuSyncNasApplicationTests {

    @Autowired
    private IQiniuService qiniuService;
    @Autowired
    private QiniuSyncNasService qiniuSyncNasService;
    @Autowired
    private BucketManager bucketManager;
    @Autowired
    private QiNiuProperties qiNiuProperties;

    @Test
    public void testBucketManager() {
        try {
            BucketInfo bi = bucketManager.getBucketInfo(qiNiuProperties.getBucketName());
            System.out.println(JSON.toJSONString(bi));
            String[] ds = bucketManager.domainList(qiNiuProperties.getBucketName());
            System.out.println(JSON.toJSONString(ds));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListFiles() {
        //文件名前缀
        String prefix = null;//""f/c9/";//"backup/db/db_ghost-blog-new-";
        //每次迭代的长度限制，推荐值 10000
        int limit = 100;
        //指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
        String delimiter = "";

        FileListing fl = null;
        String marker = null;
        boolean isEOF = false;
        try {
            while (!isEOF) {
                fl = qiniuService.listFile(prefix, marker, 300, delimiter);
                isEOF = fl.isEOF();
                marker = fl.marker;
                System.out.println(JSON.toJSONString(fl));
                FileInfo[] fis = fl.items;
                Arrays.stream(fis).forEach((fileInfo) -> {
                    System.out.println(QiniuFileUtils.getFilePath(fileInfo.key));
                });
            }
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        System.out.println("### 列举空间文件列表结束");
    }

    @Test
    public void testSync() {
        SyncResult r = qiniuSyncNasService.sync();
        Assert.assertNotNull(r);
    }

    @Test
    public void testFileUtilsGetLocation() {
        System.out.println(QiniuFileUtils.getLocationPath());
    }

    @Test
    public void testFileUtilsDownload() throws IOException {
        String fileKey = "f/3e/cc3cf5b3902dbb1ff2356fe65e67e.png";
        long fileSize = 124925;
        long filePutTime = 15094987913727947L;

        //fileSize = 4695;
        //fileKey = "static/autoc/js/autoc.min.js";
        //filePutTime = 14608713791557831L;

        fileKey = "0/90/73d58da3592f5ada5d5f690061145.jpg";
        fileSize = 2198680;
        filePutTime = 14598736966795381L;

        fileKey = "f/c9/77aeadb43ef29dd86aca81337a8a4.png";
        fileSize = 43453;
        filePutTime = 15680943262407266L;

        String downloadUrl = qiniuService.getDownloadUrl(fileKey, null);
        System.out.println(downloadUrl);
        boolean isSuccess = QiniuFileUtils.downloadFile(downloadUrl, fileKey, fileSize, filePutTime);
        System.out.println("下载成功~" + QiniuFileUtils.getFilePath(fileKey));
    }
}
