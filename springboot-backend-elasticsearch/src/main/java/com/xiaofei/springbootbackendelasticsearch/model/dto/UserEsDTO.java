package com.xiaofei.springbootbackendelasticsearch.model.dto;

import com.xiaofei.springbootinit.model.dto.post.PostEsDTO;
import com.xiaofei.springbootinit.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户 ES 文档
 */
@Document(indexName = "user")
@Data
public class UserEsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String userAccount;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String userName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String userProfile;

    @Field(type = FieldType.Keyword)
    private String userRole;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private Date createTime;
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private Date updateTime;
    
    private Integer isDelete;

    /**
     * 高亮字段 - 用于存储高亮后的文本（不映射到 ES）
     */
    private String highlightUserAccount;
    private String highlightUserName;
    private String highlightUserProfile;

    /**
     * 对象转包装类
     *
     * @param user
     * @return
     */
    public static UserEsDTO objToDto(User user) {
        if (user == null) {
            return null;
        }
        UserEsDTO userEsDTO = new UserEsDTO();
        BeanUtils.copyProperties(user, userEsDTO);
        return userEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param userEsDTO
     * @return
     */
    public static User dtoToObj(UserEsDTO userEsDTO) {
        if (userEsDTO == null) {
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(userEsDTO, user);
        return user;
    }
}
