package com.show.sign.utils;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class TokenUtils {

    private final static String LOGIN_TOKEN_NAME = "token";

    /**
     * 解密token
     * @param token
     * @return
     */
    public static ApiToken parseToken(String token) {
        Claims claims  = JwtTokenUtils.parseJWT(token);
        return JSONObject.parseObject(claims.getSubject(), ApiToken.class);
    }

    public static String createToken(ApiToken user) {
        return JwtTokenUtils.createJWT(JSONObject.toJSONString(user), user.getExpireTime());
    }

    public static boolean verifySession(HttpServletRequest req) {
        try {
            String tokenStr = req.getHeader(LOGIN_TOKEN_NAME);
            return tokenStr != null && !tokenStr.isEmpty() && !JwtTokenUtils.isExpired(tokenStr);
        } catch (Exception e) {
            log.error("【CLIENT TOKEN】error, e = {}", e);
        }
        return false;
    }

    public static ApiToken getSessionUser(HttpServletRequest req) {
        String tokenStr = req.getHeader(LOGIN_TOKEN_NAME);
        if (tokenStr != null) {
            return parseToken(tokenStr);
        }
        return null;
    }

}