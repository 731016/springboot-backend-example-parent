package com.xiaofei.springbootbackendfileupload.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendfileupload.constant.FileConstant;
import com.xiaofei.springbootbackendfileupload.model.dto.file.FileDeleteRequest;
import com.xiaofei.springbootbackendfileupload.model.dto.file.PageFileRequest;
import com.xiaofei.springbootbackendfileupload.model.dto.file.UploadFileRequest;
import com.xiaofei.springbootbackendfileupload.model.emuns.FileUploadBizEnum;
import com.xiaofei.springbootbackendfileupload.model.vo.FileVO;
import com.xiaofei.springbootbackendfileupload.model.vo.PageFileVO;
import com.xiaofei.springbootinit.model.entity.User;
import com.xiaofei.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本地文件操作
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from
 */
@Slf4j
@Component
public class LocalFileManager {

    @Resource
    private MultipartProperties multipartProperties;

    @Resource
    private UserService userService;


    public BaseResponse<String> uploadToLocal(MultipartFile file, UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件内容为空");
        }
        String fileName = file.getOriginalFilename();
        String rawFileName = StrUtil.subBefore(fileName, ".", true);
        String fileType = StrUtil.subAfter(fileName, ".", true);
        User loginUser = userService.getLoginUser(request);
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String localFilePath = String.format("/%s/%s/%s", multipartProperties.getLocation(), loginUser.getId(), rawFileName + "-" + uuid + "." + fileType);
        File dest = new File(localFilePath);
        File parent = dest.getParentFile();
        // 一次性创建多级目录
        if (!parent.exists() && !parent.mkdirs()) {
            log.error("【创建父目录失败】路径：{}", parent.getAbsolutePath());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
        try {
            file.transferTo(new File(localFilePath));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }

        log.info("【文件上传至本地】绝对路径：{}", localFilePath);
        return ResultUtils.success(localFilePath);
    }

    /**
     * 查询文件列表（游标分页）
     *
     * @param pageFileRequest
     * @return
     */
    public PageFileVO listCurrentDirLocalUserAvatar(PageFileRequest pageFileRequest) {
        PageFileVO pageFileVO = new PageFileVO();

        String preFix = pageFileRequest.getPreFix();
        if (StringUtils.isBlank(preFix)) {
            preFix = StrUtil.addSuffixIfNot(multipartProperties.getLocation(), "/");
        } else {
            //第一次搜索
            if (!preFix.contains(multipartProperties.getLocation())) {
                preFix = StrUtil.addSuffixIfNot(multipartProperties.getLocation(), "/") + preFix;
            }
        }
        Path dir = Paths.get(preFix);

        File rootDir = dir.toFile();
        if (!rootDir.exists()) {
            return pageFileVO;
        }
        // 所有文件按名字排序
        // 1. 当前目录下所有「文件」和「目录」
        List<File> all = List.of(FileUtil.ls(dir.toString()));

        String nextMarker = pageFileRequest.getNextMarker();
        int pageSize = pageFileRequest.getPageSize();
        // 游标定位
        int start = 0;
        if (StringUtils.isNotBlank(nextMarker)) {
            for (int i = 0; i < all.size(); i++) {
                File file = all.get(i);
                String name = file.getName();
                if (nextMarker.equals(name)) {
                    start = i + 1;
                    break;
                }
            }
        }
        int end = Math.min(start + pageSize, all.size());
        List<File> sub = all.subList(start, end);
        List<FileVO> files = sub.stream().map(f -> {
            String key = localPathReplace(f.getPath().toString());
            return FileVO.builder()
                    .key(removeTheRootDirectory(key))
                    .name(f.getName())
                    .path(removeTheRootDirectory(localPathReplace(dir.toString())))
                    .fileType(f.isDirectory() ? FileConstant.FILE_TYPE_DIR
                            : FileConstant.FILE_TYPE_FILE)
                    .fileSize(f.isFile() ? f.length() / 1024 : 0)
                    .lastModified(new Date(f.lastModified()))
                    .build();
        }).collect(Collectors.toList());

        String nextCursor = end < all.size() ? all.get(end - 1).getName() : null;
        boolean hasNext = end < all.size();

        pageFileVO.setFiles(files);
        pageFileVO.setCurrentMarker(nextMarker);
        pageFileVO.setNextMarker(nextCursor);
        pageFileVO.setHasNext(hasNext);
        pageFileVO.setPath(removeTheRootDirectory(localPathReplace(dir.toString())));

        return pageFileVO;
    }

    public boolean deleteRemoteUserAvatar(FileDeleteRequest fileDeleteRequest) {
        String key = fileDeleteRequest.getKey();
        Path filePath = localAddPrefix(key);
        return FileUtil.del(filePath.toFile());
    }

    /**
     * 替换本地问及那目录分隔符
     *
     * @param path
     * @return
     */
    private String localPathReplace(String path) {
        return path.replace("\\", "/");
    }

    /**
     * 去除根目录，防止访问其他目录路径
     *
     * @param path
     * @return
     */
    private String removeTheRootDirectory(String path) {
        return path.replace(multipartProperties.getLocation(), "");
    }

    /**
     * 增加本地目录路径
     *
     * @param key
     * @return
     */
    private Path localAddPrefix(String key) {
        Path file = Paths.get(multipartProperties.getLocation(), key).normalize();
        return file;
    }


    public byte[] downloadLocalUserAvatar(String key) {
        Path filePath = localAddPrefix(key);
        return FileUtil.readBytes(filePath);
    }
}
