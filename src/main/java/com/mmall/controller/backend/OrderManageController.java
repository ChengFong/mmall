package com.mmall.controller.backend;

import com.mmall.common.ServerResponse;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 後台-訂單管理
 *
 * Created by 林成峰 on 2017/8/16.
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    IOrderService iOrderService;

    /**
     * 訂單列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(
                            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){

        return iOrderService.manageList(pageNum, pageSize);
    }

    /**
     * 訂單詳情
     *
     * @param orderNo
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(Long orderNo){

        return iOrderService.manageDetail(orderNo);
    }

    /**
     * 訂單查詢
     *
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse orderSearch(Long orderNo,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){

        return iOrderService.manageSearch(orderNo, pageNum, pageSize);
    }

    /**
     * 寄貨
     *
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo){

        return iOrderService.manageSendGoods(orderNo);
    }
}
