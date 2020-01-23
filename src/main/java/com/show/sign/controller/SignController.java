package com.show.sign.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.show.sign.entity.Link;
import com.show.sign.service.LinkService;
import com.show.sign.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名测试
 * 
 * @author show
 * @date 10:53 2019/5/30
 */
@Controller
@Slf4j
@RequestMapping("/signTest")
public class SignController {
    @Autowired
    LinkService linkService;

    @PostMapping
    public Map<String, Object> signTestPost(@RequestBody JSONObject user) {
        String username = (String)user.get("username");
        String password = (String)user.get("password");
        log.info("username：{},password：{}", username, password);
        Map<String, Object> resParam = new HashMap<>(16);
        resParam.put("msg", "参数校验成功");
        resParam.put("success", "true");
        return resParam;
    }

    @GetMapping(value="/index",produces = "text/html;charset=UTF-8;")
    public String signTestGet(String username, String password) {
        log.info("username：{},password：{}", username, password);
        Map<String, Object> resParam = new HashMap<>(16);
        resParam.put("msg", "参数校验成功");
        resParam.put("success", "true");
        return JSON.toJSONString(resParam);
    }

    @GetMapping(value="/getApi",produces = "text/html;charset=UTF-8;")
    @PassToken(required=false)
    public String getApi() throws Exception{

        String tokenurl="http://localhost:8080/signTest/getToken/";
        String requesturl="http://localhost:8080/signTest/index";
        String method="/signTest/index/";
        String password = MD5Utils.string2MD5("sword01");
        Map m = new HashMap();
        m.put("password",password);
        m.put("apiUrl",requesturl);
        String token = HttpsClientUtil.executePost(tokenurl,m);
        log.info("获取token成功："+token+"开始调用接口:"+requesturl);
        log.info("开始调用接口:"+requesturl);
        Map<String, String> parameters = new HashMap<String,String>();
        parameters.put("username", "admin");
        parameters.put("password", "admin");
        parameters.put("key",SignUtil.apiKey);//标识
        parameters.put("sigVer", "1"); //签名版本
        parameters.put("token",token);
        //String ts = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS").format(LocalDateTime.now());
        String ts="2020-01-22";
        parameters.put("ts", ts); //时间戳
        log.info("创建签名开始");
        String sign = SignUtil.createSign(method,"UTF-8",parameters);
        log.info("创建签名结束，签名:"+sign);
        String reUrl = requesturl;
        log.info("api接口调用信息:");
        log.info("reUrl:"+reUrl);
        log.info("parameters:"+JSON.toJSONString(parameters));
        log.info("token:"+token);
        log.info("开始调用api，并在header中设置token");
        String response = HttpsClientUtil.executeGet(reUrl,parameters,token);
        log.info("接口调用结果:"+response);
        return response;
    }

    @GetMapping("/outofservice")
    @PassToken(required=false)
    public Map<String, Object> outofService(String username, String password) {
        Map<String, Object> resParam = new HashMap<>(16);
        resParam.put("msg", "请求过期");
        resParam.put("success", "false");
        return resParam;
    }

    @PostMapping("/getToken")
    @PassToken(required=false)
    public String getToken(String password, String apiUrl, HttpServletRequest request, HttpServletResponse response) {
        ApiToken token = new ApiToken();
        token.setUrlstr(apiUrl);
        //若生产环境，此处需要验证密码串正确性(用db/redis)，然后才生成token
        token.setPassmd5(password);
        token.setExpireTime(BigDecimal.valueOf(365).multiply(BigDecimal.valueOf(24 * 60 * 60 * 100)).longValue());
        String tokens = TokenUtils.createToken(token);
        return tokens;
    }


    @GetMapping(value="/testShortUrl",produces = "text/html;charset=UTF-8;")
    @PassToken(required=false)
    @ResponseBody
    public String getToken(String password, HttpServletRequest request, HttpServletResponse response) {
        log.info("进入短连接后的请求成功");
        return "跳转成功:"+password;
    }

    /**
     * 第三方短连接服务
     * @return
     * @throws Exception
     */
    @GetMapping("/shortUrl")
    @PassToken(required=false)
    public String shortUrl() throws  Exception{
        String requesturl="http://192.168.0.56:8080/signTest/testShortUrl?password=1233wang";
        log.info("生成短连接开始");
        //使用第三方短连接服务 http://suo.im/api.htm
        // key:5e2906893a005a3c52923648@d177b674097a5d58e2ec57c7a967c1c6
        String shortUrl = ShortUrlUtil.sinaShortUrlAPI(requesturl);
        log.info("生成短连接结束");
        return shortUrl;
    }

    /**
     *  生成短链接
     * @param url
     * @return
     */
    @RequestMapping("/api")
    @ResponseBody
    public Object api(String url){
        if (url == null || "".equals(url)){
            return null;
        }
        if(url.startsWith("http://") || url.startsWith("https://")){
            Link link = new Link();
            link.setLongUrl(url);
            return  linkService.save(link);

        }else{
            return "网址必须以http或https开头";
        }
    }

    /**
     * 301跳转  短连接解析器
     * @param url
     * @return
     */
    @RequestMapping("/{url}")
    @PassToken(required=false)
    @ResponseBody
    public String restoreUrl(@PathVariable("url") String url){
        System.out.println(url);
        String restoreUrl = linkService.restoreUrl("http://127.0.0.1:8080/signTest/"+url);
        //restoreUrl="http://192.168.0.56:8080/signTest/testShortUrl?password=1233wang";
        if(restoreUrl != null && !"".equals(restoreUrl)){
            return "longURL:"+restoreUrl;
        }else{
            return "redirect:http://www.cnilink.com";
        }

    }

}
