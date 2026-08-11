package com.sky.sky_server.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

import com.sky.sky_server.entity.Category;
import com.sky.sky_server.dto.CategoryPageQueryDTO;
import com.sky.sky_server.vo.CategoryPageVO;

@Mapper
public interface CategoryMapper {

    @Insert("insert into category (type, name, sort, status, create_time, update_time, create_user, update_user) "
            + "values (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    @Select({
            "<script>",
            "select id, type, name, sort, status, create_time, update_time from category",
            "<where>",
            "<if test='name != null and name != \"\"'>and name like concat('%', #{name}, '%')</if>",
            "<if test='type != null'>and type = #{type}</if>",
            "</where>",
            "order by sort asc, create_time desc",
            "</script>"
    })
    List<CategoryPageVO> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    @Update("update category set status = #{status}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    int updateStatus(Integer status, Long id, LocalDateTime updateTime, Long updateUser);
}
