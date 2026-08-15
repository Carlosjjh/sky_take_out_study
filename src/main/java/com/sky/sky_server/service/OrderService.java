package com.sky.sky_server.service;

import com.sky.sky_server.dto.CustomerOrderDTO;

public interface OrderService {
    Long submit(CustomerOrderDTO customerOrderDTO);
}
