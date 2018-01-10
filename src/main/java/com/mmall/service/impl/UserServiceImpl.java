package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisPoolUtil;
import com.mmall.util.RedisShardedPoolUtil;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jws.soap.SOAPBinding;
import java.util.UUID;

/**
 * Created by hasee on 2017/5/18.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * @param userName
     * @param password
     * @return
     */
    public ServerResponse<User> login(String userName, String password) {
        int resultCount = userMapper.checkUsername(userName);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //todo MD5对密码加密(数据库中的密码是用MD5加密过的)
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(userName,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }

    /**
     * 注册
     * @param user
     * @return
     */
    public ServerResponse<String> register(User user) {
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户名已存在！");
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("email已存在！");
        }
        //为新注册的用户设置角色，0是普通用户，1是管理员，角色采用内部接口的方式实现普通用户和管理员的分组管理
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //使用MD5工具类对用户注册的密码进行加密，然后再存储到数据库中(数据库中的用户密码都是加密后的字符串)
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        //insert语句返回值代表影响行数,如果是0的话代表插入失败
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 前台页面异步校验用户名和email是否已经存在
     * @param str
     * @param type
     * @return
     */
    public ServerResponse<String> checkValid(String str,String type){
        //如果前台传来的参数类型不为空的话后台开始验证用户名或email是否已存在,是空的话直接返回参数类型错误
        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("用户名已存在！");
                }
            }else if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("email已存在！");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数类型错误！");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 获取密码提示问题
     * @param username
     * @return
     */
    public ServerResponse<String> selectQuestion(String username){
        ServerResponse response = this.checkValid(username,Const.USERNAME);
        if(response.isSuccess()){
            return ServerResponse.createByErrorMessage("用户名不存在，无法获取提示问题！");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("获取提示问题为空！");
    }

    /**
     * 查询提示问题答案是否正确，正确返回Token用于密码修改
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount > 0){
            //向前台返回UUID用于用户修改密码，UUID存放在本地缓存中且有个有效时间，可以有效避免用户的横向越权，恶意修改其他用户的密码
            String forgetToken = UUID.randomUUID().toString();
            RedisShardedPoolUtil.setEx(Const.TOKEN_PREFIX + username,forgetToken,60*60*12);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("提示问题答案错误，请重新填写答案！");
    }

    /**
     * 用户忘记密码，通过提示问题答案的方式修改密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("修改密码需要传入token参数！");
        }
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户名不存在，请先确认用户名！");
        }
        String token = RedisShardedPoolUtil.get(Const.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token已经过期，操作失败！");
        }
        if(StringUtils.equals(forgetToken,token)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(resultCount > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功！");
            }
        }else{
            return ServerResponse.createByErrorMessage("token参数错误，请检查token！");
        }
        return ServerResponse.createByErrorMessage("修改密码失败！");
    }

    /**
     * 登陆用户修改密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //为防止横向越权，根据用户名和密码一起查询表中的记录数量，判断旧密码是否对应当前登录用户
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("用户密码错误！");
        }
        String password = MD5Util.MD5EncodeUtf8(passwordNew);
        user.setPassword(password);
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("密码修改成功！");
        }
        return ServerResponse.createByErrorMessage("密码修改失败！");
    }

    /**
     * 个人信息修改
     * @param user
     * @return
     */
    public ServerResponse<User> updateInformation(User user){
        int resultCount = userMapper.checkEmailByUsername(user.getEmail(),user.getId());
        if (resultCount > 0){
            return ServerResponse.createByErrorMessage("email已经存在，请重新输入email!");
        }
        User updateUser = new User();
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setId(user.getId());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if (resultCount > 0){
            return ServerResponse.createBySuccess("修改个人信息成功！",updateUser);
        }
        return ServerResponse.createByErrorMessage("修改个人信息失败！");
    }

    /**
     * 获取当前登陆用户信个人信息
     * @param userId
     * @return
     */
    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null){
            return ServerResponse.createByErrorMessage("找不到该用户！");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 判断用户是否是管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByErrorMessage("用户非管理员！");
    }
}
