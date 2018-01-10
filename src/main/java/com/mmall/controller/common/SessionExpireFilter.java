package com.mmall.controller.common;

/**
 * Created by hasee on 2018/1/9.
 */


import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 重置session有效期过滤器，过滤除login.do外所有的.do请求
 */
public class SessionExpireFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * 重置用户session有效期方法
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        String token = CookieUtil.readLoginToken(request);
        //token不为空的话查一下redis是否有用户信息
        if (!StringUtils.isEmpty(token)){
            String userJsonStr = RedisShardedPoolUtil.get(token);
            User user = JsonUtil.string2Obj(userJsonStr, User.class);
            if (user != null){
                //redis有用户信息的话就重置用户的session时间
                RedisShardedPoolUtil.expire(token, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
