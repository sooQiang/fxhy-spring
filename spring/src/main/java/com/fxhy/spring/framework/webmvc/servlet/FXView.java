package com.fxhy.spring.framework.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FXView {
    private File viewFile;

    public FXView(File templateFile) {
        this.viewFile = templateFile;
    }
    public void render(Map<String,?> model, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(viewFile,"r");

        String line = null;
        while (null != (line = ra.readLine())){
            line = new String(line.getBytes("ISO-8859-1"),"UTF-8");
            Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}",Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()){
                String paramName = matcher.group();
                paramName.replaceAll("￥\\{|\\}","");
                Object paramValue = model.get(paramName);
                line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                pattern.matcher(line);
            }
            sb.append(line);
        }
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(sb.toString());

    }

    private static String makeStringForRegExp(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }
}
