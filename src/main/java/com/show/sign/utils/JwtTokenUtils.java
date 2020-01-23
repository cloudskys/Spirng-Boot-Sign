package com.show.sign.utils;

import com.alibaba.druid.util.Base64;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Slf4j
public class JwtTokenUtils {

    private final static String JWT_ENCRYPTION_KEY = "2345678901234567";

    /**
     * 生成加密字符串
     * @return
     */
    private static SecretKey generalKey(){
        byte[] encodedKey = Base64.base64ToByteArray(JWT_ENCRYPTION_KEY);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    /**
     * 创建jwt
     * @param subject
     * @param ttlMillis
     * @return
     */
    public static String createJWT(String subject, long ttlMillis) {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        SecretKey key = generalKey();
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setSubject(subject)
                .signWith(signatureAlgorithm, key);
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    /**
     * 解密jwt
     * @param jwt
     * @return
     */
    public static Claims parseJWT(String jwt) {
        SecretKey key = generalKey();

        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt).getBody();
    }

    /**
     * 是否过期
     * @param token
     * @return
     */
    public static Boolean isExpired(String token) {
        try {
            final Claims claims = parseJWT(token);
            return claims.getExpiration().before(new Date());
        }catch (Exception e) {
            log.error("解析JWT失败, token = ", token);
        }
        return true;
    }

    /**
     * 是否在时间之前
     * @param token
     * @param timestamp
     * @return
     */
    public static Boolean isBefore(String token, Long timestamp) {
        try {
            final Claims claims = parseJWT(token);
            return claims.getExpiration().before(new Date(timestamp));
        }catch (Exception e) {
            log.error("解析JWT失败, token = ", token);
        }
        return false;
    }

}