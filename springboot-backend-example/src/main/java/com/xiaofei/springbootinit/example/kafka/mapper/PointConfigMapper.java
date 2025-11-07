package com.xiaofei.springbootinit.example.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootinit.example.kafka.model.entity.PointConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
public interface PointConfigMapper extends BaseMapper<PointConfig> {


    /**
     * 获取主点位配置
     */
    @Select("SELECT * FROM point_config WHERE isMainPoint = 1 AND status = 1 AND isDeleted = 0 LIMIT 1")
    PointConfig getMainPoint();

    /**
     * 获取所有启用的点位配置
     */
    @Select("SELECT * FROM point_config WHERE status = 1 AND isDeleted = 0")
    List<PointConfig> getAllEnabledPoints();

    /**
     * 根据点位编码获取配置
     */
    @Select("SELECT * FROM point_config WHERE pointCode = #{pointCode} AND isDeleted = 0")
    PointConfig getByPointCode(@Param("pointCode") String pointCode);

    /**
     * 批量更新点位状态
     */
    @Update("<script>" +
            "UPDATE point_config SET status = #{status} " +
            "WHERE pointCode IN " +
            "<foreach collection='pointCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " AND isDeleted = 0" +
            "</script>")
    int updatePointsStatus(@Param("pointCodes") List<String> pointCodes, @Param("status") Integer status);


    /**
     * 更新运行状态
     */
    @Update("UPDATE point_config SET runningStatus = #{status} " +
            "WHERE pointCode = #{pointCode} AND isDeleted = 0")
    int updateRunningStatus(@Param("pointCode") String pointCode, @Param("status") Integer status);
}
