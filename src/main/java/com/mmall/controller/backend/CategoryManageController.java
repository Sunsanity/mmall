package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.RedisShardedPool;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by hasee on 2017/5/21.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加分类
     * @param httpServletRequest
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpServletRequest httpServletRequest, String categoryName, @RequestParam(value = "parentId",defaultValue = "0") int parentId){
        //先验证用户是否登录
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorMessage("用户未登录，请先登录再添加分类！");
        }
        //判断当前用户是否有是管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //添加分类逻辑
            return iCategoryService.addCategory(categoryName,parentId);
        }
        return ServerResponse.createByErrorMessage("添加分类失败，用户无管理员权限！");
    }

    /**
     * 更改分类名称
     * @param httpServletRequest
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpServletRequest httpServletRequest,Integer categoryId,String categoryName){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录再更改分类名称！");
        }
        //判断当前用户是否有是管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //更改分类名称逻辑
            return iCategoryService.setCategoryName(categoryId,categoryName);
        }
        return ServerResponse.createByErrorMessage("修改分类信息失败，用户无管理员权限！");
    }

    /**
     * 根据父节点获取下一级子节点，不递归
     * @param httpServletRequest
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpServletRequest httpServletRequest,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录后再获取节点信息！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            //获取节点信息逻辑
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("无管理员权限，获取节点信息失败！");
        }
    }

    /**
     * 递归查询当前节点ID及子节点ID，得到ID集合(set)
     * @param httpServletRequest
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepCategory(HttpServletRequest httpServletRequest,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            //调用service递归查询子节点ID
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }
        return ServerResponse.createByErrorMessage("非管理员，用户无权限！");
    }
}
