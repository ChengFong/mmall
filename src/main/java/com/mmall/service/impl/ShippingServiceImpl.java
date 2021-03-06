package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 林成峰 on 2017/8/9.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService{

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){

        shipping.setUserId(userId);

        int rowCount = shippingMapper.insert(shipping);

        if(rowCount > 0){

            Map result = new HashMap();
            result.put("shippingId", shipping.getId());

            return ServerResponse.createBySuccess("新建地址成功", result);
        }

        return ServerResponse.createByErrorMessage("新建地址失敗");
    }

    public ServerResponse del(Integer userId, Integer shippingId){

        int resultCount = shippingMapper.deleteByShippingIdUserId(userId, shippingId);

        if(resultCount > 0){

            return ServerResponse.createBySuccessMessage("刪除地址成功");
        }

        return ServerResponse.createByErrorMessage("刪除地址失敗");
    }

    public ServerResponse update(Integer userId, Shipping shipping){

        shipping.setUserId(userId);

        int rowCount = shippingMapper.updateByShipping(shipping);

        if(rowCount > 0){

            return ServerResponse.createBySuccess("更新地址成功");
        }

        return ServerResponse.createByErrorMessage("更新地址失敗");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){

        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);

        if(shipping == null){

            return ServerResponse.createByErrorMessage("無法查詢到該地址");
        }

        return ServerResponse.createBySuccess("查詢地址成功", shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum, pageSize);

        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);

        PageInfo pageInfo = new PageInfo(shippingList);

        return ServerResponse.createBySuccess(pageInfo);

    }


}
