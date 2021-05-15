package com.fxhy.spring.framework.beans.support;

import com.fxhy.spring.framework.beans.config.FXBeanDefinition;
import com.fxhy.spring.framework.context.FXApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FXBeanDefinitionReader {

    private Properties contextConfig = new Properties();

    //享元模式，缓存
    private List<String> registryBeanClasses = new ArrayList<String>();

    public FXBeanDefinitionReader(String ... configLocations) {
        doLoadConfig(configLocations[0]);

        //扫描配置文件中配置的类
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    public List<FXBeanDefinition> loadBeanDefinitions() {
        List<FXBeanDefinition> result = new ArrayList<>();
        try {
        for (String className : registryBeanClasses) {
            Class<?> beanClass = null;
                beanClass = Class.forName(className);

            //保存类对应的className(全类名)
            //还有beanName
            //1.默认是类名首字母小写
            result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));
            //2.自定义
            //3.接口注入
            for (Class<?> i : beanClass.getInterfaces()) {
                result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
            }

        }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    private FXBeanDefinition doCreateBeanDefinition(String beanName,String beanClassName){
        FXBeanDefinition beanDefinition = new FXBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(beanName);
        return beanDefinition;
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classpath:",""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String scanPackage) {
        //jar 、 war 、zip 、rar
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());

        //当成是一个ClassPath文件夹
        for (File file : classPath.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {
                if(!file.getName().endsWith(".class")){continue;}
                //全类名 = 包名.类名
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                //Class.forName(className);
                registryBeanClasses.add(className);
            }
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
//        if(chars[0] > )
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
