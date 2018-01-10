package com.mmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.RedisShardedPool;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * 用户权限统一认证拦截器
 * Created by hasee on 2018/1/10.
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor{

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        //将会执行的方法
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        //解析handlerMethod得到方法名和类名
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        //解析前台传来的参数，用于打印到console观察
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = httpServletRequest.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;

            Object obj = entry.getValue();
            if (obj instanceof String[]){
                String[] str = (String[]) obj;
                mapValue = Arrays.toString(str);
            }
            //拼接前台传的参数用于打印观察
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        //拦截到登录请求直接放过，否则会造成循环拦截，用户永远无法登陆
        if (StringUtils.equals(className,"UserManageController") && StringUtils.equals(methodName,"login")){
            //此处不打印日志信息的目的是参数中有用户名和密码，安全起见，不打印日志信息
            log.info("权限拦截器拦截到请求,className:{},methodName:{}",className,methodName);
            return true;
        }

        log.info("权限拦截器拦截到请求,className:{},methodName:{},param:{}",className,methodName,requestParamBuffer.toString());

        User user = null;
        //Redis中查询用户信息
        String token = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isNotEmpty(token)){
            String userJsonStr = RedisShardedPoolUtil.get(token);
            user = JsonUtil.string2Obj(userJsonStr,User.class);
        }

        if (user == null || user.getRole().intValue() != Const.Role.ROLE_ADMIN){
            //用户为空或者非管理员权限需要返回false,不再进入controller方法
            //此拦截器的方法是实现接口的方法，无法改变boolean这个方法返回类型，但是此处又需要直接返回给前端结果，不再请求后端，所以需要用respose的getWriter
            httpServletResponse.reset();
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json;charset=UTF-8");

            PrintWriter out = httpServletResponse.getWriter();

            //区分是用户未登录，还是用户无权限
            if (user == null){
                //区分是否是富文本上传功能,是的话需要返回指定的返回值格式
                if (StringUtils.equals(className,"ProductManageController") && StringUtils.equals(methodName,"richTextImgUpload")){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","请先登录再上传图片！");
                    out.print(JsonUtil.obj2String(resultMap));
                }else{
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截,用户未登录！")));
                }
            }else{
                //区分是否是富文本上传功能,是的话需要返回指定的返回值格式
                if (StringUtils.equals(className,"ProductManageController") && StringUtils.equals(methodName,"richTextImgUpload")){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","用户无管理员权限！");
                    out.print(JsonUtil.obj2String(resultMap));
                }else{
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截,用户无权限！")));
                }

            }
            out.flush();
            out.close();

            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
