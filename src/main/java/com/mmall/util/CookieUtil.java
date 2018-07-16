package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 林成峰 on 2018/4/7.
 */
public class CookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    private final static String COOKIE_DOAMIN = ".happymmall.com";
    private final static String COOKIE_NAME = "mmall_login_token";

    public static String readLoginToken(HttpServletRequest request){

        Cookie[] cks = request.getCookies();

        if(cks != null){

            for(Cookie ck : cks) {

                logger.info("write cookieName:{}, cookieValue:()", ck.getName(), ck.getValue());

                if(StringUtils.equals(ck.getName(), COOKIE_NAME)){

                    logger.info("return cookieName:{}, cookieValue:()", ck.getName(), ck.getValue());

                    return ck.getValue();
                }
            }
        }

        return null;
    }

    //X:domain=".happymmall.com"
    //a:A.happymmall.com            cookie:domain=A.happymmall.com;path="/"
    //b:B.hayypmmall.com            cookie:domain=B.happymmall.com;path="/"
    //c:A.happymmall.com/test/cc    cookie:domain=A.happymmall.com;path="/test/cc"
    //d:A.happymmall.com/test/dd    cookie:domain=A.happymmall.com;path="/test/dd"
    //e:A.happymmall.com/test       cookie:domain=A.happymmall.com;path="/test"

    //a與b不可以互相獲取cookie，因為不同domain。
    //c、d可以獲取e的cookie，因為位在子路徑下。

    public static void writeLoginToken(HttpServletResponse response, String token){

        Cookie ck = new Cookie(COOKIE_NAME, token);
        //ck.setDomain(COOKIE_DOAMIN);
        ck.setPath("/");//代表設置在根目錄
        ck.setHttpOnly(true);

        //單位是秒。
        //如果這個maxage不設置的話，cookie就不會寫入硬盤，而是寫在內存。只在當前頁面有效。
        ck.setMaxAge(60 * 60 * 24 *365);//如果是-1，代表永久
        logger.info("write cookieName:{}, cookieValue:()", ck.getName(), ck.getValue());

        response.addCookie(ck);
    }

    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response){

        Cookie[] cks = request.getCookies();

        if(cks != null){

            for(Cookie ck : cks) {

                if(StringUtils.equals(ck.getName(), COOKIE_NAME)){

                    //ck.setDomain(COOKIE_DOAMIN);
                    ck.setPath("/");
                    ck.setMaxAge(0); //設置成0，代表刪除此cookie。

                    logger.info("del cookieName:{}, cookieValue:()", ck.getName(), ck.getValue());

                    response.addCookie(ck);

                    return;
                }
            }
        }
    }

}
