package com.alcoholfactory.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiPathPrefixConfig implements WebMvcConfigurer {

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix(
        "/api",
        c ->
            c.isAnnotationPresent(RestController.class)
                && c.getPackageName().startsWith("com.alcoholfactory.api.modules"));
  }
}
