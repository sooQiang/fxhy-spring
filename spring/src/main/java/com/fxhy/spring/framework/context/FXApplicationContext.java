package com.fxhy.spring.framework.context;

import com.fxhy.spring.framework.annotation.FXAutowired;
import com.fxhy.spring.framework.annotation.FXController;
import com.fxhy.spring.framework.annotation.FXService;
import com.fxhy.spring.framework.beans.FXBeanWrapper;
import com.fxhy.spring.framework.beans.config.FXBeanDefinition;
import com.fxhy.spring.framework.beans.support.FXBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FXApplicationContext {

    private Map<String, FXBeanDefinition> beanDefinitionMap = new HashMap<String,FXBeanDefinition>();

    private  Map<String,FXBeanWrapper> factoryBeanInstanceCache = new HashMap<String,FXBeanWrapper>();

    private Map<String,Object> factoryBeanObjectCache = new HashMap<String,Object>();

    private FXBeanDefinitionReader reader;
    public FXApplicationContext(String ... configLocations) {
        //1.加载配置文件
        reader = new FXBeanDefinitionReader(configLocations);

        //2.解析配置文件，解析成BeanDefinition
        try {
        List<FXBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        //3.缓存beanDefinition
        doRegistBeanDefinition(beanDefinitions);

        doAutowrited();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Properties getConfig(){
        return reader.getContextConfig();
    }

    private void doRegistBeanDefinition(List<FXBeanDefinition> beanDefinitions) throws Exception {
        for (FXBeanDefinition beanDefinition : beanDefinitions) {
            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The "+beanDefinition.getFactoryBeanName()+" is exists");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
        }
    }

    private void doAutowrited() {
        //调用getBean
        for (Map.Entry<String, FXBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);

        }
    }

    public Object getBean(String beanName){
        //1.先拿到配置信息
        FXBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //2.反射实例化newInstance
        Object instance = instantiateBean(beanName,beanDefinition);
        //3.封装成beanWrapper对象
        FXBeanWrapper beanWrapper = new FXBeanWrapper(instance);
        //4.保存到ioc容器
        factoryBeanInstanceCache.put(beanName,beanWrapper);
        //5.执行依赖注入
        populateBean(beanName,beanDefinition,beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    private void populateBean(String beanName, FXBeanDefinition beanDefinition, FXBeanWrapper beanWrapper) {
        //可能涉及到循环依赖
        //A{ B b}
        //B{ A a}
        //用两个缓存，循环两次
        //1.把第一次读取结果为空的BeanDefinition存到第一个缓存
        //2.等第一次循环后，第二次循环再检查第一次循环，再进行赋值



        Object instance = beanWrapper.getWrapperInstance();
        Class<?> clazz = beanWrapper.getWrappedClass();

        //在spring中都是@component
        if(!(clazz.isAnnotationPresent(FXController.class)||clazz.isAnnotationPresent(FXService.class))){
            return;
        }
        //把所有的包括private/protected/default/public 修饰字段都取出来
            for (Field field : clazz.getDeclaredFields()) {
                if(!field.isAnnotationPresent(FXAutowired.class)){ continue; }

                FXAutowired autowired = field.getAnnotation(FXAutowired.class);

                //如果用户没有自定义的beanName，就默认根据类型注入
                String autowireBeanName = autowired.value().trim();
                if("".equals(autowireBeanName)){
                    //field.getType().getName() 获取字段的类型
                    autowireBeanName = field.getType().getName();
                }

                //暴力访问
                field.setAccessible(true);

                try {
                    if(this.factoryBeanInstanceCache.get(autowireBeanName)==null){
                        continue;
                     }
                    //ioc.get(beanName) 相当于通过接口的全名拿到接口的实现的实例
                    field.set(instance,this.factoryBeanInstanceCache.get(autowireBeanName).getWrapperInstance());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

    }


    //创建真真实例对象
    private Object instantiateBean(String beanName, FXBeanDefinition beanDefinition) {

        Object instance = null;

        String className = beanDefinition.getBeanClassName();

        try {
            if(this.factoryBeanObjectCache.containsKey(beanName)){
                return this.factoryBeanObjectCache.get(beanName);
            }else {

                Class<?> clazz = Class.forName(className);

                instance = clazz.newInstance();
                this.factoryBeanObjectCache.put(beanName, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;

    }

    public Object getBean(Class beanClass){
        return getBean(beanClass.getName());
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.size()]);
    }
}
