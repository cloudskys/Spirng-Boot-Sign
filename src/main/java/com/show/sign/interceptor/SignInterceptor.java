package com.show.sign.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.show.sign.utils.HttpUtils;
import com.show.sign.utils.PassToken;
import com.show.sign.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
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
public class SignInterceptor  implements HandlerInterceptor {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 预处理回调方法，实现处理器的预处理
     * 返回值：true表示继续流程；false表示流程中断，不会继续调用其他的拦截器或处理器
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HandlerMethod handlerMethod=(HandlerMethod)handler;
        Method method=handlerMethod.getMethod();
        //检查是否有passtoken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (!passToken.required()) {
                return true;
            }
        }
        HttpServletRequest requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
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
                return false;
            }
            String apiKey = request.getParameter("key"); //apiKey
            //此处可以使用apiKey获取数据库存储的对应的apiSecret ，如果apiSecret不存在，则报异常。如果存在，则进行下一步apiSecret签名验证
            if (apiKey == null) {
                ///API签名验证失败，把请求转发到处理不合法请求的Controller
                UnauthorizedForward(request, response);
                return false;
            } else {
                // 获取全部参数(包括URL和body上的)
                SortedMap<String, String> allParams = HttpUtils.getAllParams(requestWrapper);
                // 对参数进行签名验证
                log.info("双重签名验证开始");
                boolean isSigned = SignUtil.checkSig(request, SignUtil.apiSecret);

                boolean isTokenPass = SignUtil.checkToken(request, SignUtil.apiSecret);
                log.info("后端签名验证:"+isSigned+",token验证:"+isTokenPass);

                if (isSigned && isTokenPass) {
                    log.info("双重签名通过");
                    log.info("双重签名验证结束");
                    return true;
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
                    log.info("签名验证结束");
                    return false;
                }

            }

    }

    /**
     * 后处理回调方法，实现处理器（controller）的后处理，但在渲染视图之前
     * 此时我们可以通过modelAndView对模型数据进行处理或对视图进行处理
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // TODO Auto-generated method stub

    }
    /**
     * 整个请求处理完毕回调方法，即在视图渲染完毕时回调，
     * 如性能监控中我们可以在此记录结束时间并输出消耗时间，
     * 还可以进行一些资源清理，类似于try-catch-finally中的finally，
     * 但仅调用处理器执行链中
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub

    }
    public static long timediff(Date d1, Date d2) throws ParseException {
        long diff = d2.getTime() - d1.getTime();
        long diffHours = diff / (60 * 60 * 1000) % 24;
        return diffHours;
    }
    private void UnauthorizedForward(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {

///查询不到接口配置，把请求转发到处理不合法请求的Controller

        if ("GET".equals(request.getMethod())) {

            request.getRequestDispatcher("/signTest/outofservice").forward(request, response);

        } else {

            request.getRequestDispatcher("/api/dataCenterRequest/handleUnauthorizedPostRequest").forward(request, response);

        }

    }
}