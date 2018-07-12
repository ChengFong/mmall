package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 收戶地址接口
 *
 * Created by 林成峰 on 2017/8/9.
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        return iShippingService.add(user.getId(), shipping);
    }

    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse del(HttpSession session, Integer shippingId){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        return iShippingService.del(user.getId(), shippingId);
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session, Shipping shipping){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        return iShippingService.update(user.getId(), shipping);
    }

    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse select(HttpSession session, Integer shippingId){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        return iShippingService.select(user.getId(), shippingId);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        return iShippingService.list(user.getId(), pageNum, pageSize);
    }

}
