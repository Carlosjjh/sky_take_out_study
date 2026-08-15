package com.sky.sky_server.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sky.sky_server.dto.DishPageQueryDTO;
import com.sky.sky_server.entity.Dish;
import com.sky.sky_server.vo.DishCustomerVO;
import com.sky.sky_server.vo.DishPageVO;

@Mapper
public interface DishMapper {
    @Insert("insert into dish (name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) "
            + "values (#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dish dish);

    @Select({
            "<script>",
            "select d.id, d.name, d.category_id, c.name as category_name, d.price, d.image, d.description, d.status, d.create_time, d.update_time",
            "from dish d join category c on d.category_id = c.id",
            "<where>",
            "<if test='name != null and name != \"\"'>and d.name like concat('%', #{name}, '%')</if>",
            "<if test='categoryId != null'>and d.category_id = #{categoryId}</if>",
            "</where>",
            "order by d.update_time desc, d.id desc",
            "</script>"
    })
    List<DishPageVO> pageQuery(DishPageQueryDTO queryDTO);

    @Select("select id, name, category_id, price, image, description, status from dish where id = #{id}")
    Dish getById(Long id);

    @Select("select d.id, d.name, d.category_id, d.price, d.image, d.description from dish d join category c on d.category_id = c.id where d.category_id = #{categoryId} and d.status = 1 and c.status = 1 order by d.id desc")
    List<DishCustomerVO> listEnabledByCategoryId(Long categoryId);

    @Update("update dish set status = #{status}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    int updateStatus(Integer status, Long id, LocalDateTime updateTime, Long updateUser);
}
