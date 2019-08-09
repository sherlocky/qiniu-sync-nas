package com.sherlocky.qiniusyncnas.qiniu.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 校验类
 * <p>此处使用 Condition 实现，并不优美。。</p>
 */
public class QiNiuCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        if (StringUtils.isEmpty(env.getProperty("qiniu.access-key"))) {
            throw new RuntimeException("Lack of qiniuyun configuration: qiniu.access-key");
        } else if (StringUtils.isEmpty(env.getProperty("qiniu.secret-key"))) {
            throw new RuntimeException("Lack of qiniuyun configuration: qiniu.secret-key");
        } else if (StringUtils.isEmpty(env.getProperty("qiniu.bucket-name"))) {
            throw new RuntimeException("Lack of qiniuyun configuration: qiniu.bucket-name");
        } else {
            return true;
        }
    }
}
