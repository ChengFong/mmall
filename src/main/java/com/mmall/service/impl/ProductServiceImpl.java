package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 林成峰 on 2017/8/5.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    ICategoryService iCategoryService;

    /**
     * 商品新增，保存
     * @param product
     * @return
     */
    public ServerResponse saveOrUpdateProduct(Product product){

        if(product != null){

            if(StringUtils.isNotBlank(product.getSubImages())){

                String[] subImageArray = product.getSubImages().split(",");

                if(subImageArray.length > 0){

                    product.setMainImage(subImageArray[0]);
                }
            }


            if(product.getId() != null){

                //更新
                int rowCount = productMapper.updateByPrimaryKey(product);

                if(rowCount > 0){

                    return ServerResponse.createBySuccessMessage("更新產品成功");
                }

                return ServerResponse.createByErrorMessage("更新產品失敗");
            }else {

                //新增
                int rowCount = productMapper.insert(product);

                if (rowCount > 0) {

                    return ServerResponse.createBySuccessMessage("新增產品成功");
                }

                return ServerResponse.createByErrorMessage("新增產品失敗");
            }
        }

        return ServerResponse.createByErrorMessage("新增或保存產品的參數不正確");
    }

    /**
     * 上下架功能開發
     * @param productId
     * @param status
     * @return
     */
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status){

        if(productId==null || status==null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int rowCount = productMapper.updateByPrimaryKeySelective(product);

        if(rowCount > 0){

            return ServerResponse.createBySuccessMessage("修改產品銷售狀態成功");
        }

        return ServerResponse.createByErrorMessage("修改產品銷售狀態失敗");
    }

    /**
     * 后台获取商品详情功能开发
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){

        if(productId == null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);

        if(product == null){

            return ServerResponse.createByErrorMessage("產品已下架或者刪除");
        }

        ProductDetailVo productDetailVo = this.assembleProductDetail(product);

        return  ServerResponse.createBySuccess(productDetailVo);
    }


    private ProductDetailVo assembleProductDetail(Product product){

        ProductDetailVo productDetailVo = new ProductDetailVo();

        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        //imageHost
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.imoooc.com/"));

        //parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){

            productDetailVo.setParentCategoryId(0); //默認根節點
        }else{

            productDetailVo.setParentCategoryId(category.getParentId());
        }

        //createTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));

        //updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }


    /**
     * 後台商品列表動態分頁功能開發
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum, pageSize);

        List<Product> productList = productMapper.selectList();

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){

            ProductListVo productListVo = this.assembleProductList(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productList);

        return  ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductList(Product product){

        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.imoooc.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());

        return productListVo;
    }

    /**
     * 后台商品搜索功能开发
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum, pageSize);

        if(StringUtils.isNotBlank(productName)){

            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }

        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = this.assembleProductList(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productList);

        return  ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){

        if(productId == null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);

        if(product == null){

            return ServerResponse.createByErrorMessage("產品已下架或者刪除");
        }

        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){

            return ServerResponse.createByErrorMessage("產品已下架或者刪除");
        }

        ProductDetailVo productDetailVo = this.assembleProductDetail(product);

        return  ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy){

        if(StringUtils.isBlank(keyword) && categoryId == null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        List<Integer> categoryIdList = new ArrayList<>();

        if(categoryId != null){

            Category category = categoryMapper.selectByPrimaryKey(categoryId);

            if(category == null && StringUtils.isBlank(keyword)){

                //沒有該分類,並且還​​沒有關鍵字,這個時候返回一個空的結果集,不報錯
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);

                return ServerResponse.createBySuccess(pageInfo);
            }

            categoryIdList = iCategoryService.getCategoryAndDeepChildrenCategoryById(category.getId()).getData();
        }

        if(StringUtils.isNotBlank(keyword)){

            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum, pageSize);

        //排序處理
        if(StringUtils.isNotBlank(orderBy)){

            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){

                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }

        List<Product> productList = productMapper.selectByNameAndCategoryIds(
                (StringUtils.isBlank(keyword)? null : keyword),
                categoryIdList.size()==0? null : categoryIdList);

        List<ProductListVo> productListVoList = Lists.newArrayList();

        for(Product product : productList){

            ProductListVo productListVo = this.assembleProductList(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

}
