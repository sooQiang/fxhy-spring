package com.fxhy.spring.framework.webmvc.servlet;

import java.io.File;

public class FXViewResolver {
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";
    private File templateRootDir;

    public FXViewResolver(String tempateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(tempateRoot).getFile();
        this.templateRootDir = new File(templateRootPath);
    }

    public FXView resolveViewName(String viewName) {
        if(null == viewName || "".equals(viewName.trim())){return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)? viewName:(viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
        return new FXView(templateFile);
    }
}
