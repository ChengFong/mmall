package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;

/**
 * Created by 林成峰 on 2017/8/3.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加分類
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse<String> addCategory(String categoryName, Integer parentId){

        if(parentId == null || StringUtils.isBlank(categoryName)){

            return ServerResponse.createByErrorMessage("添加商品參數錯誤");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); //這個分類是可用的

        int rowCount = categoryMapper.insert(category);

        if(rowCount > 0){

            return ServerResponse.createBySuccessMessage("添加品項成功");
        }

        return ServerResponse.createByErrorMessage("添加品項失敗");
    }

    /**
     * 更新分類名字功能
     *
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse<String> updateCategoryName(Integer categoryId, String categoryName){

        if(categoryId == null || StringUtils.isBlank(categoryName)){

            return ServerResponse.createByErrorMessage("更新商品參數錯誤");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);

        if(rowCount > 0){

            return ServerResponse.createBySuccessMessage("更新品類名子成功");
        }

        return ServerResponse.createByErrorMessage("更新品類名子失敗");
    }

    /**
     * 查詢節點
     *
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){

        List<Category> categoryList = categoryMapper.selectChildrenParallelId(categoryId);

        for(Category category: categoryList) {

            System.out.println(category);
        }

        if(CollectionUtils.isEmpty(categoryList)){

            logger.info("未找到當前分類的子分類");
        }

        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 遞歸查詢本節點的id及子節點的id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategoryById(Integer categoryId){

        Set<Category> categorySet = Sets.newHashSet();
        this.findChildCategory(categorySet, categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();

        for(Category category: categorySet){
            categoryIdList.add(category.getId());
        }

        return ServerResponse.createBySuccess(categoryIdList);
    }

    //遞歸算法，算出子節點
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId){

        Category category = categoryMapper.selectByPrimaryKey(categoryId);

        if(category != null){
            categorySet.add(category);
        }

        //查找子節點，遞歸演算法一定要有一個退出條件
        List<Category> categoryList = categoryMapper.selectChildrenParallelId(categoryId);

        for(Category categoryItem : categoryList){

            findChildCategory(categorySet, categoryItem.getId());
        }

        return  categorySet;
    }


}
