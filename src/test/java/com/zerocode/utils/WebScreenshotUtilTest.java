package com.zerocode.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScreenshotUtilTest {

    @Test
    void takeScreenshot() {
        WebScreenshotUtil.takeScreenshot("http://localhost:8111/static/vue_project_315239888158633984/");
    }
}