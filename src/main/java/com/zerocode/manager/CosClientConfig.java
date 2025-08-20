package com.zerocode.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CosClientConfig {

    @Value("${cos.client.secretId}")
    public String SECRET_ID;

    @Value("${cos.client.secretKey}")
    public String SECRET_KEY;

    @Value("${cos.client.region}")
    public String REGION;

    @Value("${cos.client.bucket}")
    public String BUCKET;

    @Value("${cos.client.host}")
    public String HOST;

    @Bean
    public COSClient cosClient() {
        // 1. 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(SECRET_ID, SECRET_KEY);
        // 2. 指定地域
        Region region = new Region(REGION);
        ClientConfig clientConfig = new ClientConfig(region);
        // 3. 生成 cos 客户端
        return new COSClient(cred, clientConfig);
    }
}
