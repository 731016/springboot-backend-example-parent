package com.xiaofei.springbootinit.job.once;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaofei.springbootinit.model.entity.LogData;
import com.xiaofei.springbootinit.model.entity.User;
import com.xiaofei.springbootinit.service.LogDataService;
import com.xiaofei.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class FullSyncLogData implements CommandLineRunner {
    @Resource
    private LogDataService logDataService;

    @Resource
    private UserService userService;

    @Override
    public void run(String... args) {

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("id", "1871119384983060482");
        List<User> userList = userService.list(wrapper);

        User user = userList.get(0);
        String jsonData = JSONUtil.toJsonStr(user);

        List<LogData> logDataList = new ArrayList<>();

        Date nowDate = new Date();
        int dataSize = 30 * 10000;
        for (int i = 0; i < dataSize; i++) {
            LogData logData = new LogData();
            logData.setValueid(user.getId());
            logData.setData(jsonData);
            logData.setCreatedate(nowDate);
            logData.setCreateuser(user.getId());
            logData.setId(Long.valueOf(i));
            logDataList.add(logData);
        }

        logDataService.saveBatch(logDataList);
    }
}
