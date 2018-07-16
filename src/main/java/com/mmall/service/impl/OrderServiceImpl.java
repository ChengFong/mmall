package com.mmall.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by 林成峰 on 2017/8/12.
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


    public ServerResponse createOrder(Integer userId, Integer shippingId) {

        //從購物車獲取數據
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

        //訂單詳情
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);

        if (!serverResponse.isSuccess()) {

            return serverResponse;
        }

        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();

        //計算這個訂單的總價
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        //生成訂單
        Order order = this.assembleOrder(userId, shippingId, payment);
        if (order == null) {

            return ServerResponse.createByErrorMessage("生成訂單錯誤");
        }

        for (OrderItem orderItem : orderItemList) {

            orderItem.setOrderNo(order.getOrderNo());
        }

        //mybatis的批量插入
        orderItemMapper.batchInsert(orderItemList);

        //生成成功，我們要減少我們產品的庫存
        this.reduceProductStock(orderItemList);

        //清空一下購物車
        this.cleanCart(cartList);

        //返回給前端返回給前端數據
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {

        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());

        if (shipping != null) {

            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList) {

            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }

        orderVo.setOrderItemVoList(orderItemVoList);

        return orderVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    private void cleanCart(List<Cart> cartList) {

        for (Cart cart : cartList) {

            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {

        for (OrderItem orderItem : orderItemList) {

            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());

            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {

        Order order = new Order();

        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());

        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);

        //發貨時間
        //付款時間等等

        int rowCount = orderMapper.insert(order);

        if (rowCount > 0) {

            return order;
        }

        return null;
    }

    /**
     * 訂單號產生
     *
     * @return
     */
    private long generateOrderNo() {

        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {

        BigDecimal payment = new BigDecimal("0");

        for (OrderItem orderItem : orderItemList) {

            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }

        return payment;
    }


    private ServerResponse getCartOrderItem(Integer userId, List<Cart> cartList) {

        List<OrderItem> orderItemList = Lists.newArrayList();

        if (orderItemList.isEmpty()) {

            ServerResponse.createByErrorMessage("購物車為空");
        }

        //校驗購物車的數據，包括產品的狀態和數量
        for (Cart cartItem : cartList) {

            OrderItem orderItem = new OrderItem();

            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {

                return ServerResponse.createByErrorMessage("產品" + product.getName() + "不是再現售狀態");
            }

            //校驗庫存
            if (cartItem.getQuantity() > product.getStock()) {

                return ServerResponse.createByErrorMessage("產品" + product.getName() + "庫存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());

            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItem.getQuantity().doubleValue()));

            orderItemList.add(orderItem);
        }

        return ServerResponse.createBySuccess(orderItemList);
    }


    public ServerResponse pay(Integer userId, Long orderNo) {

        Order order = orderMapper.selectByOrderNo(orderNo);

        if (order == null) {

            return ServerResponse.createByErrorMessage("非系統訂單，回調忽略");
        }

        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {

            return ServerResponse.createBySuccessMessage("已支付過");
        }

        order.setStatus(Const.OrderStatusEnum.PAID.getCode());
        orderMapper.updateByPrimaryKeySelective(order);

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {

        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);

        if (order == null) {
            return ServerResponse.createByErrorMessage("用戶沒有該訂單");
        }

        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {

            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();
    }

    public ServerResponse<String> cancel(Integer userId, Long orderNo) {

        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);

        if (order == null) {

            return ServerResponse.createByErrorMessage("該用戶此訂單不存在");
        }

        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {

            return ServerResponse.createByErrorMessage("已付款，無法取消訂單");
        }

        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

        int row = orderMapper.updateByPrimaryKeySelective(updateOrder);

        if (row > 0) {

            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();
    }

    public ServerResponse getOrderCartProduct(Integer userId) {

        OrderProductVo orderProductVo = new OrderProductVo();

        //從購物車中從購物車中獲取數據
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);

        if (!serverResponse.isSuccess()) {

            return serverResponse;
        }

        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal payment = new BigDecimal("0");

        for (OrderItem orderItem : orderItemList) {

            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(this.assembleOrderItemVo(orderItem));
        }

        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServerResponse.createBySuccess(orderProductVo);
    }

    public ServerResponse<OrderVo> getOrderDetailVo(Integer userId, Long orderNo){

        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);

        if(order != null){

            List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(userId, orderNo);
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

            return ServerResponse.createBySuccess(orderVo);
        }

        return ServerResponse.createByErrorMessage("沒有找到該訂單");
    }

    public ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectByUserId(userId);

        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId){

        List<OrderVo> orderVoList = Lists.newArrayList();

        for(Order order : orderList){

            List<OrderItem> orderItemList = Lists.newArrayList();

            if(userId == null){

                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());

            }else{

                orderItemList = orderItemMapper.getByOrderNoUserId(userId, order.getOrderNo());
            }

            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

            orderVoList.add(orderVo);
        }

        return orderVoList;
    }


    //backend
    public ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, null);

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return  ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<OrderVo> manageDetail(Long orderNo){

        Order order = orderMapper.selectByOrderNo(orderNo);

        if(order != null){

            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

            return ServerResponse.createBySuccess(orderVo);
        }

        return ServerResponse.createByErrorMessage("訂單不存在");
    }

    public ServerResponse<PageInfo> manageSearch(Long orderNo, Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum, pageSize);

        Order order = orderMapper.selectByOrderNo(orderNo);

        if(order != null){

            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(orderVo));

            return ServerResponse.createBySuccess(pageInfo);
        }

        return ServerResponse.createByErrorMessage("訂單不存在");
    }

    public ServerResponse<String> manageSendGoods(Long orderNo){

        Order order = orderMapper.selectByOrderNo(orderNo);

        if(order != null){

            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){

                order.setStatus(Const.OrderStatusEnum.SHIPPINED.getCode());
                order.setSendTime(new Date());

                orderMapper.updateByPrimaryKeySelective(order);

                return ServerResponse.createBySuccess("發貨成功");
            }

            return ServerResponse.createByErrorMessage("訂單未付費");
        }

        return ServerResponse.createByErrorMessage("訂單不存在");
    }

}
