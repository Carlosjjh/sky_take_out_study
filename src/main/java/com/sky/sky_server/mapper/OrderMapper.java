package com.sky.sky_server.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import com.sky.sky_server.entity.CustomerOrder;
import com.sky.sky_server.entity.OrderDetail;

@Mapper
public interface OrderMapper {
    @Insert("insert into customer_order (customer_name, phone, address, amount, status, order_time) "
            + "values (#{customerName}, #{phone}, #{address}, #{amount}, #{status}, #{orderTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CustomerOrder order);

    @Insert("insert into order_detail (order_id, dish_id, name, number, amount) "
            + "values (#{orderId}, #{dishId}, #{name}, #{number}, #{amount})")
    void insertDetail(OrderDetail detail);
}
