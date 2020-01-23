package com.show.sign.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 签名工具类
 * 
 * @author xuanweiyao
 * @date 10:01 2019/5/30
 */
@Slf4j
public class SignUtil {
    public static final String  apiSecret="wangyt";
    public static final String  apiKey="apiKey";


    public static String createSign(String path,String characterEncoding,Map<String, String> parameters){
///1、将所有业务请求参数按字母先后顺序排序.
        // 2、参数名称和参数值链接成一个字符串A
        String sign = SignUtil.getSig( path, characterEncoding,apiSecret, parameters);
        parameters.put("sign", sign); //API签名
        return  sign;
    }
    private static String getSig(String path,String characterEncoding,String apiSecret, Map<String, String> params) {

        ///1、将所有业务请求参数按字母先后顺序排序.
        // 2、参数名称和参数值链接成一个字符串A
        StringBuilder stringA = new StringBuilder();
        Set<String> keySet = new TreeSet<>(params.keySet());
        for (String key : keySet) {
            String value = params.get(key);
            if (value == null) {
                continue;
            }
            stringA.append(key);
            stringA.append("=");
            stringA.append(params.get(key));
            stringA.append("&");
        }
        stringA.setLength(stringA.length() - 1); // trim the last "&"
        log.info("参数名称和参数值链接成一个字符串A：" + stringA.toString());
        String sign = null;
        ///3、在字符串A的首尾加上apiSecret组成一个新字符串B
        StringBuilder stringB = new StringBuilder();
        stringB.append(apiSecret).append(stringA).append(apiSecret);
        try {
            //对字符串进行MD5散列运算得到签名sign，然后再进行Base64编码
            byte[] bytes = Base64.getEncoder().encode(MD5Utils.string2MD5(stringB.toString()).getBytes(characterEncoding));
            sign = new String(bytes, characterEncoding);
            return sign;
        } catch (Exception e) {
          log.error("getSig error: " + e.getMessage());
        }
        return sign;
    }
    public static void main(String[] args) {
        String token="eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE1Nzk2NzM0NTMsInN1YiI6IntcImV4cGlyZVRpbWVcIjozMTUzNjAwMDAwLFwicGFzc21kNVwiOlwiNWY0ZGNjM2I1YWE3NjVkNjFkODMyN2RlYjg4MmNmOTlcIixcInVybHN0clwiOlwiL3NpZ25UZXN0L2luZGV4XCJ9IiwiZXhwIjoxNTgyODI3MDUzfQ.WZi5ueFm-Xo3Rj16GCkHv3uEr3Z-V1C1GW6yDGQKjSU";
        Map<String, String> parameters = new HashMap<String,String>();
        parameters.put("username", "admin");
        parameters.put("password", "admin");
        parameters.put("key",apiKey);//标识
        parameters.put("sigVer", "1"); //签名版本
        parameters.put("token",token);
        //String ts = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS").format(LocalDateTime.now());
        String ts="2020-01-22";
        parameters.put("ts", ts); //时间戳
        String sign = SignUtil.createSign("","UTF-8",parameters);
        System.out.println(sign);
    }
    /**
     * 检查API签名是否合法
     * （1）客户端请求里面会携带签名（客户端利用apiSecret和给定的算法产生签名）
     * （2）服务器端会使用存在服务器端的apiSecret和相同的算法产生一个签名。
     * （3）服务器端对这两个签名进行校验，得出签名的有效性。如果有效，则正常走业务流程，否则拒绝请求。
     */

    public static boolean checkToken(HttpServletRequest request, String apiSecret) throws IllegalArgumentException {
        boolean tokenVerify = TokenUtils.verifySession(request);
        return tokenVerify;
    }

    public static boolean checkSig(HttpServletRequest request, String apiSecret) throws IllegalArgumentException {
       try {
           //1、得到请求方携带的API签名
           String clientSign = null;
           StringBuilder stringA = new StringBuilder();
           Enumeration<String> parameterNames = request.getParameterNames();
           Set<String> keySet = new TreeSet<String>();
            //2、将所有业务请求参数按字母先后顺序排序。
           while (parameterNames.hasMoreElements()) {
               String parameterName = parameterNames.nextElement();
               if (parameterName.equals("sign")) {
                //获取客户端的API签名
                   clientSign = request.getParameter("sign");
                   continue;
               }
               keySet.add(parameterName);
           }
            //3、参数名称和参数值链接成一个字符串A。
           for (String key : keySet) {
               String value = request.getParameter(key);
               if (value == null) {
                   continue;
               }
               stringA.append(key);
               stringA.append("=");
               stringA.append(value);
               stringA.append("&");
           }
           stringA.setLength(stringA.length() - 1); // trim the last "&"
            //服务器端根据参数生成的签名
           String serverSig = null;
            //Base64解码客户端的签名
           clientSign = new String(Base64.getDecoder().decode(clientSign), "UTF-8");
            //4、在字符串A的首尾加上apiSecret接口密匙组成一个新字符串B。
           StringBuilder stringB = new StringBuilder();
           stringB.append(apiSecret).append(stringA).append(apiSecret);
            //5、对新字符串B进行MD5散列运算生成服务器端的API签名，将客户端的API签名进行Base64解码，然后开始验证签名。
            //6、如果服务器端生成的API签名与客户端请求的API签名是一致的，则请求是可信的，否则就是不可信的。
           serverSig = MD5Utils.string2MD5(stringB.toString());
           return clientSign != null && serverSig != null && clientSign.equals(serverSig);
       }catch(Exception e){
           log.error("验证签名失败:{0}",e);
           return false;
       }
    }
    public static String getparamAndUrl(String url, Map<String, String> params){
        StringBuilder stringA = new StringBuilder();
        Set<String> keySet = new TreeSet<>(params.keySet());
        for (String key : keySet) {
            String value = params.get(key);
            if (value == null) {
                continue;
            }
            stringA.append(key);
            stringA.append("=");
            stringA.append(params.get(key));
            stringA.append("&");
        }
        stringA.setLength(stringA.length() - 1); // trim the last "&"
        return url.concat("?").concat(stringA.toString());
    }
}