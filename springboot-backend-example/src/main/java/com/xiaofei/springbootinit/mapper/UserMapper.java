package com.xiaofei.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootinit.model.entity.Post;
import com.xiaofei.springbootinit.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * 用户数据库操作
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户列表（包括已被删除的数据）
     */
    List<User> listPostWithDelete(Date minUpdateTime);

    /**
     * 查询已删除的用户列表（增量查询）
     * @param minUpdateTime 最小更新时间
     * @return 已删除的用户列表
     */
    List<User> listDeletedUsers(Date minUpdateTime);

}




