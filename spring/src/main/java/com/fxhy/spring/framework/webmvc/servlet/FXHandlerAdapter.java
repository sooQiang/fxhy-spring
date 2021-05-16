package com.fxhy.spring.framework.webmvc.servlet;

import com.fxhy.spring.framework.annotation.FXRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FXHandlerAdapter {

    public FXModerAndView handler(HttpServletRequest req, HttpServletResponse resp,FXHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {

        Map<String,Integer> paramIndexMapping = new HashMap<String, Integer>();

        Annotation[][] annotations = handler.getMethod().getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation a : annotations[i]) {
                if(a instanceof FXRequestParam){
                    String paramName = ((FXRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName,i);
                    }
                }

            }
        }

        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if(parameterType == HttpServletRequest.class||parameterType == HttpServletResponse.class){
                paramIndexMapping.put(parameterType.getName(),i);
            }
        }

        Map<String,String[]> parameterMap = req.getParameterMap();
        Object[] paramValues = new Object[parameterTypes.length];
        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            String value = Arrays.toString(parameterMap.get(param.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+","");

        if(!paramIndexMapping.containsKey(param.getKey())){ continue;}

            Integer index = paramIndexMapping.get(param.getKey());

            paramValues[index] = castStringValue(value, parameterTypes[index]);

        }
        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            Integer index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            Integer index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = req;
        }

        Object invoke = handler.getMethod().invoke(handler.getController(), paramValues);
        if(invoke == null||invoke instanceof Void){
            return null;
        }
        Boolean isModerAndView = handler.getMethod().getReturnType() == FXModerAndView.class;
        if(isModerAndView){
            return (FXModerAndView) invoke;
        }else{
            return null;
        }
    }

    private Object castStringValue(String value ,Class<?> paramType){
       if(String.class == paramType){
           return value;
       }else if(Integer.class == paramType){
            return Integer.valueOf(value);
       }else if(Double.class == paramType){
            return Double.valueOf(value);
       }else{
           if(value != null){
                return value;
           }
       }
       return null;
    }
}
