package com.zerocode.constant;

import java.io.File;

/**
 * 应用常量
 */
public interface AppConstant {

    public static final Integer DEFAULT_PRIORITY = 0;

    public static final Integer GOOD_PRIORITY = 99;

    public static final String APP_PATH = System.getProperty("user.dir") + File.separator + "tmp"+File.separator+"code";

    public static final String DEPLOY_PATH = System.getProperty("user.dir") + File.separator + "tmp"+File.separator+"deploy";

    public static final String DEPLOY_HOST = "http://localhost";
}
