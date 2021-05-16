package com.fxhy.spring.framework.webmvc.servlet;

import com.fxhy.spring.framework.annotation.*;
import com.fxhy.spring.framework.context.FXApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 职责：任务调度，分发
 * Created by Tom.
 */
public class FXDispatchServlet extends HttpServlet {

    private FXApplicationContext applicationContext;

    private List<FXHandlerMapping> handlerMappings = new ArrayList<FXHandlerMapping>();

    private Map<FXHandlerMapping,FXHandlerAdapter> handlerAdapter = new HashMap<FXHandlerMapping, FXHandlerAdapter>();

    private List<FXViewResolver> viewResolvers = new ArrayList<FXViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //6、委派,根据URL去找到一个对应的Method并通过response返回
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        FXHandlerMapping handlerMapping = getHandler(req);
        if(handlerMapping == null){
            processDispatchResult(req,resp,new FXModerAndView("404"));
            return;
        }

        FXHandlerAdapter ha = getHandlerAdapter(handlerMapping);

        FXModerAndView mv = ha.handler(req, resp, handlerMapping);

        processDispatchResult(req,resp,mv);

    }

    private FXHandlerAdapter getHandlerAdapter(FXHandlerMapping handlerMapping) {
        if(null == handlerMapping){return null;}
        return this.handlerAdapter.get(handlerMapping);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, FXModerAndView mv) throws IOException {
        if(null == mv){return;}
        if(this.viewResolvers.isEmpty()){return;}
        for (FXViewResolver viewResolver : viewResolvers ) {
            FXView view = viewResolver.resolveViewName(mv.getViewName());
            view.render(mv.getModel(),req,resp);
            return;
        }
    }

    private FXHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){return null;}
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        for (FXHandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if(!matcher.matches()){continue;}
                return handlerMapping;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化springioc核心容器
        applicationContext = new FXApplicationContext(config.getInitParameter("contextConfigLocation"));


        initStrategies(applicationContext);

        //1、加载配置文件
//        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
//        doScanner(contextConfig.getProperty("scanPackage"));

        //==============IoC部分==============
        //3、初始化IoC容器，将扫描到的相关的类实例化，保存到IcC容器中

        //AOP，新生成的代理对象

        //==============DI部分==============
        //4、完成依赖注入

        //==============MVC部分==============
        //5、初始化HandlerMapping

        System.out.println("GP Spring framework is init.");
    }

    private void initStrategies(FXApplicationContext context) {
//        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMapping(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    private void initHandlerAdapters(FXApplicationContext context){
        for (FXHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapter.put(handlerMapping,new FXHandlerAdapter());
        }

    }
    private void initViewResolvers(FXApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new FXViewResolver(templateRoot));
        }
    }

    private void initHandlerMapping(FXApplicationContext context) {
        if(applicationContext.getBeanDefinitionCount() == 0){ return;}

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

            if(!clazz.isAnnotationPresent(FXController.class)){ continue; }


            //相当于提取 class上配置的url
            String baseUrl = "";
            if(clazz.isAnnotationPresent(FXRequestMapping.class)){
                FXRequestMapping requestMapping = clazz.getAnnotation(FXRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取public的方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(FXRequestMapping.class)){continue;}
                //提取每个方法上面配置的url
                FXRequestMapping requestMapping = method.getAnnotation(FXRequestMapping.class);

                // //demo//query
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new FXHandlerMapping(pattern,method,instance));
                System.out.println("Mapped : " + regex + "," + method);
            }

        }
    }

    //自己写，自己用
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
//        if(chars[0] > )
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
