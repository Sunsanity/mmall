package com.mmall.util;

/**
 * Created by hasee on 2018/1/9.
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie工具类
 */
@Slf4j
public class CookieUtil {

    //cookie所在域，写到一级域名两个tomcat都可以访问到cookie
    //private static final String COOKIE_DOMAIN = "xn--gmq18dr0k17bucv74fotz57p.com"; //中文域名转码后
    private static final String COOKIE_DOMAIN = ".imooc.com"; //测试domain
    //写到浏览器中的cookie的key
    private static final String COOKIE_NAME = "mmall_login_token";

    /**
     * 读取cookie方法
     * @param request
     * @return
     */
    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cks = request.getCookies();
        if (cks != null){
            for(Cookie ck : cks){
                if (StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    log.info("返回cookie key={} value={}",ck.getName(),ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 向浏览器写入cookie,值是sessionId,通过这个sessionId可以从redis中拿到用户信息
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response,String token){
        Cookie cookie = new Cookie(COOKIE_NAME,token);
        cookie.setDomain(COOKIE_DOMAIN);
        //设置在根目录
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        //cookie有效期设置为一年，这样cookie才可以写入硬盘当中，不设置的话为永久有效，只会存在于内存当中
        cookie.setMaxAge(60*60*24*365);
        log.info("写入cookie key={} value={}",cookie.getName(),cookie.getValue());
        response.addCookie(cookie);
    }

    /**
     * 删除cookie,其实就是从request中读取到正确的cookie,然后将cookie的有效期设置为0,就代表删除了cookie
     * @param request
     * @param response
     */
    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cks = request.getCookies();
        if (cks != null){
            for(Cookie ck : cks){
                if (StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    ck.setMaxAge(0);
                    log.info("删除cookie key={} value={}",ck.getName(),ck.getValue());
                    response.addCookie(ck);
                    return;
                }
            }
        }
    }
}
