package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class CorsConfig extends WebMvcConfigurationSupport{

	 private CorsConfiguration buildConfig() {
	       CorsConfiguration corsConfiguration = new CorsConfiguration();
	       corsConfiguration.addAllowedOriginPattern("*");
	       corsConfiguration.addAllowedHeader("*");
	       corsConfiguration.addAllowedMethod("*");
	       //默认的跨域请求不会发送cookie等用户认证凭据
	       //必须前端设置withCredentials=true且后端设置AllowCredentials(true)
	       //才能使cookie+session认证生效;
	       corsConfiguration.setAllowCredentials(true);
	       return corsConfiguration;
	   }

	   @Bean
	   public CorsFilter corsFilter() {
	       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	       source.registerCorsConfiguration("/**", buildConfig());
	       return new CorsFilter(source);
	   }

	   @Override
	    public void addCorsMappings(CorsRegistry registry) {
	        // 设置允许跨域的路由
	        registry.addMapping("/**")
	                // 设置允许跨域请求的域名
	                .allowedOriginPatterns("*")
	                // 是否允许证书（cookies）
	                .allowCredentials(true)
	                // 设置允许的方法
	                .allowedMethods("*")
	                // 跨域允许时间
	                .maxAge(3600);
	    }
}
