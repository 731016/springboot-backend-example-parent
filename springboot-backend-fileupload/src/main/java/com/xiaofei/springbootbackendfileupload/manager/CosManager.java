package com.xiaofei.springbootbackendfileupload.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.Tag.Tag;
import com.qcloud.cos.utils.IOUtils;
import com.xiaofei.springbootbackendfileupload.config.CosClientConfig;
import com.xiaofei.springbootbackendfileupload.constant.FileConstant;
import com.xiaofei.springbootbackendfileupload.model.dto.file.FileDeleteRequest;
import com.xiaofei.springbootbackendfileupload.model.dto.file.FileDownloadRequest;
import com.xiaofei.springbootbackendfileupload.model.dto.file.PageFileRequest;
import com.xiaofei.springbootbackendfileupload.model.dto.file.UploadFileRequest;
import com.xiaofei.springbootbackendfileupload.model.emuns.FileUploadBizEnum;
import com.xiaofei.springbootbackendfileupload.model.vo.FileVO;
import com.xiaofei.springbootbackendfileupload.model.vo.PageFileVO;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootinit.model.entity.User;
import com.xiaofei.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Cos 对象存储操作
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from
 */
@Slf4j
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    @Resource
    private UserService userService;

    /**
     * 上传对象
     *
     * @param key           唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
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

    public BaseResponse<String> uploadFileToRemote(MultipartFile multipartFile,
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
            putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(cosClientConfig.getHost() + filepath);
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
     * 根据当前文件key查询tag
     *
     * @param key
     * @return
     */
    public FileVO queryCurrentRemoteUserAvatarTags(String key) {
        GetObjectTaggingRequest getObjectTaggingRequest = new GetObjectTaggingRequest(cosClientConfig.getBucket(), key);
        GetObjectTaggingResult getObjectTaggingResult = cosClient.getObjectTagging(getObjectTaggingRequest);
        List<Tag> resultTagSet = getObjectTaggingResult.getTagSet();
        FileVO fileVO = new FileVO();
        fileVO.setTags(resultTagSet);
        return fileVO;
    }

    /**
     * 查询当前目录下的文件
     *
     * @return
     */
    public PageFileVO listCurrentDirRemoteUserAvatar(PageFileRequest pageFileRequest) {
        String preFix = pageFileRequest.getPreFix();
        String nextMarker = pageFileRequest.getNextMarker();
        Integer maxKeys = pageFileRequest.getMaxKeys();
        if (StringUtils.isBlank(preFix)) {
            preFix = StrUtil.addSuffixIfNot(FileUploadBizEnum.USER_AVATAR.getValue(), "/");
        } else {
            //第一次搜索
            if (!preFix.contains(FileUploadBizEnum.USER_AVATAR.getValue())) {
                preFix = StrUtil.addSuffixIfNot(FileUploadBizEnum.USER_AVATAR.getValue(), "/") + preFix;
            }
        }
        List<FileVO> fileVOList = new ArrayList<>();

        ListObjectsRequest request = new ListObjectsRequest()
                .withBucketName(cosClientConfig.getBucket())
                // 只扫这个“目录”
                .withPrefix(preFix).withDelimiter("/")
                .withMarker(nextMarker)
                // 每页条数
                .withMaxKeys(maxKeys);
        log.info("文件名模糊搜索，参数：" + preFix);
        ObjectListing objectListing = null;
        try {
            objectListing = cosClient.listObjects(request);
        } catch (CosServiceException e) {
            log.error("用户头像查询失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户头像查询失败：" + e.getMessage());
        } catch (CosClientException e) {
            log.error("用户头像查询失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户头像查询失败：" + e.getMessage());
        }
        // object summary 表示所有列出的 object 列表
        List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
        //当前目录名称 + 子级的目录名称
        List<String> commonPrefixes = objectListing.getCommonPrefixes();

        //当前目录名称
        String prefix = objectListing.getPrefix();

        //找当前文件夹下的目录 commonPrefixes 去掉 prefix
        for (String commonPrefix : commonPrefixes) {
            String dirName = commonPrefix.replace(prefix, "");
            FileVO fileVO = new FileVO();
            fileVO.setName(dirName);
            fileVO.setPath(prefix);
            fileVO.setKey(commonPrefix);
            fileVO.setFileType(FileConstant.FILE_TYPE_DIR);
            fileVOList.add(fileVO);
        }

        //找当前文件夹下的文件 cosObjectSummaries key 去掉prefix 就是文件名
        for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
            // 文件的路径 key
            String key = cosObjectSummary.getKey();

            //以/结尾的是目录，不管
            if (StrUtil.endWith(key, "/")) {
                continue;
            }

            String userAvatar = String.format("%s/%s", cosClientConfig.getHost(), key);

            // 文件的 etag
            String etag = cosObjectSummary.getETag();
            // 文件的长度
            long fileSize = cosObjectSummary.getSize();
            // 文件的存储类型
            String storageClasses = cosObjectSummary.getStorageClass();

            FileVO fileVO = new FileVO();
            fileVO.setUserAvatar(userAvatar);
            fileVO.setKey(key);

            fileVO.setName(getFileName(key));
            fileVO.setPath(getFilePath(key));

            fileVO.setEtag(etag);
            fileVO.setFileSize(fileSize / 1024);
            fileVO.setStorageClasses(storageClasses);
            fileVO.setLastModified(cosObjectSummary.getLastModified());
            fileVO.setFileType(FileConstant.FILE_TYPE_FILE);
            fileVOList.add(fileVO);
        }
        PageFileVO pageFileVO = new PageFileVO();
        pageFileVO.setFiles(fileVOList);
        pageFileVO.setCurrentMarker(nextMarker);
        pageFileVO.setNextMarker(objectListing.getNextMarker());
        pageFileVO.setHasNext(StringUtils.isNotBlank(objectListing.getNextMarker()));
        //获取文件路径，最后一个/前面的
        pageFileVO.setPath(getFileName(objectListing.getPrefix()));

//        int total = listUserAvatarTotal(preFix);
//        pageFileVO.setTotal(total);

        return pageFileVO;
    }

    /**
     * 获取文件路径，不包含文件名，最后一个/前面的
     *
     * @param preFix
     * @return
     */
    private String getFilePath(String preFix) {
        List<String> filePathList = new ArrayList<>();
        if (StringUtils.isNotBlank(preFix)) {
            String[] keySplit = preFix.split("/");
            String fileName = "";
            for (int i = 0; i < keySplit.length; i++) {
                String s = keySplit[i];
                if (i < keySplit.length - 1) {
                    filePathList.add(s);
                }
            }
        }
        return StrUtil.addSuffixIfNot(StringUtils.join(filePathList, "/"), "/");
    }

    /**
     * 获取文件名称，不包含路径，最后一个/后面的
     *
     * @param key
     * @return
     */
    private String getFileName(String key) {
        String fileName = "";
        if (StringUtils.isNotBlank(key)) {
            String[] keySplit = key.split("/");
            for (int i = 0; i < keySplit.length; i++) {
                String s = keySplit[i];
                if (i == keySplit.length - 1) {
                    fileName = s;
                }
            }
        }
        return fileName;
    }


    /**
     * 根据key删除对象
     *
     * @param fileDeleteRequest
     */
    public void deleteRemoteUserAvatar(FileDeleteRequest fileDeleteRequest) {
        String key = fileDeleteRequest.getKey();
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 下载文件
     *
     * @param fileDeleteRequest
     */
    public byte[] downloadRemoteUserAvatar(FileDownloadRequest fileDeleteRequest) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), fileDeleteRequest.getKey());
        //如果存储桶开启了版本控制功能，需要下载指定版本的对象，可通过 setVersionId 函数指定对象的版本号
        //getObjectRequest.setVersionId("versionId");
        InputStream cosObjectInput = null;

        try {
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            cosObjectInput = cosObject.getObjectContent();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 处理下载到的流
        // 这里是直接读取，按实际情况来处理
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(cosObjectInput);
            return bytes;
        } catch (IOException e) {
            log.error("下载失败", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            // 用完流之后一定要调用 close()
            try {
                cosObjectInput.close();
            } catch (IOException e) {
                log.error("下载失败", e.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        }
    }

    /**
     * 根据目录，查询当前目录下的所有文件
     *
     * @param preFix
     * @return
     */
    public int listUserAvatarTotal(String preFix) {
        if (StringUtils.isBlank(preFix)) {
            preFix = FileUploadBizEnum.USER_AVATAR.getValue();
        }
        ObjectListing objectListing = null;
        ListObjectsRequest request = new ListObjectsRequest()
                .withBucketName(cosClientConfig.getBucket())
                // 只扫这个“目录”
                .withPrefix(StrUtil.addSuffixIfNot(preFix, "/"))
                //deliter 表示分隔符, 设置为/表示列出当前目录下的 object, 设置为空表示列出所有的 object
                .withDelimiter("/");
        try {
            objectListing = cosClient.listObjects(request);
        } catch (CosServiceException e) {
            log.error("用户头像查询失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户头像查询失败：" + e.getMessage());
        } catch (CosClientException e) {
            log.error("用户头像查询失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户头像查询失败：" + e.getMessage());
        }
        // common prefix 表示被 delimiter 截断的路径, 如 delimter 设置为/, common prefix 则表示所有子目录的路径
        List<String> commonPrefixs = objectListing.getCommonPrefixes();

        // object summary 表示所有列出的 object 列表，这里面没有目录，目录都在commonPrefixs里面
        List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries()
                .stream().filter(file -> !StrUtil.endWith(file.getKey(), "/")).toList();

        return commonPrefixs.size() + cosObjectSummaries.size();
    }
}
