package com.sherlocky.qiniusyncnas;

import com.sherlocky.qiniusyncnas.service.IQiniuService;
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

    @Test
    public void contextLoads() {
        System.out.println(qiniuService);
    }

}
