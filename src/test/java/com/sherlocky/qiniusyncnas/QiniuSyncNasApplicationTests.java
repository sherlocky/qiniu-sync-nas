package com.sherlocky.qiniusyncnas;

import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.FileListing;
import com.sherlocky.qiniusyncnas.qiniu.config.QiNiuProperties;
import com.sherlocky.qiniusyncnas.qiniu.service.IQiniuService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("prod")
public class QiniuSyncNasApplicationTests {

    @Autowired
    private IQiniuService qiniuService;
    @Autowired
    private BucketManager bucketManager;
    @Autowired
    private QiNiuProperties qiNiuProperties;

    @Test
    public void contextLoads() {
        System.out.println(qiniuService);
    }

    @Test
    public void testListFiles() {
        //文件名前缀
        String prefix = "";//"backup/db/db_ghost-blog-new-";
        //每次迭代的长度限制，最大1000，推荐值 1000
        int limit = 100;
        //指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
        String delimiter = "";

        FileListing fl = null;
        String marker = null;
        boolean isEOF = false;
        try {
            while (!isEOF) {
                fl = qiniuService.listFile(marker, limit);
                isEOF = fl.isEOF();
                marker = fl.marker;
                System.out.println(JSON.toJSONString(fl));
            }
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        System.out.println("### 列举空间文件列表结束");
        /*//列举空间文件列表
        FileInfo[] files = fl.items;
        System.out.println(JSON.toJSONString(files));*/
    }
}
