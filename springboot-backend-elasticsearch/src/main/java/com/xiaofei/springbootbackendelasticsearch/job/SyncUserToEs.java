package com.xiaofei.springbootbackendelasticsearch.job;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaofei.springbootbackendelasticsearch.esdao.UserEsDao;
import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsDTO;
import com.xiaofei.springbootinit.mapper.PostMapper;
import com.xiaofei.springbootinit.mapper.UserMapper;
import com.xiaofei.springbootinit.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户同步到es
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<User> userList = userMapper.listPostWithDelete(fiveMinutesAgoDate);
        if (CollUtil.isEmpty(userList)) {
            log.info("no inc post");
            return;
        }
        List<UserEsDTO> userEsDTOList = userList.stream()
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

    /**
     * 增量删除已删除的用户
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void deleteDeletedUsers() {
        try {
            // 获取上次同步删除的时间
            String lastSyncTimeStr = null;
            Date minUpdateTime;
            if (lastSyncTimeStr == null || lastSyncTimeStr.isEmpty()) {
                // 如果Redis中没有记录，则查询近5分钟内的数据（首次运行）
                minUpdateTime = new Date(new Date().getTime() - 5 * 60 * 1000L);
                log.info("首次运行删除任务，查询近5分钟内的已删除用户");
            } else {
                // 使用上次同步的时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                minUpdateTime = sdf.parse(lastSyncTimeStr);
                log.info("增量删除任务，查询 {} 之后的已删除用户", lastSyncTimeStr);
            }

            // 查询已删除的用户（增量查询）
            List<User> deletedUserList = userMapper.listDeletedUsers(minUpdateTime);
            
            if (CollUtil.isEmpty(deletedUserList)) {
                log.info("没有需要删除的用户");
                return;
            }

            // 从ES中删除这些用户
            List<Long> userIds = deletedUserList.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            int total = userIds.size();
            log.info("开始删除ES中的用户，总数: {}", total);

            // 批量删除
            final int pageSize = 500;
            int successCount = 0;
            for (int i = 0; i < total; i += pageSize) {
                int end = Math.min(i + pageSize, total);
                List<Long> batchIds = userIds.subList(i, end);
                log.info("删除ES用户批次: {} 到 {}", i, end);
                
                // 批量删除
                for (Long userId : batchIds) {
                    try {
                        userEsDao.deleteById(userId);
                        successCount++;
                    } catch (Exception e) {
                        log.warn("删除ES用户失败，userId: {}, error: {}", userId, e.getMessage());
                    }
                }
            }

            // 更新上次同步时间（使用本次查询到的最大updateTime）
            // 只有在成功删除数据后才更新同步时间
            if (successCount > 0) {
                Date maxUpdateTime = deletedUserList.stream()
                        .map(User::getUpdateTime)
                        .max(Date::compareTo)
                        .orElse(new Date());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                log.info("删除ES用户完成，总数: {}, 成功: {}, 下次同步时间: {}", total, successCount, sdf.format(maxUpdateTime));
            } else {
                log.warn("删除ES用户完成，但没有成功删除任何用户");
            }
        } catch (Exception e) {
            log.error("删除ES用户任务执行失败", e);
        }
    }
}
