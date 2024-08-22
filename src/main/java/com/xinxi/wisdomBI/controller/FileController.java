package com.xinxi.wisdomBI.controller;

import cn.hutool.core.io.FileUtil;
import com.xinxi.wisdomBI.common.BaseResponse;
import com.xinxi.wisdomBI.common.ErrorCode;
import com.xinxi.wisdomBI.common.ResultUtils;
import com.xinxi.wisdomBI.exception.BusinessException;
import com.xinxi.wisdomBI.model.enums.FileUploadBizEnum;

import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Resource;

import com.xinxi.wisdomBI.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *
 * @author 蒲月理想
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private  AliOSSUtils aliOSSUtils;
    /**
     * 上传图片
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/img")
    public BaseResponse<String> upload(@RequestPart("file")  MultipartFile file) throws IOException {

        if(file == null || file.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"上传文件为空");
        }
        // 校验文件：
        validFile(file,FileUploadBizEnum.USER_AVATAR);
        log.info("文件上传, 文件名: {}", file.getOriginalFilename());

        //调用阿里云OSS工具类进行文件上传
        String url = aliOSSUtils.upload(file);
        log.info("文件上传完成,文件访问的url: {}", url);
        // 返回上传成功的图片：
        return ResultUtils.success(url);
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
