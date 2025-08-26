package com.zerocode.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;

/**
 * 对象存储上传工具类
 */
@Slf4j
@Component
public class CosManager {

    @Resource
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    public String uploadScreenshot(File file, String key) {
        // 1.甚至上传路径key
        PutObjectResult putObjectResult = uploadFile(file, key);
        if (putObjectResult != null){
            // 2.返回图片访问路径
            String url = cosClientConfig.getHOST() +File.separator+ key;
            log.info("上传成功：{}", url);
            return url;
        }
        return null;
    }

    public String uploadMermaidDiagram(File file, String key) {
        // 1.甚至上传路径key
        PutObjectResult putObjectResult = uploadFile(file, key);
        if (putObjectResult != null){
            // 2.返回图片访问路径
            String url = cosClientConfig.getHOST() +"/"+ key;
            log.info("上传成功：{}", url);
            return url;
        }
        return null;
    }

    /**
     * 图片文件上传
     * @param file
     * @param key
     * @return PutObjectResult
     */
    public PutObjectResult uploadFile(File file, String key) {
        // 1.获取存储桶名称
        String bucket = cosClientConfig.getBUCKET();
        // 2.创建上传请求
        PutObjectRequest request = new PutObjectRequest(bucket, key, file);
        // 3.上传
        PutObjectResult putObjectResult = cosClient.putObject(request);
        return putObjectResult;
    }

    public COSObject downloadFile(String url) {
        // 1.获取存储桶名称
        String bucket = cosClientConfig.getBUCKET();
        String key = url.substring(57);
        // 2.创建下载请求
        GetObjectRequest request = new GetObjectRequest(bucket, key);
        // 3.下载
        COSObject cosObject = cosClient.getObject(request);
        return cosObject;
    }

    /**
     * 图片删除
     */
    public boolean deleteFile(String url) {
        try {
            String bucket = cosClientConfig.getBUCKET();
            String key = url.substring(57);
            cosClient.deleteObject(bucket, key);
            return true; // 删除成功
        } catch (CosServiceException e) {
            System.err.println("删除失败: " + e.getMessage());
            return false; // 删除失败
        } catch (CosClientException e) {
            System.err.println("删除失败: " + e.getMessage());
            return false; // 删除失败
        }
    }

}
