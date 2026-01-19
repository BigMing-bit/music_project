package com.pang.controller.common;

import com.pang.common.Result;
import com.pang.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@RestController
public class UploadController {

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    /**
     * 上传文件接口
     * @param file 用户上传的文件
     * @return Result 返回上传结果，包含文件的访问URL
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("上传文件：{}", file.getOriginalFilename());

        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择文件");
        }
        // 调用OSS上传方法
        String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
        log.info("文件上传至OSS成功，URL: {}", url);

        return Result.success("上传成功", url);
    }
}
