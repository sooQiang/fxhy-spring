package com.fxhy.demo.action;


import com.fxhy.demo.service.IQueryService;
import com.fxhy.spring.framework.annotation.*;
import com.fxhy.spring.framework.webmvc.servlet.FXModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@FXController
@FXRequestMapping("/")
public class PageAction {

    @FXAutowired
    IQueryService queryService;

    @FXRequestMapping("/first.html")
    public FXModelAndView query(@FXRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new FXModelAndView("first.html",model);
    }

}
