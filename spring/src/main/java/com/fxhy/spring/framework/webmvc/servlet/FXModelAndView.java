package com.fxhy.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * Created by Tom.
 */
public class FXModelAndView {
    private String viewName;
    private Map<String,?> model;

    public FXModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public FXModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}


