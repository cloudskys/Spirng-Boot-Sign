package com.show.sign.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HttpRequestUtil.java描述了 发送http请求的类
 *
 * @author ccy
 * @version 1.0.0, 2015年2月9日
 *
 * @author xuyd 2017-03-22 增加字符编码
 */
public class HttpRequestUtil {
   public static final String UTF_8 = "UTF-8";
   /**
    * 向指定URL发送GET方法的请求
    *
    * @param url
    *            发送请求的URL
    * @param param
    *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
    * @return URL 所代表远程资源的响应结果
    */
   public static String sendGet(String url, String param) {
      return sendGet(url, param, "utf-8");
   }

   /**
    * 向指定URL发送GET方法的请求
    *
    * @param url
    *            发送请求的URL
    * @param param
    *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
    * @param charset
    *            读取字节流编码
    *
    * @return URL 所代表远程资源的响应结果
    */
   public static String sendGet(String url, String param, String charset) {
      String result = "";
      BufferedReader in = null;
      try {
         String urlNameString = url;
         if (param != null && param.length() > 0) {
            urlNameString += ("?" + param);
         }
         URL realUrl = new URL(urlNameString);
         // 打开和URL之间的连接
         URLConnection connection = realUrl.openConnection();
         // 建立实际的连接
         connection.connect();
         // 定义 BufferedReader输入流来读取URL的响应
         in = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
         String line;
         while ((line = in.readLine()) != null) {
            result += line;
         }
      } catch (Exception e) {
         throw new RuntimeException("http get请求失败", e);
      } finally {
         // 使用finally块来关闭输入流
         if (in != null) {
            try {
               in.close();
            } catch (Exception e2) {
            }
         }
      }
      return result;
   }

   /**
    * 向指定 URL 发送POST方法的请求
    *
    * @param url
    *            发送请求的 URL
    * @param params
    *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
    * @return 所代表远程资源的响应结果
    */
   public static String sendPost(String url, String params) {
      return sendPost(url, params, "utf-8", null);
   }

   /**
    * 向指定 URL 发送POST方法的请求
    *
    * @param url
    *            发送请求的 URL
    * @param params
    *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
    * @param headers
    *            请求头属性设置
    * @return 所代表远程资源的响应结果
    */
   public static String sendPost(String url, String params, Map<String, String> headers) {
      return sendPost(url, params, "utf-8", headers);
   }

   /**
    * 向指定 URL 发送POST方法的请求
    *
    * @param url
    *            发送请求的 URL
    * @param params
    *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
    * @param charset
    *            读取字节流编码
    * @return 所代表远程资源的响应结果
    */
   public static String sendPost(String url, String params, String charset, Map<String, String> headers) {
      PrintWriter out = null;
      BufferedReader in = null;
      String result = "";
      try {
         URL realUrl = new URL(url);
         // 打开和URL之间的连接
         URLConnection conn = realUrl.openConnection();
         if(headers != null && headers.size() > 0) {
            Iterator<String> iter = headers.keySet().iterator();
            while(iter.hasNext()) {
               String key = iter.next();
               String val = headers.get(key);
               conn.setRequestProperty(key, val);
            }
         }
         // 发送POST请求必须设置如下两行
         conn.setDoOutput(true);
         conn.setDoInput(true);
         // 获取URLConnection对象对应的输出流
         out = new PrintWriter(conn.getOutputStream());
         // 发送请求参数
         out.print(params);
         // flush输出流的缓冲
         out.flush();
         // 定义BufferedReader输入流来读取URL的响应
         in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
         String line;
         while ((line = in.readLine()) != null) {
            result += line;
         }
      } catch (Exception e) {
         throw new RuntimeException("http post请求失败", e);
      } finally {
         // 使用finally块来关闭输出流、输入流

         if (out != null) {
            out.close();
         }
         if (in != null) {
            try {
               in.close();
            } catch (IOException e2) {
            }
         }
      }
      return result;
   }

   /**
    * POST发送JSON对象
    *
    * @param url
    *            请求URL
    * @param json
    *            JSON格式的字符串
    * @return
    */
   public static String sendJsonPost(String url, String json) {
      org.apache.http.client.HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost(url);
      post.setHeader("Content-Type", "application/json");
      // post.addHeader("Authorization", "*");token验证
      InputStream inStream = null;
      String result = null;
      try {
         StringEntity se = new StringEntity(json, "utf-8");
         se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
         post.setEntity(se);
         HttpResponse httpResponse = client.execute(post);

         // 响应OK
         if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // 获取响应输入流
            inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));

            // 读取数据
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
               sb.append(line + "\n");
            }
            result = sb.toString();
         } else {
            // 未响应
         }
      } catch (Exception e) {
         throw new RuntimeException("http json post请求失败", e);
      } finally {
         if (inStream != null) {
            try {
               inStream.close();
            } catch (IOException e) {
            }
         }
      }
      return result;
   }

   /**
    *
    * @param url
    * @return
    */
   public static String sendHttpGet(String url) {
      String jsonResult = null;
      try {
         DefaultHttpClient client = new DefaultHttpClient();
         // 发送get请求
         HttpGet request = new HttpGet(url);
         HttpResponse response = client.execute(request);

         /** 请求发送成功，并得到响应 **/
         if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            /** 读取服务器返回过来的json字符串数据 **/
            jsonResult = EntityUtils.toString(response.getEntity());
            /** 把json字符串转换成json对象 **/
            // jsonResult = JSONObject.fromObject(strResult);
            url = URLDecoder.decode(url, "UTF-8");
         }
      } catch (IOException e) {
         throw new RuntimeException("方法#sendHttpGet请求失败", e);
      }
      return jsonResult;
   }
   public static Map<String, String> getGetResp(String url, List<NameValuePair> httpParameters) throws Exception {

      Map<String, String> map = new HashMap<String, String>();

      CloseableHttpClient httpClient = null;
      ByteArrayOutputStream exceptionOutputStream = null;
      CloseableHttpResponse httpResponse = null;
      PrintStream exceptionPrintStream = null;

      try {
         HttpGet httpGet = new HttpGet(url);
         httpClient = HttpClients.createDefault();

         String sendstr = "";
         if (httpParameters != null && !httpParameters.isEmpty()) {
            sendstr = EntityUtils.toString(new UrlEncodedFormEntity(httpParameters, UTF_8));
            httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + sendstr));
         } else {
            httpGet.setURI(new URI(httpGet.getURI().toString()));
         }

         map.put("requestUrl", url);
         map.put("requestMethod", httpGet.getMethod());
         map.put("requestTime", "2020-10-12 10:12:15");
         map.put("requestHeaders", getHeaders(httpGet.getAllHeaders()));
         map.put("requestBody", getBody(httpParameters).toString());

         httpResponse = httpClient.execute(httpGet);

         int responseStatus = httpResponse.getStatusLine().getStatusCode();
         String responseBody = EntityUtils.toString(httpResponse.getEntity(), "GB2312"/*UTF_8*/);

         map.put("responseStatus", String.valueOf(responseStatus));
         map.put("responseBody", responseBody);
         map.put("responseTime", "2020-01-23 12:10:13");
         map.put("responseHeaders", getHeaders(httpResponse.getAllHeaders()));

      } catch (Exception e) {
         e.printStackTrace();
         // 获取异常
         exceptionPrintStream = new PrintStream(exceptionOutputStream);
         exceptionOutputStream = new ByteArrayOutputStream();
         map.put("exception", new String(exceptionOutputStream.toByteArray()));
         e.printStackTrace(exceptionPrintStream);
      } finally {
         IOUtils.closeQuietly(httpResponse);
         IOUtils.closeQuietly(httpClient);
         IOUtils.closeQuietly(exceptionPrintStream);
         IOUtils.closeQuietly(exceptionOutputStream);
      }

      return map;
   }
   private  static  String getHeaders(Header... headers){
      StringBuffer headerstr = new StringBuffer();
      if (headers != null && headers.length >0){
         for (Header header: headers){
            headerstr.append(header.getName());
            headerstr.append("→");
            headerstr.append(header.getValue());
            headerstr.append("|");
         }
      }
      return headerstr.toString();
   }
   private  static  String getBody(List<NameValuePair> httpParameters){
      if (httpParameters == null || httpParameters.isEmpty()){
         return "";
      }
      StringBuffer requestBody=new StringBuffer();
      Iterator it= httpParameters.iterator();
      while(it.hasNext()) {
         NameValuePair parameter = (NameValuePair)it.next();
         String encodedName = parameter.getName();
         String encodedValue = parameter.getValue();
         if(requestBody.length() > 0) {
            requestBody.append("&");
         }
         requestBody.append(encodedName);
         if(encodedValue != null) {
            requestBody.append("=");
            requestBody.append(encodedValue);
         }
      }
      return  requestBody.toString();
   }
   public static Map<String, String> getGetResp(String url, List<NameValuePair> httpParameters,String charset) throws Exception {

      Map<String, String> map = new HashMap<String, String>();

      CloseableHttpClient httpClient = null;
      ByteArrayOutputStream exceptionOutputStream = null;
      CloseableHttpResponse httpResponse = null;
      PrintStream exceptionPrintStream = null;

      try {
         HttpGet httpGet = new HttpGet(url);
         httpClient = HttpClients.createDefault();

         String sendstr = "";
         if (httpParameters != null && !httpParameters.isEmpty()) {
            sendstr = EntityUtils.toString(new UrlEncodedFormEntity(httpParameters, UTF_8));
            httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + sendstr));
         } else {
            httpGet.setURI(new URI(httpGet.getURI().toString()));
         }

         map.put("requestUrl", url);
         map.put("requestMethod", httpGet.getMethod());
         map.put("requestTime", "2020-01-23 12:12:19");
         map.put("requestHeaders", getHeaders(httpGet.getAllHeaders()));
         map.put("requestBody", getBody(httpParameters).toString());

         httpResponse = httpClient.execute(httpGet);

         int responseStatus = httpResponse.getStatusLine().getStatusCode();
         String responseBody = EntityUtils.toString(httpResponse.getEntity(), charset);

         map.put("responseStatus", String.valueOf(responseStatus));
         map.put("responseBody", responseBody);
         map.put("responseTime", "2020-01-23 12:12:19");
         map.put("responseHeaders", getHeaders(httpResponse.getAllHeaders()));

      } catch (Exception e) {
         e.printStackTrace();
         // 获取异常
         exceptionPrintStream = new PrintStream(exceptionOutputStream);
         exceptionOutputStream = new ByteArrayOutputStream();
         map.put("exception", new String(exceptionOutputStream.toByteArray()));
         e.printStackTrace(exceptionPrintStream);
      } finally {
         IOUtils.closeQuietly(httpResponse);
         IOUtils.closeQuietly(httpClient);
         IOUtils.closeQuietly(exceptionPrintStream);
         IOUtils.closeQuietly(exceptionOutputStream);
      }

      return map;
   }
}

