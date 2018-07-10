package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 後台用戶接口
 *
 * Created by 林成峰 on 2017/8/2.
 */
@Controller
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用戶登入
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){

        ServerResponse<User> response = iUserService.login(username, password);

        if(response.isSuccess()){

            User user = response.getData();

            if (user.getRole() == Const.Role.ROLE_ADMIN){

                //說明登入的是管理員
                session.setAttribute(Const.CURRENT_USER, user);

                return response;
            }
            else{

                return ServerResponse.createByErrorMessage("不是管理員，無法登入");
            }
        }

        return response;
    }
}
