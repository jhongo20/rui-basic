package com.rui.basic.app.basic.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.noCache())
                .setCachePeriod(0);
        
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/img/")
                .setCacheControl(CacheControl.noCache())
                .setCachePeriod(0);

        // Permitir acceso a recursos de RUI\ATTACHMENTS
        registry.addResourceHandler("/documents/**")
                .addResourceLocations("file:C:/RUI/ATTACHMENTS/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Content-Disposition");
    }
}
