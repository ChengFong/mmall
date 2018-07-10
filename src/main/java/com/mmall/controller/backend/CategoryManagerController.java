package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import com.mmall.common.ServerResponse;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by 林成峰 on 2017/8/3.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManagerController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加分類
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        //校驗一下是否是管理員
        if(iUserService.checkAdminRole(user).isSuccess()){

            //是管理員
            //增加我們處理分類的邏輯
            return iCategoryService.addCategory(categoryName, parentId);

        }else{
            return ServerResponse.createByErrorMessage("無權限操作，需要管理員權限操作");
        }
    }

    /**
     * 更新分類名字功能
     *
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session, Integer categoryId, String categoryName){

                  User user = (User) session.getAttribute(Const.CURRENT_USER);

            if(user == null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
            }

            //校驗一下是否是管理員
            if(iUserService.checkAdminRole(user).isSuccess()){

            //更新categoryName
            return iCategoryService.updateCategoryName(categoryId, categoryName);

        }else{
            return ServerResponse.createByErrorMessage("無權限操作，需要管理員權限操作");
        }
    }

    /**
     * 查詢平級節點
     *
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,
                                                      @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        //校驗一下是否是管理員
        if(iUserService.checkAdminRole(user).isSuccess()){

            //查詢子節點的category信息，並且不遞歸，保持平級
            return ServerResponse.createBySuccess(iCategoryService.getChildrenParallelCategory(categoryId));

        }else{
            return ServerResponse.createByErrorMessage("無權限操作，需要管理員權限操作");
        }

    }

    /**
     * 遞歸查詢本節點的id及子節點的id
     *
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用戶未登錄，請登陸");
        }

        //校驗一下是否是管理員
        if(iUserService.checkAdminRole(user).isSuccess()){

            //查詢當前節點的id與遞歸節點的id
            return ServerResponse.createBySuccess(iCategoryService.getCategoryAndDeepChildrenCategoryById(categoryId));
        }else{
            return ServerResponse.createByErrorMessage("無權限操作，需要管理員權限操作");
        }
    }

}
