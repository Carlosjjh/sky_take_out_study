package com.sky.sky_server.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.sky.sky_server.utils.JwtUtil;
import com.sky.sky_server.context.BaseContext;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Value("${sky.jwt.secret-key}")
    private String secretkey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object hanler) throws Exception {
        String token = request.getHeader("token");

        try {
            Claims claims = JwtUtil.parseJWT(secretkey, token);
            Long employeeId = Long.valueOf(claims.get("employeeId").toString());

            BaseContext.setCurrentId(employeeId);

            log.info("JWT校验通过，当前员工id:{}", employeeId);
            return true;
        } catch (Exception ex) {
            log.info("JWT校验失败：{}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }

}
