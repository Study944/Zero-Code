package com.zerocode.constant;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;

/**
 * 应用常量
 */
public interface AppConstant {

    Integer DEFAULT_PRIORITY = 0;

    Integer GOOD_PRIORITY = 99;

    String APP_PATH = System.getProperty("user.dir") + File.separator + "tmp"+File.separator+"code";

    String DEPLOY_PATH = System.getProperty("user.dir") + File.separator + "tmp"+File.separator+"deploy";

    @Value("${code.deploy-host}")
    String DEPLOY_HOST = "http://localhost";
}
