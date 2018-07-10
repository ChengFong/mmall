package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by 林成峰 on 2017/8/16.
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    IUserService iUserService;

    @Autowired
    IOrderService iOrderService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session,
                            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        //校驗一下是否是管理員
        if(iUserService.checkAdminRole(user).isSuccess()){

            //填充業務
            return iOrderService.manageList(pageNum, pageSize);

        }else{
            return ServerResponse.createByErrorMessage("無權限操作，需要管理員權限操作");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session, Long orderNo){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        //校驗一下是否是管理員
        if(iUserService.checkAdminRole(user).isSuccess()){

            //填充業務
            return iOrderService.manageDetail(orderNo);

        }else{

            return ServerResponse.createByErrorMessage("無權限操作，需要管理員權限操作");
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse orderSearch(HttpSession session,
                                      Long orderNo,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        //校驗一下是否是管理員
        if(iUserService.checkAdminRole(user).isSuccess()){

            //填充業務
            return iOrderService.manageSearch(orderNo, pageNum, pageSize);

        }else{
            return ServerResponse.createByErrorMessage("無權限操作，需要管理員權限操作");
        }
    }

    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        //校驗一下是否是管理員
        if(iUserService.checkAdminRole(user).isSuccess()){

            //填充業務
            return iOrderService.manageSendGoods(orderNo);

        }else{
            return ServerResponse.createByErrorMessage("無權限操作，需要管理員權限操作");
        }
    }
}
