package com.xiaofei.springbootbackendfileupload.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendfileupload.manager.CosManager;
import com.xiaofei.springbootbackendfileupload.manager.LocalFileManager;
import com.xiaofei.springbootbackendfileupload.model.dto.file.FileDeleteRequest;
import com.xiaofei.springbootbackendfileupload.model.dto.file.FileDownloadRequest;
import com.xiaofei.springbootbackendfileupload.model.dto.file.PageFileRequest;
import com.xiaofei.springbootbackendfileupload.model.dto.file.UploadFileRequest;
import com.xiaofei.springbootbackendfileupload.model.vo.FileVO;
import com.xiaofei.springbootbackendfileupload.model.vo.PageFileVO;
import com.xiaofei.springbootinit.annotation.AuthCheck;
import com.xiaofei.springbootinit.constant.UserConstant;
import com.xiaofei.springbootinit.example.interfaceaop.annotation.ApiLog;
import com.xiaofei.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Resource
    private LocalFileManager localFileManager;


    @ApiLog(value = "上传文件到本地")
    @PostMapping(value = "/uploadToLocal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<String> uploadToLocal(@RequestParam("file") MultipartFile file, @RequestPart("uploadFileRequest") UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        return localFileManager.uploadToLocal(file, uploadFileRequest, request);
    }

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @ApiLog(value = "上传文件到远程")
    @PostMapping("/uploadToRemote")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           @RequestPart("uploadFileRequest") UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        return cosManager.uploadFileToRemote(multipartFile, uploadFileRequest, request);
    }

    /**
     * 查询所有用户头像
     *
     * @param pageFileRequest
     * @param request
     * @return
     */
    @ApiLog(value = "查询所有远程用户头像")
    @PostMapping("/listCurrentDirRemoteUserAvatar")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageFileVO> listCurrentDirRemoteUserAvatar(@RequestBody PageFileRequest pageFileRequest,
                                                                   HttpServletRequest request) {
        PageFileVO pageFileVO = cosManager.listCurrentDirRemoteUserAvatar(pageFileRequest);
        return ResultUtils.success(pageFileVO);
    }

    /**
     * 查询文件所有文件
     *
     * @param pageFileRequest
     * @param request
     * @return
     */
    @ApiLog(value = "查询所有本地用户头像")
    @PostMapping("/listCurrentDirLocalUserAvatar")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageFileVO> listCurrentDirLocalUserAvatar(@RequestBody PageFileRequest pageFileRequest,
                                                                  HttpServletRequest request) {
        PageFileVO pageFileVO = localFileManager.listCurrentDirLocalUserAvatar(pageFileRequest);
        return ResultUtils.success(pageFileVO);
    }

    /**
     * 根据当前文件key查询tag
     *
     * @param pageFileRequest
     * @param request
     * @return
     */
    @ApiLog(value = "查询当前远程用户头像tag")
    @PostMapping("/queryCurrentRemoteUserAvatarTags")
    public BaseResponse<FileVO> queryCurrentRemoteUserAvatarTags(@RequestBody PageFileRequest pageFileRequest,
                                                                 HttpServletRequest request) {
        FileVO fileVO = cosManager.queryCurrentRemoteUserAvatarTags(pageFileRequest.getKey());
        return ResultUtils.success(fileVO);
    }


    /**
     * 删除文件
     *
     * @param fileDeleteRequest
     * @param request
     * @return
     */
    @ApiLog(value = "删除远程用户头像")
    @DeleteMapping("/deleteRemoteUserAvatar")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> deleteRemoteUserAvatar(@RequestBody FileDeleteRequest fileDeleteRequest,
                                                 HttpServletRequest request) {
        cosManager.deleteRemoteUserAvatar(fileDeleteRequest);
        return ResultUtils.success("删除成功");
    }

    @ApiLog(value = "删除本地用户头像")
    @DeleteMapping("/deleteLocalUserAvatar")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> deleteLocalUserAvatar(@RequestBody FileDeleteRequest fileDeleteRequest,
                                                       HttpServletRequest request) {
        localFileManager.deleteRemoteUserAvatar(fileDeleteRequest);
        return ResultUtils.success("删除成功");
    }

    /**
     * 下载文件
     *
     * @param fileDownloadRequest
     * @param request
     * @return
     */
    @ApiLog(value = "下载远程用户头像")
    @PostMapping("/downloadRemoteUserAvatar")
    public BaseResponse<String> downloadRemoteUserAvatar(@RequestBody FileDownloadRequest fileDownloadRequest,
                                                   HttpServletRequest request, HttpServletResponse response) {
        byte[] bytes = cosManager.downloadRemoteUserAvatar(fileDownloadRequest);
        response.setContentType("application/octet-stream");
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileDownloadRequest.getKey(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("下载失败", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
        response.setContentLength(bytes.length);
        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            log.error("下载失败", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }

        return ResultUtils.success("下载成功");
    }

    /**
     * 下载本地文件
     *
     * @param fileDownloadRequest
     * @param request
     * @param response
     * @return
     */
    @ApiLog(value = "下载本地用户头像")
    @PostMapping("/downloadLocalUserAvatar")
    public BaseResponse<String> downloadLocalUserAvatar(@RequestBody FileDownloadRequest fileDownloadRequest,
                                                        HttpServletRequest request, HttpServletResponse response) {
        byte[] bytes = localFileManager.downloadLocalUserAvatar(fileDownloadRequest.getKey());
        response.setContentType("application/octet-stream");
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileDownloadRequest.getKey(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("下载失败", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
        response.setContentLength(bytes.length);
        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            log.error("下载失败", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }

        return ResultUtils.success("下载成功");
    }


    /**
     * 测试下载帆软PDF报表使用
     *
     * @param remoteUrl
     * @return
     */
    @ApiLog(value = "测试下载帆软PDF报表使用")
    @PostMapping("/downFileByRemoteUrl")
    public BaseResponse<String> downFileByRemoteUrl(@RequestParam String remoteUrl) {
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
