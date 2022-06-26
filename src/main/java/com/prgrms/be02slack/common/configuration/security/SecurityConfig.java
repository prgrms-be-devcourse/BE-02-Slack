package com.prgrms.be02slack.common.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.prgrms.be02slack.security.AccessDeniedHandlerImpl;
import com.prgrms.be02slack.security.AuthenticationEntryPointImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf()
          .disable()
        .headers()
          .disable()
        .formLogin()
          .disable()
        .httpBasic()
          .disable()
        .rememberMe()
          .disable()
        .logout()
          .disable()
        .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          .and()
        .exceptionHandling()
          .accessDeniedHandler(accessDeniedHandlerImpl())
          .and()
        .exceptionHandling()
          .authenticationEntryPoint(authenticationEntryPointImpl())
          .and()
        .authorizeRequests()
          .anyRequest().permitAll()
          .and();
  }

  @Bean
  public AccessDeniedHandlerImpl accessDeniedHandlerImpl() {
    return new AccessDeniedHandlerImpl();
  }

  @Bean
  public AuthenticationEntryPointImpl authenticationEntryPointImpl() {
    return new AuthenticationEntryPointImpl();
  }
}

