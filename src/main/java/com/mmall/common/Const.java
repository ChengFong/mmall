package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by 林成峰 on 2017/7/31.
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface ProductListOrderBy{

        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Cart{
        int CHECKED = 1; //購物車選中狀態
        int UN_CHECKED = 0; //購物車未選中狀態

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }


    public interface Role{
        int ROLE_CUSTOMER = 0; //普通用戶
        int ROLE_ADMIN = 1; //管理員
    }

    public enum ProductStatusEnum{

        ON_SALE(1, "在現");

        private int code;
        private String value;

        ProductStatusEnum(int code, String value){
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0, "已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已付款"),
        SHIPPINED(40, "已發貨"),
        ORDER_SUCCESS(50, "訂單完成"),
        ORDER_CLOSE(60, "訂單關閉");

        OrderStatusEnum(int code, String value){
            this.code = code;
            this.value = value;
        }

        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        public static OrderStatusEnum codeOf(int code){
            for (OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }

            throw new RuntimeException("沒有找到對應的枚舉");
        }
    }

    public interface  AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");

        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static PayPlatformEnum codeOf(int code){
            for (PayPlatformEnum payPlatformEnum : values()){
                if(payPlatformEnum.getCode() == code){
                    return payPlatformEnum;
                }
            }

            throw new RuntimeException("沒有找到對應的枚舉");
        }
    }

    public enum PaymentTypeEnum{

        ONLINE_PAY(1, "在線支付");

        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static PaymentTypeEnum codeOf(int code){
            for (PaymentTypeEnum paymentTypeEnum : values()){
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }

            throw new RuntimeException("沒有找到對應的枚舉");
        }

    }
}
