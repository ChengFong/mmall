package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by 林成峰 on 2017/7/31.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;

    /**
     * 登入功能
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {

        int resultCount = userMapper.checkUsername(username);

        if(resultCount == 0){

            return ServerResponse.createByErrorMessage("用戶名不存在");
        }

        //密碼登入MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);

        User user = userMapper.selectLogin(username, md5Password);

        if(user == null){

            return ServerResponse.createByErrorMessage("密碼錯誤");
        }

        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("登入成功", user);
    }

    /**
     * 註冊功能
     * @param user
     * @return
     */
    public ServerResponse<String> register(User user){

        //校驗用戶名
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!validResponse.isSuccess()){

            return validResponse;
        }

        //校驗信箱
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){

            return validResponse;
        }

        //設置用戶角色
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);

        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("註冊失敗");
        }

        return ServerResponse.createBySuccessMessage("註冊成功");
    }

    /**
     * 校验功能
     * @param str
     * @param type
     * @return
     */
    public ServerResponse<String> checkValid(String str, String type){

        if(org.apache.commons.lang3.StringUtils.isNotBlank(type)){

            //開始校驗
            if(Const.USERNAME.equals(type)){

                int resultCount = userMapper.checkUsername(str);

                if(resultCount > 0 ){

                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }

            if(Const.EMAIL.equals(type)){

                int resultCount = userMapper.checkEmail(str);

                if(resultCount > 0 ){

                    return ServerResponse.createByErrorMessage("Email已存在");
                }
            }
        }else{

            return ServerResponse.createByErrorMessage("參數錯誤");
        }

        return ServerResponse.createBySuccessMessage("校驗成功");
    }

    /**
     * 提示问题功能
     * @param username
     * @return
     */
    public ServerResponse<String> selectQuestion(String username){

        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);

        if(validResponse.isSuccess()){

            //用戶不存在
            ServerResponse.createByErrorMessage("用戶不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){

            return ServerResponse.createBySuccess(question);
        }

        return ServerResponse.createByErrorMessage("找回密碼的問題是空的");
    }

    /**
     * 確認問題答案，與產生token功能
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username, String question, String answer){

        int resultCount = userMapper.checkAnswer(username, question, answer);

        //說明問題及問題答案是這個用戶的，並且是正確的
        if(resultCount > 0){

            String forgetToken = UUID.randomUUID().toString();
            RedisPoolUtil.setEx(Const.TOKEN_PREFIX+username, forgetToken, 60*60*12);

            return ServerResponse.createBySuccess(forgetToken);
        }

        return ServerResponse.createByErrorMessage("問題的答案錯誤");
    }

    /**
     * 忘记密码中的重置密码功能开发
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){

        if(StringUtils.isBlank(forgetToken)){

            return ServerResponse.createByErrorMessage("參數錯誤，token需要傳遞");
        }

        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);

        if(validResponse.isSuccess()){

            //用戶不存在
            return ServerResponse.createByErrorMessage("用戶不存在");
        }

        String token = RedisPoolUtil.get(Const.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){

            return ServerResponse.createByErrorMessage("token過期或無效");
        }


        if(StringUtils.equals(forgetToken, token)){

            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);

            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);

            if(rowCount > 0){

                return ServerResponse.createBySuccessMessage("修改密碼成功");
            }
        }else{

            return ServerResponse.createByErrorMessage("token錯誤，請重新獲取密碼的token");
        }

        return ServerResponse.createByErrorMessage("修改密碼失敗");
    }

    /**
     * 登录状态下重置密码功能开发
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){

        //防止橫向越權，要校驗一下這個用戶的舊密碼，一定要指定是這個用戶，因此我們會查詢一個count(1)，
        //如果不指定id的話，那結果就是true，count > 0
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());

        if(resultCount == 0){

            return ServerResponse.createByErrorMessage("舊密碼錯誤");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));

        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){

            return ServerResponse.createBySuccessMessage("密碼更新成功");
        }

        return ServerResponse.createByErrorMessage("密碼更新失敗");
    }

    /**
     * 更新用戶個人信息功能開發
     * @param user
     * @return
     */
    public ServerResponse<User> updateInformation(User user){

        //username是不能被更新的
        //email也要進行校驗，校驗新的email是不是已經存在，如果存在的email相同的話，不能是我們當前用戶的。
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());

        if(resultCount > 0){

            return ServerResponse.createByErrorMessage("email已存在，請更換email在嘗試更新");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);

        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新個人信息成功", updateUser);
        }

        return ServerResponse.createByErrorMessage("更新個人信息失敗");
    }

    /**
     * 获取用户详细信息功能开发
     * @param userId
     * @return
     */
    public ServerResponse<User> getInformation(Integer userId){

        User user = userMapper.selectByPrimaryKey(userId);

        if(user == null){

            return ServerResponse.createByErrorMessage("找不到當前用戶");
        }

        //前端不會拿取到密碼，所以要置為空
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess(user);
    }

    //backend

    /**
     * 校驗是否是管理員
     * @param user
     * @return
     */
    public ServerResponse<String> checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return  ServerResponse.createByError();
    }

}
