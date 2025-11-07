package com.xiaofei.springbootinit.example.kafka.task;

import com.xiaofei.springbootinit.example.kafka.service.DataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
@Slf4j
@Component
public class CollectionTaskLoader implements ApplicationRunner {

    @Autowired
    private DataCollectionService dataCollectionService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
//            dataCollectionService.startAllCollectTasks();
//            log.info("采集任务启动成功");
        } catch (Exception e) {
            log.error("启动采集任务失败", e);
        }
    }
}
