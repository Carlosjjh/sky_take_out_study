package com.sky.sky_server.mapper;

import com.sky.sky_server.entity.Employee;
import com.sky.sky_server.vo.EmployeePageVO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
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
}
