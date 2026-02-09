package com.xiaofei.springbootbackendkafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootbackendkafka.model.entity.PointConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Mapper
public interface PointConfigMapper extends BaseMapper<PointConfig> {


    /**
     * 获取主点位配置
     */
    @Select("SELECT * FROM point_config WHERE isMainPoint = 1 AND status = 1 LIMIT 1")
    PointConfig getMainPoint();

    /**
     * 获取所有启用的点位配置
     */
    @Select("SELECT * FROM point_config WHERE status = 1")
    List<PointConfig> getAllEnabledPoints();

    /**
     * 根据点位编码获取配置
     */
    @Select("SELECT * FROM point_config WHERE pointCode = #{pointCode}")
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
            "</script>")
    int updatePointsStatus(@Param("pointCodes") List<String> pointCodes, @Param("status") Integer status);


    /**
     * 更新运行状态
     */
    @Update("UPDATE point_config SET runningStatus = #{status} " +
            "WHERE pointCode = #{pointCode}")
    int updateRunningStatus(@Param("pointCode") String pointCode, @Param("status") Integer status);

    /**
     * 当前点位的最后移除运行时间
     * @param pointCode
     * @return
     */
    @Select("select collectTime from data_detail where pointCode = #{pointCode} order by collectTime desc limit 1")
    Date getLastCollectTime(@Param("pointCode") String pointCode);
}
