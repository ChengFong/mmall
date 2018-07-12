package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
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
 * 用戶接口
 *
 * Created by 林成峰 on 2017/7/31.
 */
@Controller
@RequestMapping("/user/")
public class UserController {

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

            session.setAttribute(Const.CURRENT_USER, response.getData());
        }

        return response;
    }

    /**
     * 登出
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do")
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){

        session.removeAttribute(Const.CURRENT_USER);

        return ServerResponse.createBySuccess("退出成功");
    }

    /**
     * 註冊
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){

        return iUserService.register(user);
    }

    /**
     * 校驗功能開發
     *
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){

        return iUserService.checkValid(str, type);
    }


    /**
     * 獲取用戶登入信息
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do")
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user != null){

            return ServerResponse.createBySuccess(user);
        }

        return ServerResponse.createByErrorMessage("用戶未登入，無法獲取當前用戶的信息");
    }

    /**
     * 提示問題
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do")
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){

        return iUserService.selectQuestion(username);
    }


    /**
     * 確認問題答案，與產生token
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do")
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer){

        return iUserService.checkAnswer(username, question, answer);
    }

    /**
     * 忘記密碼中的重置密碼功能開發
     *
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken){

        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 登錄狀態下重置密碼功能開發
     *
     * @param session
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew){

        User user = (User)session.getAttribute(Const.CURRENT_USER);

        if(user == null){

            return ServerResponse.createByErrorMessage("用戶未登入");
        }

        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }

    /**
     * 更新用戶個人信息功能開發
     *
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user){

        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);

        if(currentUser == null){

            return ServerResponse.createByErrorMessage("用戶未登錄");
        }

        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());

        ServerResponse<User> response = iUserService.updateInformation(user);

        if(response.isSuccess()){

            //更新過後的user不包含username所以要重新加進去
            response.getData().setUsername(currentUser.getUsername());

            //更新session為最新的user
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }

        return response;
    }

    /**
     * 獲取用戶詳細信息功能開發
     * @param session
     * @return
     */
    @RequestMapping(value = "get_information.do")
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session){

        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);

        if(currentUser == null){

            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "未登錄，需要強制登陸status=10");
        }

        return iUserService.getInformation(currentUser.getId());
    }
}
