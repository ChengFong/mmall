package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by 林成峰 on 2017/8/7.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService{

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count){

        if(productId == null || count == null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);

        if(product == null){

            return ServerResponse.createByErrorCodeMessage(Const.ProductStatusEnum.NOT_EXIST.getCode(), Const.ProductStatusEnum.NOT_EXIST.getValue());
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);

        if(cart == null){

            //這個產品不在這個購物車裡,需要新增一個這個產品的記錄
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);

            cartMapper.insert(cartItem);

        }else{

            //這個產品已經在購物車裡了.
            //如果產品已存在,數量相加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);

            cartMapper.updateByPrimaryKeySelective(cart);
        }

        return this.list(userId);
    }

    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count){

        if(productId == null || count == null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);

        if(product == null){

            return ServerResponse.createByErrorCodeMessage(Const.ProductStatusEnum.NOT_EXIST.getCode(), Const.ProductStatusEnum.NOT_EXIST.getValue());
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if(cart != null){

            cart.setQuantity(count);
        }

        cartMapper.updateByPrimaryKeySelective(cart);

        return this.list(userId);
    }

    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds){

        List<String> productList = Splitter.on(",").splitToList(productIds);

        if(CollectionUtils.isEmpty(productList)){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        cartMapper.deleteByUserIdProductIds(userId, productList);

        return this.list(userId);
    }

    public ServerResponse<CartVo> list(Integer userId){

        System.out.print(userId);

        CartVo cartVo = this.getCartVoLimit(userId);

        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> selectOrUnselect(Integer userId, Integer productId, Integer checked){

        cartMapper.checkedOrUncheckedAllProduct(userId, productId, checked);

        return this.list(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){

        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }

        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    private CartVo getCartVoLimit(Integer userId){

        CartVo cartVo = new CartVo();

        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        if(CollectionUtils.isNotEmpty(cartList)){

            for(Cart cartItem : cartList){

                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);

                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){

                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());

                    //判斷庫存
                    int buyLimitCount = 0;

                    if(product.getStock() >= cartItem.getQuantity()){

                        //庫存充足的時候
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);

                    }else{

                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);

                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);

                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }

                    cartProductVo.setQuantity(buyLimitCount);

                    //計算總價
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                if(cartItem.getChecked() == Const.Cart.CHECKED){

                    //如果已經勾選,增加到整個的購物車總價中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }

                cartProductVoList.add(cartProductVo);
            }
        }

        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){

        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0? true: false;
    }

}
