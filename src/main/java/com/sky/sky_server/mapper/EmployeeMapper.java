package com.sky.sky_server.mapper;

import com.sky.sky_server.entity.Employee;
import com.sky.sky_server.vo.EmployeePageVO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import java.util.List;
import com.sky.sky_server.dto.EmployeePageQueryDTO;

@Mapper
public interface EmployeeMapper {
        @Select("select * from employee where username = #{username}")
        Employee getByUsername(String username);

        @Insert("insert into employee " +
                        "(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) "
                        +
                        "values " +
                        "(#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
        void insert(Employee employee);

        @Select({
                        "<script>",
                        "select id, name, username, phone, sex, id_number, status, create_time, update_time, create_user, update_user from employee",
                        "<where>",
                        "<if test='name != null and name != \"\"'>",
                        "and name like concat('%', #{name}, '%')",
                        "</if>",
                        "</where>",
                        "order by create_time desc",
                        "</script>"
        })
        List<EmployeePageVO> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

        @Update({
                        "<script>",
                        "update employee",
                        "<set>",
                        "<if test='name != null'>name = #{name},</if>",
                        "<if test='username != null'>username = #{username},</if>",
                        "<if test='password != null'>password = #{password},</if>",
                        "<if test='phone != null'>phone = #{phone},</if>",
                        "<if test='sex != null'>sex = #{sex},</if>",
                        "<if test='idNumber != null'>id_number = #{idNumber},</if>",
                        "<if test='status != null'>status = #{status},</if>",
                        "<if test='updateTime != null'>update_time = #{updateTime},</if>",
                        "<if test='updateUser != null'>update_user = #{updateUser},</if>",
                        "</set>",
                        "where id = #{id}",
                        "</script>"

        })
        void update(Employee employee);

        @Select("select id, name, username, phone, sex, id_number, status, create_time, update_time, create_user, update_user from employee where id = #{id}")
        EmployeePageVO getById(Long id);
}
