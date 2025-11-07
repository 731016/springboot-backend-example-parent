package com.xiaofei.springbootinit.example.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootinit.example.kafka.model.entity.DataStatistics;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
public interface DataStatisticsMapper extends BaseMapper<DataStatistics> {

    /**
     * 获取进行中的统计记录
     */
    @Select("SELECT * FROM data_statistics WHERE pointCode = #{pointCode} AND status = 1 AND isDeleted = 0")
    DataStatistics getActiveStatistics(@Param("pointCode") String pointCode);

    /**
     * 更新统计值
     */
    @Update("UPDATE data_statistics SET " +
            "maxValue = #{maxValue}, " +
            "minValue = #{minValue}, " +
            "avgValue = #{avgValue}, " +
            "updateTime = NOW() " +
            "WHERE id = #{id} AND isDeleted = 0")
    int updateStatisticsValues(@Param("id") Long id,
                               @Param("maxValue") BigDecimal maxValue,
                               @Param("minValue") BigDecimal minValue,
                               @Param("avgValue") BigDecimal avgValue);

    /**
     * 完成统计
     */
    @Update("UPDATE data_statistics SET " +
            "status = 2, " +
            "endTime = #{endTime}, " +
            "updateTime = NOW() " +
            "WHERE id = #{id} AND isDeleted = 0")
    int completeStatistics(@Param("id") Long id, @Param("endTime") Date endTime);

    /**
     * 获取时间范围内的统计数据
     */
    @Select("SELECT * FROM data_statistics " +
            "WHERE pointCode = #{pointCode} " +
            "AND startTime >= #{startTime} " +
            "AND startTime <= #{endTime} " +
            "AND isDeleted = 0 " +
            "ORDER BY startTime DESC")
    List<DataStatistics> getStatisticsByTimeRange(@Param("pointCode") String pointCode,
                                                  @Param("startTime") Date startTime,
                                                  @Param("endTime") Date endTime);

}
