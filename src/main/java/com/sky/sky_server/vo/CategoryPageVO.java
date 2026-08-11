package com.sky.sky_server.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CategoryPageVO {
  private Long id;
  private Integer type;
  private String name;
  private Integer sort;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}