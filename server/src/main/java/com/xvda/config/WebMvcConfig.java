package com.xvda.config;

import com.xvda.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 配置：登录拦截 + 上传图片静态映射
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${xvda.upload-dir:./upload}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/ping",
                        "/api/auth/login"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /upload/** -> 本地磁盘目录（绝对路径，跨平台）
        String absolute = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + absolute + "/");
    }
}
