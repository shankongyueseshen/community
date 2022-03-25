package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //用到了反射
        if(handler instanceof HandlerMethod){
            //强制转换
            HandlerMethod handlerMethod=(HandlerMethod) handler;
            //得到方法
            Method method =handlerMethod.getMethod();
            //用方法去获取注解
            LoginRequired loginRequired =method.getAnnotation(LoginRequired.class );
            if(loginRequired!=null&&hostHolder.getUser()==null){
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }
}
//还需要配置，把静态资源排除掉，因为不需要拦截哪些资源，排除之后再看哪个有这种注解
