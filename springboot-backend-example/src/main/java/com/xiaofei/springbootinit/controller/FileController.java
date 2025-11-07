package com.xiaofei.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.constant.FileConstant;
import com.xiaofei.springbootinit.exception.BusinessException;
import com.xiaofei.springbootinit.manager.CosManager;
import com.xiaofei.springbootinit.model.dto.file.UploadFileRequest;
import com.xiaofei.springbootinit.model.entity.User;
import com.xiaofei.springbootinit.model.enums.FileUploadBizEnum;
import com.xiaofei.springbootinit.service.UserService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
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


    @PostMapping("/downUrlFile")
    public BaseResponse<String> downUrlFile(@RequestParam String remoteUrl) {
        // 保存文件的本地路径
        String savePath = "src/main/resources/files/";

        try {
            // 创建保存文件的目录（如果不存在）
            File saveDir = new File(savePath).getParentFile();
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            // 打开连接
            URL url = new URL(remoteUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            //connection.setRequestProperty("fine_auth_token", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwidGVuYW50SWQiOiJkZWZhdWx0IiwiaXNzIjoiZmFucnVhbiIsImRlc2NyaXB0aW9uIjoidXNlcih1c2VyKSIsImV4cCI6MTc1MDQ0MDAwNCwiaWF0IjoxNzUwNDM2NDA0LCJqdGkiOiJXSUVweVlGc1JUOEptV0xSTmRtTnhGeVp3eTBQT0FWYzQzSFBiZTljNFhRSFJpZnYifQ.AQauaYPDLOQv1wskRLKK95Fke0yMTLXTRKNaxLj-YDo");


            // 尝试从响应头中获取文件名
            String fileName = getFileNameFromResponseHeader(connection);
            if (fileName == null || fileName.isEmpty()) {
                // 如果无法获取文件名，使用默认文件名
                fileName = "default_file.pdf";
            }

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                // 打印响应头
                log.info("Response Headers:");
                connection.getHeaderFields().forEach((k, v) -> log.info(k + ":" + v));

                // 检查Content-Type
                String contentType = connection.getContentType();
                log.info("Content-Type: " + contentType);

                if (!contentType.contains("pdf")) {
                    throw new IOException("服务器返回的内容类型不是PDF，实际类型为：" + contentType);
                }

                // 获取Content-Length（文件大小）
                long contentLength = connection.getContentLengthLong();
                log.info("文件大小：" + contentLength);

                String filePath = savePath + fileName;

                // 读取文件流
                try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                     BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filePath))) {

                    byte[] dataBuffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;

                    while ((bytesRead = in.read(dataBuffer, 0, 4096)) != -1) {
                        out.write(dataBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                    }

                    // 检查文件头
//                    try (FileInputStream fis = new FileInputStream(savePath)) {
//                        byte[] fileHeader = new byte[5];
//                        fis.read(fileHeader);
//                        String header = new String(fileHeader);
//                        if (!header.startsWith("%PDF-")) {
//                            throw new IOException("文件内容不正确，不是有效的PDF文件");
//                        }
//                    }

                    log.info("Total bytes read: " + totalBytesRead);

                    if (contentLength > 0 && totalBytesRead != contentLength) {
                        throw new IOException("文件下载不完整，预期大小：" + contentLength + "，实际大小：" + totalBytesRead);
                    }
                    return ResultUtils.success("文件下载成功，保存路径为：" + savePath);
                }
            } else {
                log.error("服务器响应错误，响应码：" + responseCode);
            }
        } catch (IOException e) {
            log.error("文件下载失败：" + e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "文件下载失败");
    }

    /**
     * 从HTTP响应头中获取文件名
     *
     * @param connection HttpURLConnection对象
     * @return 文件名，如果无法获取则返回null
     */
    private String getFileNameFromResponseHeader(HttpURLConnection connection) {
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null) {
            // 使用正则表达式提取文件名
            Pattern pattern = Pattern.compile("filename=\"([^\"]+)\"|filename=([^;]+)");
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            }
        }
        return null;
    }
}
