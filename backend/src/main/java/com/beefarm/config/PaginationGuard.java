package com.beefarm.config;
import jakarta.servlet.http.*;
import com.beefarm.common.exception.BusinessException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class PaginationGuard implements WebMvcConfigurer {
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
                for (String field : new String[]{"pageNum", "pageSize"}) {
                    String raw = req.getParameter(field);
                    if (raw == null) continue;
                    try {
                        int value = Integer.parseInt(raw);
                        if (value < 1 || (field.equals("pageSize") && value > 500)) throw new NumberFormatException();
                    } catch (NumberFormatException e) { throw new BusinessException(400, "分页参数不合法，每页最多500条"); }
                }
                return true;
            }
        });
    }
}
