package com.sky.sky_server.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sky.sky_server.dto.CustomerOrderDTO;
import com.sky.sky_server.dto.CustomerOrderItemDTO;
import com.sky.sky_server.entity.CustomerOrder;
import com.sky.sky_server.entity.Dish;
import com.sky.sky_server.entity.OrderDetail;
import com.sky.sky_server.exception.BusinessException;
import com.sky.sky_server.mapper.DishMapper;
import com.sky.sky_server.mapper.OrderMapper;
import com.sky.sky_server.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {
    private final DishMapper dishMapper;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(DishMapper dishMapper, OrderMapper orderMapper) {
        this.dishMapper = dishMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public Long submit(CustomerOrderDTO customerOrderDTO) {
        CustomerOrder order = new CustomerOrder();
        order.setCustomerName(customerOrderDTO.getCustomerName().trim());
        order.setPhone(customerOrderDTO.getPhone());
        order.setAddress(customerOrderDTO.getAddress().trim());
        order.setStatus(1);
        order.setOrderTime(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (CustomerOrderItemDTO item : customerOrderDTO.getItems()) {
            Dish dish = dishMapper.getById(item.getDishId());
            if (dish == null || dish.getStatus() != 1) {
                throw new BusinessException("a dish in the cart is unavailable");
            }
            total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(item.getNumber())));
        }
        order.setAmount(total);
        orderMapper.insert(order);

        for (CustomerOrderItemDTO item : customerOrderDTO.getItems()) {
            Dish dish = dishMapper.getById(item.getDishId());
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getId());
            detail.setDishId(dish.getId());
            detail.setName(dish.getName());
            detail.setNumber(item.getNumber());
            detail.setAmount(dish.getPrice().multiply(BigDecimal.valueOf(item.getNumber())));
            orderMapper.insertDetail(detail);
        }
        return order.getId();
    }
}
