package com.xiaofei.springbootbackendelasticsearch.job;

import cn.hutool.core.collection.CollUtil;
import com.xiaofei.springbootbackendelasticsearch.esdao.UserEsDao;
import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsDTO;
import com.xiaofei.springbootinit.mapper.PostMapper;
import com.xiaofei.springbootinit.mapper.UserMapper;
import com.xiaofei.springbootinit.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增量同步用户到 es
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
// todo 取消注释开启任务
@Component
@Slf4j
public class SyncUserToEs {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserEsDao userEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<User> postList = userMapper.listPostWithDelete(fiveMinutesAgoDate);
        if (CollUtil.isEmpty(postList)) {
            log.info("no inc post");
            return;
        }
        List<UserEsDTO> userEsDTOList = postList.stream()
                .map(UserEsDTO::objToDto)
                .collect(Collectors.toList());
        final int pageSize = 500;
        int total = userEsDTOList.size();
        log.info("SyncUserToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            userEsDao.saveAll(userEsDTOList.subList(i, end));
        }
        log.info("SyncUserToEs end, total {}", total);
    }
}
