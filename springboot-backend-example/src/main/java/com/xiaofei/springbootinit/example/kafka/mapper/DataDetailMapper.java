package com.xiaofei.springbootinit.example.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootinit.example.kafka.model.entity.DataDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
public interface DataDetailMapper extends BaseMapper<DataDetail> {

    /**
     * 批量插入数据明细
     */
    @Insert("<script>" +
            "INSERT INTO data_detail (pointCode, collectTime, value, attributeName, statisticsId, createTime) VALUES " +
            "<foreach collection='details' item='detail' separator=','>" +
            "(#{detail.pointCode}, #{detail.collectTime}, #{detail.value}, " +
            "#{detail.attributeName}, #{detail.statisticsId}, NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("details") List<DataDetail> details);

    /**
     * 获取统计ID下的所有明细数据
     */
    @Select("SELECT * FROM data_detail WHERE statisticsId = #{statisticsId} AND isDeleted = 0")
    List<DataDetail> getDetailsByStatisticsId(@Param("statisticsId") Long statisticsId);

    /**
     * 获取时间范围内的明细数据
     */
    @Select("SELECT * FROM data_detail " +
            "WHERE pointCode = #{pointCode} " +
            "AND collectTime >= #{startTime} " +
            "AND collectTime <= #{endTime} " +
            "AND isDeleted = 0 " +
            "ORDER BY collectTime DESC")
    List<DataDetail> getDetailsByTimeRange(@Param("pointCode") String pointCode,
                                           @Param("startTime") Date startTime,
                                           @Param("endTime") Date endTime);

    /**
     * 计算统计值
     */
    @Select("SELECT " +
            "MAX(value) as maxValue, " +
            "MIN(value) as minValue, " +
            "AVG(value) as avgValue " +
            "FROM data_detail " +
            "WHERE statisticsId = #{statisticsId} " +
            "AND isDeleted = 0")
    Map<String, BigDecimal> calculateStatistics(@Param("statisticsId") Long statisticsId);

    /**
     * 删除过期数据
     */
    @Update("UPDATE data_detail SET isDeleted = 1 " +
            "WHERE collectTime < #{expireTime} " +
            "AND isDeleted = 0")
    int deleteExpiredData(@Param("expireTime") Date expireTime);
}
