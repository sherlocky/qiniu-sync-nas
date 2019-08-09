package com.sherlocky.qiniusyncnas.qiniu.constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 七牛源站信息（上传/下载地址等）
 * @author: zhangcx
 * @date: 2019/8/8 19:33
 */
@Data
@AllArgsConstructor
public class QiniuOrigin {
    /** 存储区域 */
    private String zone;
    /** 地域简称 */
    private String zoneCode;
    /** 服务器端上传域名 */
    private String serverUploadDomain;
    /** 客户端上传域名 */
    private String clientUploadDomain;
    /** 下载域名 */
    private String downloadDomain;

    private static Map<String, QiniuOrigin> ZONES = new HashMap<>();

    static {
        ZONES.put("z0", new QiniuOrigin("华东", "z0",
                "http://up.qiniup.com", "http://upload.qiniup.com",
                "http://iovip.qbox.me"));
        ZONES.put("z1", new QiniuOrigin("华北", "z1",
                "http://up-z1.qiniup.com", "http://upload-z1.qiniup.com",
                "http://iovip-z1.qbox.me"));
        ZONES.put("z2", new QiniuOrigin("华南", "z2",
                "http://up-z2.qiniup.com", "http://upload-z2.qiniup.com",
                "http://iovip-z2.qbox.me"));
        ZONES.put("na0", new QiniuOrigin("北美", "na0",
                "http://up-na0.qiniup.com", "http://upload-na0.qiniup.com",
                "http://iovip-na0.qbox.me"));
        ZONES.put("as0", new QiniuOrigin("东南亚", "as0",
                "http://up-as0.qiniup.com", "http://upload-as0.qiniup.com",
                "http://iovip-as0.qbox.me"));
    }

    public static QiniuOrigin get(String zoneCode) {
        return (QiniuOrigin) MapUtils.getObject(ZONES, zoneCode);
    }
}
