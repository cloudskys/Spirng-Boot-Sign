package com.show.sign.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 短连接生成
 */
public class ShortUrlUtil {


   private final String redisconfig;
   private final Jedis jedis;
   public ShortUrlUtil(String redisconfig){
      this.redisconfig=redisconfig;
      this.jedis=new Jedis(this.redisconfig);
   }
   public static String sinaShortUrlAPI(String url) throws  Exception {
      if(null == url){
         return "";
      }
      List<NameValuePair> pairs = new ArrayList<>();
      //设置接口返回格式
      pairs.add(new BasicNameValuePair("format", "json"));
      pairs.add(new BasicNameValuePair("key", "5e2906893a005a3c52923648@d177b674097a5d58e2ec57c7a967c1c6"));
      pairs.add(new BasicNameValuePair("url", url));
      Map<String, String> rst;
      try {
         rst = HttpRequestUtil.getGetResp("http://suo.im/api.htm", pairs, "UTF-8");
      } catch (Exception e) {
         return "";
      }
      JSONObject json = JSON.parseObject(rst.get("responseBody"));
      if(StringUtils.isEmpty(json.getString("err"))){
         String shortUrl = json.getString("url");
         if(StringUtils.isEmpty(shortUrl)){
            return "";
         }else{
            return shortUrl;
         }
      }else{
         return "err";
      }
   }

   public static String gererateShortUrl(String url) {
      // 可以自定义生成 MD5 加密字符传前的混合 KEY
      String key = "caron" ;
      // 要使用生成 URL 的字符
      String[] chars = new String[] { "a" , "b" , "c" , "d" , "e" , "f" , "g" , "h" ,
              "i" , "j" , "k" , "l" , "m" , "n" , "o" , "p" , "q" , "r" , "s" , "t" ,
              "u" , "v" , "w" , "x" , "y" , "z" , "0" , "1" , "2" , "3" , "4" , "5" ,
              "6" , "7" , "8" , "9" , "A" , "B" , "C" , "D" , "E" , "F" , "G" , "H" ,
              "I" , "J" , "K" , "L" , "M" , "N" , "O" , "P" , "Q" , "R" , "S" , "T" ,
              "U" , "V" , "W" , "X" , "Y" , "Z"

      };
      // 对传入网址进行 MD5 加密
      String sMD5EncryptResult = MD5Utils.MD5(key+url);
      String hex = sMD5EncryptResult;

//        String[] resUrl = new String[4];
//        for ( int i = 0; i < 4; i++) {

      // 把加密字符按照 8 位一组 16 进制与 0x3FFFFFFF 进行位与运算
      String sTempSubString = hex.substring(2 * 8, 2 * 8 + 8);    //固定取第三组

      // 这里需要使用 long 型来转换，因为 Inteper .parseInt() 只能处理 31 位 , 首位为符号位 , 如果不用 long ，则会越界
      long lHexLong = 0x3FFFFFFF & Long.parseLong (sTempSubString, 16);
      String outChars = "" ;
      for ( int j = 0; j < 6; j++) {
         // 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
         long index = 0x0000003D & lHexLong;
         // 把取得的字符相加
         outChars += chars[( int ) index];
         // 每次循环按位右移 5 位
         lHexLong = lHexLong >> 5;
      }
      // 把字符串存入对应索引的输出数组
//            resUrl[i] = outChars;
//        }
      return outChars;
   }

   public static void main(String[] args) {
      String sLongUrl = "http://474515923.qzone.qq.com" ; //长链接
      String result = gererateShortUrl(sLongUrl);
      System.out.println(result);
   }
}

