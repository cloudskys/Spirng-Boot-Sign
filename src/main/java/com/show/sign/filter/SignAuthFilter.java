package com.show.sign.filter;

import com.alibaba.fastjson.JSONObject;
import com.show.sign.utils.HttpUtils;
import com.show.sign.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;

/**
 * 签名过滤器
 * 
 * @author show
 * @date 10:03 2019/5/30
 * @Component 注册 Filter 组件
 */
@Slf4j
@Component
public class SignAuthFilter implements Filter {
    static final String FAVICON = "/favicon.ico";
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public void init(FilterConfig filterConfig) {
        log.info("初始化 SignAuthFilter");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException,IllegalArgumentException {
        HttpServletResponse response = (HttpServletResponse)res;
        // 防止流读取一次后就没有了, 所以需要将流继续写出去
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletRequest requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
        System.out.println(requestWrapper.getRequestURI()+"---");
        if (FAVICON.equals(requestWrapper.getRequestURI()) || ("/signTest/outofservice").equals(requestWrapper.getRequestURI())) { // 获取图标不需要验证签名
            chain.doFilter(request, response);
        }else {
            String ts = request.getParameter("ts");
            long diftime = 0;
            try {
                diftime = timediff(new Date(), format.parse(ts));
                log.info("time:" + diftime);
            } catch (Exception e) {
            }
            if (diftime < 0) {
                UnauthorizedForward(request, response);
                log.error("请求已过期");
                return;
            }
            String apiKey = request.getParameter("key"); //apiKey
            //此处可以使用apiKey获取数据库存储的对应的apiSecret ，如果apiSecret不存在，则报异常。如果存在，则进行下一步apiSecret签名验证
            if (apiKey == null) {
                ///API签名验证失败，把请求转发到处理不合法请求的Controller
                UnauthorizedForward(request, response);
                return;
            } else {
                // 获取全部参数(包括URL和body上的)
                SortedMap<String, String> allParams = HttpUtils.getAllParams(requestWrapper);
                // 对参数进行签名验证
                boolean isSigned = SignUtil.checkSig(request, SignUtil.apiSecret);
                if (isSigned) {
                    log.info("签名通过");
                    chain.doFilter(requestWrapper, response);
                } else {
                    log.info("参数校验出错");
                    // 校验失败返回前端
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json; charset=utf-8");
                    PrintWriter out = response.getWriter();
                    JSONObject resParam = new JSONObject();
                    resParam.put("msg", "参数校验出错");
                    resParam.put("success", "false");
                    out.append(resParam.toJSONString());
                }
            }
        }
    }
    private void UnauthorizedForward(HttpServletRequest request,HttpServletResponse response) throws IOException,ServletException{

///查询不到接口配置，把请求转发到处理不合法请求的Controller

        if ("GET".equals(request.getMethod())) {

            request.getRequestDispatcher("/signTest/outofservice").forward(request, response);

        } else {

            request.getRequestDispatcher("/api/dataCenterRequest/handleUnauthorizedPostRequest").forward(request, response);

        }

    }

    @Override
    public void destroy() {

        log.info("销毁 SignAuthFilter");
    }

    public static long timediff(Date d1, Date d2) throws ParseException {
        long diff = d2.getTime() - d1.getTime();
        long diffHours = diff / (60 * 60 * 1000) % 24;
        return diffHours;
    }
}
