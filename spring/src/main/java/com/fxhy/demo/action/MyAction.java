package com.fxhy.demo.action;

import com.fxhy.demo.service.IModifyService;
import com.fxhy.demo.service.IQueryService;
import com.fxhy.spring.framework.annotation.*;
import com.fxhy.spring.framework.webmvc.servlet.FXModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 公布接口url
 * @author Tom
 *
 */
@FXController
@FXRequestMapping("/web")
public class MyAction {

	@FXAutowired
	IQueryService queryService;
	@FXAutowired
	IModifyService modifyService;

	@FXRequestMapping("/query.json")
	public FXModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@FXRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@FXRequestMapping("/add*.json")
	public FXModelAndView add(HttpServletRequest request,HttpServletResponse response,
			   @FXRequestParam("name") String name,@FXRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		return out(response,result);
	}
	
	@FXRequestMapping("/remove.json")
	public FXModelAndView remove(HttpServletRequest request, HttpServletResponse response,
								 @FXRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@FXRequestMapping("/edit.json")
	public FXModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@FXRequestParam("id") Integer id,
			@FXRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private FXModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
