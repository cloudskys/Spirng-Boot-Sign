package com.show.sign.utils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class StringUtils {
   public static boolean isNotEmpty(Object[] array) {
      return (array != null && array.length != 0);
   }

   /**
    * 字符串是否为空或者空串
    *
    * @param val 字符串
    * @return 空或者空串：true
    */
   public static boolean isEmpty(Object val) {
      return (val == null || val.toString().length() <= 0);
   }

   /**
    * 返回字符串
    *
    * @param o 对象
    * @return 字符串
    */
   public static String toString(Object o) {
      if (isEmpty(o)) {
         return null;
      }
      String val = (o instanceof String ? ((String) o).toString()
              : (o instanceof Integer ? "" + ((Integer) o).intValue()
              : (o instanceof Long ? "" + ((Long) o).longValue()
              : (o instanceof Double ? "" + ((Double) o).doubleValue()
              : (o instanceof Float ? "" + ((Float) o).floatValue()
              : (o instanceof Short ? "" + ((Short) o).shortValue()
              : (o instanceof Byte ? "" + ((Byte) o).byteValue()
              : (o instanceof Boolean ? ((Boolean) o).toString()
              : (o instanceof Character ? ((Character) o).toString()
              : o.toString())))))))));
      return val;
   }


   /**
    * 返回int
    *
    * @param o 对象
    * @return int
    */
   public static int toInt(Object o) {
      if (o == null) {
         return 0;
      }
      String val = o.toString();
      // NumberFormatException
      int ret = Integer.valueOf(val);
      return ret;
   }

   /**
    * 十进制转二进制
    *
    * @param num
    * @return
    */
   public static String convertToBinary(int num) {
      return Integer.toBinaryString(num);
   }

   /**
    * 十进制转二进制
    *
    * @param num    被转换的数字
    * @param length 返回的二进制的位数
    * @return
    */
   public static String convertToBinary(int num, int length) {
      String binary = convertToBinary(num);
      int diff = length - binary.length();
      if (diff <= 0)
         return binary;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < diff; i++) {
         sb.append("0");
      }
      sb.append(binary);
      return sb.toString();
   }

   /**
    * 二进制转十进制
    *
    * @param num
    * @return
    */
   public static Integer convertToDecimal(String num) {
      return Integer.valueOf(num, 2);
   }

   public static boolean isNotEmpty(String str) {
      return !StringUtils.isEmpty(str);
   }

   public static boolean isEmpty(String str) {
      return str == null || str.length() == 0;
   }

   /**
    * 获取随机字符串(包括数字和大写字母)
    *
    * @param length
    * @return
    */
   public static String getRandomString(int length) {
      String allStr = "23456789abcdefghijkmnpqrstuvwxyz";
      StringBuffer sb = new StringBuffer("");
      Random random = new Random();
      while (length > 0) {
         sb.append(allStr.charAt(random.nextInt(allStr.length())));
         length--;
      }
      return sb.toString();
   }

   /**
    * 生成随机数
    *
    * @return
    */
   public static String generateRandom(int len) {
      Random r = new Random();
      StringBuilder s = new StringBuilder();
      do {
         s.append(r.nextInt(10));
      } while (s.length() < len);
      return s.toString();
   }

   public static String clearHtml(String StrHtml) {
      StrHtml = StrHtml.replaceAll("<script[^>]*?>.*?</script>", "");
      StrHtml = StrHtml.replaceAll("<(.[^>]*)>", "");
      StrHtml = StrHtml.replaceAll("([\\r\\n])[\\s]+", "");
      StrHtml = StrHtml.replaceAll("-->", "");
      StrHtml = StrHtml.replaceAll("<!--.*", "");
      StrHtml = StrHtml.replaceAll("&(quot|#34);", "\\");
      StrHtml = StrHtml.replaceAll("&(amp|#38);", "&");
      StrHtml = StrHtml.replaceAll("&(lt|#60);", "<");
      StrHtml = StrHtml.replaceAll("&(gt|#62);", ">");
      StrHtml = StrHtml.replaceAll("&(nbsp|#160);", "");
      StrHtml = StrHtml.replaceAll("&(iexcl|#161);", "\\xa1");
      StrHtml = StrHtml.replaceAll("&(cent|#162);", "\\xa2");
      StrHtml = StrHtml.replaceAll("&(pound|#163);", "\\xa3");
      StrHtml = StrHtml.replaceAll("&(copy|#169);", "\\xa9");
      StrHtml = StrHtml.replaceAll("&(gt|#62);", ">");
      StrHtml = StrHtml.replaceAll("&#(\\d+);", "");
      return StrHtml.trim();

   }

   public static int ipToInt(String strIp) {
      int[] ip = new int[4];
      int position1 = strIp.indexOf(".");
      int position2 = strIp.indexOf(".", position1 + 1);
      int position3 = strIp.indexOf(".", position2 + 1);
      ip[0] = Integer.parseInt(strIp.substring(0, position1));
      ip[1] = Integer.parseInt(strIp.substring(position1 + 1, position2));
      ip[2] = Integer.parseInt(strIp.substring(position2 + 1, position3));
      ip[3] = Integer.parseInt(strIp.substring(position3 + 1));
      return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
   }

   public static String intToIP(int intIp) {
      StringBuffer sb = new StringBuffer("");
      // 直接右移24位
      sb.append(String.valueOf((intIp >>> 24)));
      sb.append(".");
      // 将高8位置0，然后右移16位
      sb.append(String.valueOf((intIp & 0x00FFFFFF) >>> 16));
      sb.append(".");
      // 将高16位置0，然后右移8位
      sb.append(String.valueOf((intIp & 0x0000FFFF) >>> 8));
      sb.append(".");
      // 将高24位置0
      sb.append(String.valueOf((intIp & 0x000000FF)));
      return sb.toString();
   }

   public static String addBackslash(String str) {
      if (str == null) {
         return null;
      }
      // + - && || ! ( ) { } [ ] ^ ” ~ * ? : \
      str = str.replaceAll("\\\\", "\\\\\\\\");
      str = str.replaceAll("\\+", "\\\\+");
      str = str.replaceAll("-", "\\\\-");
      str = str.replaceAll("&", "\\\\&");
      str = str.replaceAll("\\|", "\\\\|");
      str = str.replaceAll("!", "\\\\!");
      str = str.replaceAll("\\(", "\\\\(");
      str = str.replaceAll("\\)", "\\\\)");
      str = str.replaceAll("\\{", "\\\\{");
      str = str.replaceAll("\\}", "\\\\}");
      str = str.replaceAll("\\[", "\\\\[");
      str = str.replaceAll("\\]", "\\\\]");
      str = str.replaceAll("\\^", "\\\\^");
      str = str.replaceAll("\\\"", "\\\\\"");
      str = str.replaceAll("~", "\\\\~");
      str = str.replaceAll("\\*", "\\\\*");
      str = str.replaceAll("\\?", "\\\\?");
      str = str.replaceAll(":", "\\\\:");
      return str;
   }










   /**
    * 生成随机ID
    *
    * @return
    */
   public static String generateId() {
      return UUID.randomUUID().toString().replace("-", "");
   }

   /**
    * 生成伪用户ID：9+(8位)随机数
    *
    * @return
    */
   public static String generateFakeUserId() {
      StringBuilder sb = new StringBuilder();
      Random rd = new Random();
      sb.append(String.format("9%08d", rd.nextInt(99999999)));
      return sb.toString();
   }

   /**
    * 手机中间段加星号
    *
    * @param mobilePhone
    * @return
    */
   public static String starPhone(String mobilePhone) {
      if (mobilePhone == null || mobilePhone.length() == 0) {
         return mobilePhone;
      }
      String result = mobilePhone;
      if (mobilePhone.contains(" ") || mobilePhone.length() != 11) {
         String temp1 = "";
         String temp2 = mobilePhone;
         if (mobilePhone.split(" ").length > 1) {
            temp1 = mobilePhone.split(" ")[0] + " ";
            temp2 = mobilePhone.split(" ")[1];
         }
         if (temp2.length() > 3) {
            int first = temp2.length() / 3;
            int second = temp2.length() - first * 2;
            temp2 = temp2.replaceAll("(\\d{" + first + "})\\d{" + second + "}(\\d{" + first + "})", "$1****$2");
         }
         result = temp1 + temp2;
      } else {
         result = mobilePhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
      }
      return result;
   }

   /**
    * 正则匹配是否是国内手机号
    *
    * @param val 匹配的值
    * @return boolean
    */
   public static boolean isMobilephoneCN(String val) {
      String reg = "^((1[3,5,8][0-9])|(14[5,7])|(17[0,6,7,8])|(19[7]))\\d{8}$";
      return isMatch(reg, val);
   }

   /**
    * 正则匹配
    *
    * @param reg 正则
    * @param val 匹配的值
    * @return boolean
    * @see #isMatch(String, String, boolean)
    */
   public static boolean isMatch(String reg, String val) {
      return isMatch(reg, val, false);
   }

   /**
    * 正则匹配
    *
    * @param reg             正则
    * @param val             匹配的值
    * @param caseInsensitive 是否不区分大小写,true:不区分
    * @return boolean
    */
   public static boolean isMatch(String reg, String val, boolean caseInsensitive) {
      if (reg == null) {
         return false;
      }
      Pattern pattern;
      if (caseInsensitive) {
         pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
      } else {
         pattern = Pattern.compile(reg);
      }
      return pattern.matcher(val).matches();
   }

   /**
    * 把对象转成字符串
    *
    * @param obj
    * @return String
    */

   public static String convertString(Object obj) {
      if (obj == null) {
         return "";
      }
      try {
         return obj.toString();
      } catch (NumberFormatException e) {
         return "";
      }
   }


   /**
    * URL后面拼接参数【使用encodeURIComponent】
    *
    * @param url 指定的url
    * @param key 参数key
    * @param val 参数值
    * @return 拼接后的url
    */
   public static String addUrlParam(String url, String key, String val) {
      if (StringUtils.isEmpty(key)) {
         return url;
      }
      if (StringUtils.isEmpty(val)) {
         val = "";
      }

      // #以后不作处理，直接拼接上
      String urlAddr = url;
      String urlAnchor = "";
      int pos = url.indexOf("#");
      if (pos >= 0) {
         urlAddr = url.substring(0, pos);
         urlAnchor = url.substring(pos);// 包含了#符号
      }
      if (urlAddr.indexOf('?') > -1) {
         urlAddr += ('&' + key + '=' + val);
      } else {
         urlAddr += ('?' + key + '=' + val);
      }
      return urlAddr + urlAnchor;
   }


   /**
    * URL后面拼接参数【使用encodeURIComponent】
    *
    * @param url 指定的url
    * @param key 参数key
    * @param val 参数值
    * @return 拼接后的url
    */
   public static String addShareUrlParam(String url, String key, String val) {
      if (StringUtils.isEmpty(key)) {
         return url;
      }
      if (StringUtils.isEmpty(val)) {
         val = "";
      }

      // #也是url一部分
      if (url.indexOf('?') > -1) {
         url += ('&' + key + '=' + val);
      } else {
         url += ('?' + key + '=' + val);
      }
      return url;
   }

   /**
    * 对象clone
    *
    * @param obj
    * @param <T>
    * @return
    */
   public static <T extends Serializable> T clone(T obj) {
      T cloneObj = null;
      try {
         // 写入字节流
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         ObjectOutputStream obs = new ObjectOutputStream(out);
         obs.writeObject(obj);
         obs.close();

         // 分配内存，写入原始对象，生成新对象
         ByteArrayInputStream ios = new ByteArrayInputStream(out.toByteArray());
         ObjectInputStream ois = new ObjectInputStream(ios);
         // 返回生成的新对象
         cloneObj = (T) ois.readObject();
         ois.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return cloneObj;
   }

   /**
    * 拼接redis key
    *
    * @param keys 各个key数组
    * @return
    */
   public static String makeRedisKey(String... keys) {
      String key = org.apache.commons.lang3.StringUtils.join(keys, ":");
      return key;
   }

   /**
    * 拼接Map成HTML参数结构<br>
    * key1=val1&key2=val2...(key1，key2必须是按照ASCII码升序排列)
    *
    * @param params
    *            参数Map对象
    * @return
    */
   public static String concatMap2Html(Map<String, Object> params) {
      if (params == null) {
         return null;
      }
      StringBuilder content = new StringBuilder();
      Set<String> keySet = params.keySet();
      Iterator<String> iter = keySet.iterator();
      int i = 0;
      while (iter.hasNext()) {
         String key = iter.next();
         String val = StringUtils.toString(params.get(key));
         if (i != 0) {
            content.append("&");
         }
         content.append(key + "=" + val);
         i++;
      }
      return content.toString();
   }

   /**
    * List类型转成Map
    *
    * @param l         list类型对象
    * @param clazz     value的class类型
    * @param keyGetter key获取的方法名
    * @param <T>
    * @param <V>
    * @return
    */
   public static <T, V> Map<T, V> listToMap(List<V> l, Class<V> clazz, String keyGetter) {
      if (l == null) {
         return null;
      }

      Map<T, V> map = new HashMap<>();
      try {
         Method method = clazz.getDeclaredMethod(keyGetter);
         for (V v : l) {
            Object obj = method.invoke(v);
            T key = (T) obj;
            map.put(key, v);
         }
      } catch (Exception e) {
         // 只要转换异常就返回null
         return null;
      }
      return map;
   }

   /**
    * List类型转成Map List
    *
    * @param l         list类型对象
    * @param clazz     value的class类型
    * @param keyGetter key获取的方法名
    * @param <T>
    * @param <V>
    * @return
    */
   public static <T, V> Map<T, List<V>> listToMapList(List<V> l, Class<V> clazz, String keyGetter) {
      if (l == null) {
         return null;
      }

      Map<T, List<V>> map = new HashMap<>();
      try {
         Method method = clazz.getDeclaredMethod(keyGetter);
         for (V v : l) {
            T key;
            Object obj = method.invoke(v);
            key = (T) obj;
            List<V> list = map.get(key);
            if (list == null) {
               list = new ArrayList<>();
               map.put(key, list);
            }
            list.add(v);
         }
      } catch (Exception e) {
         // 只要转换异常就返回null
         return null;
      }
      return map;
   }

   /**
    * 字符串ids转成List
    *
    * @param ids
    * @return
    */
   public static List<Integer> toIntList(String ids) {
      List<Integer> idsList = null;
      if (ids != null) {
         idsList = new ArrayList<>();
         String[] strIds = ids.split(",");
         for (String id : strIds) {
            if (!StringUtils.isEmpty(id)) {
               idsList.add(Integer.valueOf(id));
            }
         }
      }
      return idsList;
   }


   public static void main(String[] args) {
//		String s="5349443732397646558001874";
//		System.out.println(makeRedisKey("aaa","bbb","cc"));
//		System.out.println(generateFakeUserId());

//		java.util.List<String> f = new java.util.ArrayList<String>();
//		String v = null;
//		for(int i = 0; i < 1; i++) {
//			v = generateFakeUserId();
//			System.out.println(v);
//		}
//
//		String card = "622202**1239";
//		String[] c = card.split("\\*");
//		java.util.Date d = new Date();
//		long msTime = 1503409560000L;
//		msTime = 1503409440000L;
//		java.util.Calendar calendar = java.util.Calendar.getInstance();
//		calendar.setTimeInMillis(msTime);
//		System.out.println(com.heyou.util.DateUtil.formatDate(calendar.getTime(), com.heyou.util.DateUtil.Y_M_D_HMS));
//
//		String dd = "20170808131829";
//		System.out.println(com.heyou.util.DateUtil.formatStringToDate(dd, com.heyou.util.DateUtil.YMDHMS));
//		System.out.println(c[1]);

//		System.out.println(isMobilephoneCN("15917955923"));
   }
}

