package com.xiaofei.springbootbackendelasticsearch.job;

import cn.hutool.core.collection.CollUtil;
import com.xiaofei.springbootbackendelasticsearch.model.dto.ApiLogEsDTO;
import com.xiaofei.springbootinit.mapper.ApiLogMapper;
import com.xiaofei.springbootinit.model.entity.ApiLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 增量同步 API 日志到 ES
 * 每 30 秒执行一次
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
@Component
@Slf4j
public class SyncApiLogToEs {

    @Resource
    private ApiLogMapper apiLogMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * API 日志同步到 ES
     * 每 30 秒执行一次
     */
    @Scheduled(fixedRate = 30 * 1000)
    public void run() {
        try {
            // 获取上次同步的时间
            String lastSyncTimeStr = null;
            Date minCreateTime;
            
            if (lastSyncTimeStr == null || lastSyncTimeStr.isEmpty()) {
                // 如果Redis中没有记录，则查询近1分钟内的数据（首次运行）
                minCreateTime = new Date(new Date().getTime() - 60 * 1000L);
                log.info("首次运行API日志同步任务，查询近1分钟内的日志");
            } else {
                // 使用上次同步的时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                minCreateTime = sdf.parse(lastSyncTimeStr);
                log.info("增量同步API日志，查询 {} 之后的日志", lastSyncTimeStr);
            }

            // 查询需要同步的日志（增量查询）
            List<ApiLog> apiLogList = apiLogMapper.listApiLogsAfterTime(minCreateTime);
            
            if (CollUtil.isEmpty(apiLogList)) {
                log.info("没有需要同步的API日志");
                return;
            }

            // 转换为 ES DTO
            List<ApiLogEsDTO> apiLogEsDTOList = apiLogList.stream()
                    .map(ApiLogEsDTO::objToDto)
                    .collect(Collectors.toList());

            int total = apiLogEsDTOList.size();
            log.info("SyncApiLogToEs start, total {}", total);

            // 按日期分组，因为不同日期的日志需要保存到不同的索引
            Map<String, List<ApiLogEsDTO>> groupedByDate = apiLogEsDTOList.stream()
                    .collect(Collectors.groupingBy(dto -> {
                        Date createTime = dto.getCreateTime() != null ? dto.getCreateTime() : new Date();
                        return ApiLogEsDTO.getIndexName(createTime);
                    }));

            // 按索引分组批量保存
            final int pageSize = 500;
            int successCount = 0;
            Date maxCreateTime = null;

            for (Map.Entry<String, List<ApiLogEsDTO>> entry : groupedByDate.entrySet()) {
                String indexName = entry.getKey();
                List<ApiLogEsDTO> logsForIndex = entry.getValue();
                IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);

                // 确保索引存在且映射正确
                if (!elasticsearchRestTemplate.indexOps(indexCoordinates).exists()) {
                    // 创建动态索引
                    elasticsearchRestTemplate.indexOps(indexCoordinates).create();
                    
                    // 从 ApiLogEsDTO 类获取映射信息并应用到动态索引
                    // 这样可以确保 @Field 注解中的 date 类型映射被正确应用
                    Document mapping = elasticsearchRestTemplate.indexOps(ApiLogEsDTO.class).createMapping();
                    elasticsearchRestTemplate.indexOps(indexCoordinates).putMapping(mapping);
                    
                    log.info("创建索引并设置映射: {}", indexName);
                }

                // 分批保存
                for (int i = 0; i < logsForIndex.size(); i += pageSize) {
                    int end = Math.min(i + pageSize, logsForIndex.size());
                    List<ApiLogEsDTO> batch = logsForIndex.subList(i, end);
                    log.info("同步API日志到索引 {}，批次: {} 到 {}", indexName, i, end);
                    
                    try {
                        // 批量保存到 ES（同一索引可以批量保存）
                        for (ApiLogEsDTO dto : batch) {
                            try {
                                elasticsearchRestTemplate.save(dto, indexCoordinates);
                                successCount++;
                                
                                // 更新最大创建时间
                                if (dto.getCreateTime() != null) {
                                    if (maxCreateTime == null || dto.getCreateTime().after(maxCreateTime)) {
                                        maxCreateTime = dto.getCreateTime();
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("同步单条API日志到ES失败，id: {}, error: {}", dto.getId(), e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("同步API日志到ES失败，索引: {}, 批次: {} 到 {}, error: {}", 
                                indexName, i, end, e.getMessage());
                    }
                }
            }

            // 更新上次同步时间（使用本次查询到的最大createTime）
            // 只有在成功同步数据后才更新同步时间
            if (successCount > 0 && maxCreateTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                log.info("同步API日志到ES完成，总数: {}, 成功: {}, 下次同步时间: {}",
                        total, successCount, sdf.format(maxCreateTime));
            } else {
                log.warn("同步API日志到ES完成，但没有成功同步任何日志");
            }
        } catch (Exception e) {
            log.error("同步API日志到ES任务执行失败", e);
        }
    }
}
