package com.xiaofei.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaofei.springbootinit.mapper.LogDataMapper;
import com.xiaofei.springbootinit.model.entity.LogData;
import com.xiaofei.springbootinit.service.LogDataService;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【log_data】的数据库操作Service实现
* @createDate 2025-08-05 21:51:08
*/
@Service
public class LogDataServiceImpl extends ServiceImpl<LogDataMapper, LogData>
    implements LogDataService {

}




