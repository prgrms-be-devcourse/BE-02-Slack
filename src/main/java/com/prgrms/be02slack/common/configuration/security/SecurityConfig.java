package com.prgrms.be02slack.common.configuration.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.prgrms.be02slack.security.AccessDeniedHandlerImpl;
import com.prgrms.be02slack.security.AuthenticationEntryPointImpl;
import com.prgrms.be02slack.security.TokenProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final TokenProvider tokenProvider;

  public SecurityConfig(TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

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
        .accessDeniedHandler(new AccessDeniedHandlerImpl())
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(new AuthenticationEntryPointImpl())
        .and()
        .authorizeRequests()
        .anyRequest()
        .permitAll()
        .and();
  }
}

