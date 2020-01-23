package com.show.sign.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.util.FileCopyUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
/*
 * 利用HttpClient进行post请求的工具类
 */
@Slf4j
public class HttpsClientUtil {
	

	private static PoolingHttpClientConnectionManager connMgr;  
    private static RequestConfig requestConfig = null;
	private static final int MAX_READ_TIMEOUT = 10000;		//读取等待时间
	private static final int MAX_CONNECTION_TIMEOUT = 1000;		//链接超时时间
	private static final int MAX_FROM_POOL_TIMEOUT = 5000;		//链接在线程池等待时间
	private static final int MAX_SIZE = 600;				//最大链接数
	private static final int MAX_PER_ROUTE_SIZE = 200;		//访问具体一个url最大链接数  比如访问www.baidu.com 最多只有40个链接可以访问www.baidu.com
	private static HttpClientBuilder HttpClientBuilder = null;
	private static CloseableHttpClient CLIENT = null;
	static{
		HttpClientBuilder b = HttpClientBuilder.create();
		SSLContext sslContext = null;
		try {
			sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			        return true;
			    }
			}).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		b.setSslcontext(sslContext);

        // don't check Hostnames, either.
        //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

        // here's the special part:
        //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
        //      -- and create a Registry, to register it.
        //
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();
		
		ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {  
			  
		    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {  
		        // Honor 'keep-alive' header  
		        HeaderElementIterator it = new BasicHeaderElementIterator(  
		                response.headerIterator(HTTP.CONN_KEEP_ALIVE));  
		        while (it.hasNext()) {  
		            HeaderElement he = it.nextElement();  
		            String param = he.getName();  
		            String value = he.getValue();  
		            if (value != null && param.equalsIgnoreCase("timeout")) {  
		                try {  
		                    return Long.parseLong(value) * 1000;  
		                } catch(NumberFormatException ignore) {  
		                }  
		            }  
		        }  
		        HttpHost target = (HttpHost) context.getAttribute(  
		                HttpClientContext.HTTP_TARGET_HOST);
		        return 6 * 1000;  
		    }  
		  
		};
		
		// 设置连接池  
        connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);  
        // 设置连接池大小  
        connMgr.setMaxTotal(MAX_SIZE);  
        connMgr.setDefaultMaxPerRoute(MAX_PER_ROUTE_SIZE);  
  
        RequestConfig.Builder configBuilder = RequestConfig.custom();  
        // 设置连接超时  
        configBuilder.setConnectTimeout(MAX_CONNECTION_TIMEOUT);  
        // 设置读取超时  
        configBuilder.setSocketTimeout(MAX_READ_TIMEOUT);  
        // 设置从连接池获取连接实例的超时  
        configBuilder.setConnectionRequestTimeout(MAX_FROM_POOL_TIMEOUT);  
        
        configBuilder.setExpectContinueEnabled(true);
        configBuilder.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST));
        configBuilder.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC));
        
        requestConfig = configBuilder.build();
        HttpClientBuilder = HttpClients.custom()
        .setKeepAliveStrategy(myStrategy)
        .setConnectionManager(connMgr).setConnectionManagerShared(true);
        CLIENT = HttpClientBuilder.build();
        Thread connectionMonitorThread = new Thread(new IdleConnectionMonitorThread(connMgr));
        connectionMonitorThread.start();
	}
	
	public static CloseableHttpClient getCloseableHttpClient(){
		return HttpClientBuilder.build();
	}
	
	public static RequestConfig getRequestConfig(){
		return requestConfig;
	}
	
	public static String executePost(String uri, Map<String, Object> params){
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;");
		httpPost.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			for(String name: params.keySet()){
				urlParameters.add(new BasicNameValuePair(name, params.get(name)+""));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
			response = CLIENT.execute(httpPost);
		    int statusCode = response.getStatusLine().getStatusCode();
		    if(statusCode == 200){
		    	HttpEntity respEntity = response.getEntity();
		    	String str = EntityUtils.toString(respEntity);
		    	return str;
		    }
		}catch(Exception e){
			log.error("executePost url="+uri, e);
		} finally {
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.error("executePost consume url="+uri, e);
				}
			}
		}
		return null;
	}
	
	public static String executePostJson(String uri, String requestBody){
		return executePost(uri, requestBody, "application/json");
	}
	
	public static String executePost(String uri, String requestBody){
		return executePost(uri, requestBody, null);
	}
	
	public static String executePost(String uri, String requestBody, String contentType){
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.102 Safari/537.36");
		if(contentType != null){
			httpPost.addHeader("Content-Type", contentType);
		}
		httpPost.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			HttpEntity entity = new StringEntity(requestBody, "utf-8");
			httpPost.setEntity(entity);
			response = CLIENT.execute(httpPost);
		    int statusCode = response.getStatusLine().getStatusCode();
		    if(statusCode == 200){
		    	HttpEntity respEntity = response.getEntity();
		    	String str = EntityUtils.toString(respEntity);
		    	return str;
		    }
		}catch(Exception e){
			log.error("executePost url="+uri, e);
		} finally {
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (Exception e) {
					log.error("consume executePost url="+uri, e);
				}
			}
		}
		return null;
	}
	
	public static String executePostOfHeader(String uri, String requestBody, Map<String, String> headers){
		HttpPost httpPost = new HttpPost(uri);
		if(headers != null){
			for(String key: headers.keySet()){
				httpPost.addHeader(key, headers.get(key));
			}
		}
		httpPost.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			HttpEntity entity = new StringEntity(requestBody, "utf-8");
			httpPost.setEntity(entity);
			response = CLIENT.execute(httpPost);
		    int statusCode = response.getStatusLine().getStatusCode();
		    if(statusCode == 200){
		    	HttpEntity respEntity = response.getEntity();
		    	String str = EntityUtils.toString(respEntity);
		    	return str;
		    }
		}catch(Exception e){
			log.error("executePost url="+uri, e);
		} finally {
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (Exception e) {
					log.error("consume executePost url="+uri, e);
				}
			}
		}
		return null;
	}
	
	public static String executeGet(String uri,Map<String, String> parameters,String token ) {

		CloseableHttpResponse response = null;
		try {
			// 创建uri
			URIBuilder builder = new URIBuilder(uri);
			if (parameters != null) {
				for (String key : parameters.keySet()) {
					builder.addParameter(key, parameters.get(key));
				}
			}
			URI uri1 = builder.build();
			// 创建http GET请求
			HttpGet httpGet = new HttpGet(uri1);
			httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.102 Safari/537.36");
			httpGet.addHeader("token",token);
			httpGet.addHeader("Content-Type","charset=UTF-8");
			httpGet.setConfig(requestConfig);
			response = CLIENT.execute(httpGet);
		    int statusCode = response.getStatusLine().getStatusCode();
		    if(statusCode == 200){
		    	HttpEntity respEntity = response.getEntity();
		    	String str = EntityUtils.toString(respEntity);
		    	return str;
		    }
		}catch(Exception e){
			log.error("executeGet url="+uri, e);
		} finally {
			
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.error("consume executeGet url="+uri, e);
				}
			}
		}
		return null;
	}
	
	/**
	 * 获取URL对应的字节数组（下载）
	 * @param uri
	 * @return data
	 */
	public static byte[] getByteArray(String uri) {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.102 Safari/537.36");
		httpGet.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			response = CLIENT.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200){
				// 将输入流转换为字节数组
				return FileCopyUtils.copyToByteArray(response.getEntity().getContent());
			}
		}catch(Exception e){
			log.error("executeGet url="+uri, e);
		} finally {
			
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.error("consume executeGet url="+uri, e);
				}
			}
		}
		return null;
	}
	
	public static String post(String url, File file, String fileKey,  Map<String, Object> params){
		HttpPost post = new HttpPost(url);
		post.addHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;");
		post.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
	    	if(null != file && null != fileKey && !"".equals(fileKey)){
	    		MultipartEntityBuilder builder = MultipartEntityBuilder.create();  
	    		builder.addPart(fileKey, new FileBody(file)); 
		    	if(null != params){
		    		for(String name: params.keySet()){
		    			builder.addPart(name, new StringBody(params.get(name)+"", ContentType.TEXT_PLAIN));
		    		}
		    	}
		    	HttpEntity entity = builder.build();
		    	post.setEntity(entity);
	    	}
	        response = CLIENT.execute(post);
	        if(response != null){
	        	InputStream ins = response.getEntity().getContent();
	        	return IOUtils.toString(ins,"UTF-8");
	        }
		} catch (Exception e) {
			log.error("post url="+url, e);
		} finally {
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.error("consume post url="+url, e);
				}
			}
		}
    	return null;
	}
	
	/**
	 * 文件上传
	 * @param url
	 * @param data
	 * @param filename
	 * @param fileKey
	 * @param params
	 * @return
	 */
	public static String post(String url, byte[] data, String filename, String fileKey,  Map<String, Object> params){
		HttpPost post = new HttpPost(url);
		post.addHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;");
		post.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			if(null != data && null != fileKey && !"".equals(fileKey)){
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();  
				builder.addPart(fileKey, new ByteArrayBody(data, filename)); 
				if(null != params){
					for(String name: params.keySet()){
						builder.addPart(name, new StringBody(params.get(name)+"", ContentType.TEXT_PLAIN));
					}
				}
				HttpEntity entity = builder.build();
				post.setEntity(entity);
			}
			response = CLIENT.execute(post);
			if(response != null){
				InputStream ins = response.getEntity().getContent();
				return IOUtils.toString(ins,"UTF-8");
			}
		} catch (Exception e) {
			log.error("post url="+url, e);
		} finally {
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.error("consume post url="+url, e);
				}
			}
		}
		return null;
	}
	
	public static String post(String url, InputStream file,String fileName, String fileKey,  Map<String, Object> params){
		HttpPost post = new HttpPost(url);
		post.addHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;");
		RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setExpectContinueEnabled(true)
            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
            .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
            .build();
		 RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
		 post.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
	    //	post.addHeader("Content-Type", "utf-8");
	    	if(null != file && null != fileKey && !"".equals(fileKey)){
	    		MultipartEntityBuilder builder = MultipartEntityBuilder.create();  
	    		builder.addPart(fileKey, new InputStreamBody(file,fileName)); 
	    		ContentType type =ContentType.create("application/text", Consts.UTF_8);
		    	if(null != params){
		    		for(String name: params.keySet()){
		    			builder.addPart(name, new StringBody(params.get(name)+"",type));
		    		}
		    	}
		    	HttpEntity entity = builder.build();
		    	post.setEntity(entity);
	    	}
	        response = CLIENT.execute(post);
	        if(response != null){
	        	InputStream ins = response.getEntity().getContent();
	        	return IOUtils.toString(ins,"UTF-8");
	        }
		} catch (Exception e) {
			log.error("post url="+url, e);
		} finally {
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.error("consume post url="+url, e);
				}
			}
		}
    	return null;
	}
	
    
	/** 
	* @Title: doGet 
	* @Description: https以get方式请求数据 
	* @param @param url
	* @param @return    设定文件 
	* @return String    返回类型 
	* @throws 
	*/
	public static String doHttpsGet(String url){
		HttpGet get=new HttpGet(url);
		CloseableHttpResponse response=null;
		String result = "";
		try {
			response=CLIENT.execute(get);
			result=EntityUtils.toString(response.getEntity());
		} catch (ClientProtocolException e) {
			log.error("doHttpsGet url="+url, e);
		} catch (IOException e) {
			log.error("doHttpsGet url="+url, e);
		}finally{
			if(response !=null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.error("doHttpsGet consume url="+url, e);
				}
			}
		}
		return result;
	}
	
	
	@SuppressWarnings("deprecation")
	private static CloseableHttpClient acceptsUntrustedCertsHttpClient() throws Exception {
        HttpClientBuilder b = HttpClientBuilder.create();

        // setup a Trust Strategy that allows all certificates.
        //
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                return true;
            }
        }).build();
        b.setSslcontext( sslContext);

        // don't check Hostnames, either.
        //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

        // here's the special part:
        //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
        //      -- and create a Registry, to register it.
        //
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        // now, we create connection-manager using our Registry.
        //      -- allows multi-threaded use
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
        b.setConnectionManager( connMgr);

        // finally, build the HttpClient;
        //      -- done!
        CloseableHttpClient client = b.build();
        return client;
    }
	
	public static void main(String[] args) {
		
		String requestBody = "";
		doPostsDhb( "https://www.baidu.com",  requestBody,"application/json");
	}
	
	public static JSONObject executeH5PostJson(String requestBodyJso,String url){
		if(url==null){
			log.error("调用api对应的url地址没有配置");
			return null;
		} 
		log.info("--------------调用api接口入参数据：" + requestBodyJso);
		String jsonStr = executePost(url, requestBodyJso, "application/json");
		log.info("--------------调用api接口返回数据：" + jsonStr);
		if(jsonStr == null){
			return null;
		}
		return JSONObject.parseObject(jsonStr);
	}
	
	/**
	 * 
	 * @Title: doPostsDhb 
	 * @Description: 贷后帮的post接口请求规则，第一次请求设置30秒超时，如果超时发起第二次请求超时设置为60秒
	 * @param @param uri
	 * @param @param requestBody
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws 
	 *
	 */
	public static String doPostsDhb(String uri, String requestBody, String contentType){		
		String postsDhbRet = doPostsDhb(uri,requestBody,"application/json", 125000);//第一次设置超时为30秒30000
		return postsDhbRet;
	}
	
	public static String doPostsDhb(String uri, String requestBody, String contentType ,int timeout){
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.102 Safari/537.36");
		if(contentType != null){
			httpPost.addHeader("Content-Type", contentType);
		}
		
		RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setExpectContinueEnabled(true)
            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
            .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
            .build();
		RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
                .setSocketTimeout(timeout)
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(5000)
                .build();
		 httpPost.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			HttpEntity entity = new StringEntity(requestBody, "utf-8");
			httpPost.setEntity(entity);
			response = CLIENT.execute(httpPost);
		    int statusCode = response.getStatusLine().getStatusCode();
		    if(statusCode == 200){
		    	HttpEntity respEntity = response.getEntity();
		    	String str = EntityUtils.toString(respEntity);
		    	System.out.println(str);
		    	return str;
		    }
		}catch(Exception e){
			log.error("doPostsDhb url="+uri, e);
		} finally {
			if(response != null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (Exception e) {
					log.error("doPostsDhb consume url="+uri, e);
				}
			}
		}
		return null;
	}
}