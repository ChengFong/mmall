package com.mmall.controller.backend;

import com.mmall.common.ServerResponse;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 分類接口
 *
 * Created by 林成峰 on 2017/8/3.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManagerController {


    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加分類
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(String categoryName, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId){

        return iCategoryService.addCategory(categoryName, parentId);
    }

    /**
     * 更新分類名字功能
     *
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName){


        return iCategoryService.updateCategoryName(categoryId, categoryName);
    }

    /**
     * 查詢平級節點
     *
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory( @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){

        //查詢子節點的category信息，並且不遞歸，保持平級
        return ServerResponse.createBySuccess(iCategoryService.getChildrenParallelCategory(categoryId));
    }

    /**
     * 遞歸查詢本節點的id及子節點的id
     *
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){

        //查詢當前節點的id與遞歸節點的id
        return ServerResponse.createBySuccess(iCategoryService.getCategoryAndDeepChildrenCategoryById(categoryId));
    }

}
