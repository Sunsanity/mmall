package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by hasee on 2017/6/4.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 新增收货地址
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setId(userId);
        int resultCount = shippingMapper.insert(shipping);
        if (resultCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新增地址成功！",result);
        }
        return ServerResponse.createByErrorMessage("新增地址失败！");
    }

    /**
     * 删除收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse<String> del(Integer userId, Integer shippingId){
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("删除收货地址成功！");
        }
        return ServerResponse.createByErrorMessage("删除收货地址失败！");
    }

    /**
     * 更新收货地址
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse<String> update(Integer userId, Shipping shipping){
        //防止前台直接传过来一个模拟的假的USERID，此处更新一下shipping对象的id为当前登陆用户的userid
        shipping.setUserId(userId);
        int resultCount = shippingMapper.updateByShipping(shipping);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新收货地址成功！");
        }
        return ServerResponse.createByErrorMessage("更新收货地址失败！");
    }

    /**
     * 查看一个收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if (shipping == null){
            return ServerResponse.createByErrorMessage("无法查询该收货地址！");
        }
        return ServerResponse.createBySuccess("查询收货地址成功！",shipping);
    }

    /**
     * 分页查询用户收货地址
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
