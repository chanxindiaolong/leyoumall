package com.leyou.cart.interceptor;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties prop;

    private static  final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        if(StringUtils.isBlank(token)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        try {
            UserInfo user = JwtUtils.getUserInfo(prop.getPublicKey(), token);
            //spring不推荐使用
            // request.setAttribute("user",user);
            tl.set(user);
            return true;
        } catch (Exception e) {
            log.error("【购物车服务】解析用户身份失败。",e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    public static UserInfo getLoginUser() {
        return tl.get();
    }
}
