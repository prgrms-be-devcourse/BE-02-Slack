package com.prgrms.be02slack.common.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.prgrms.be02slack.security.AccessDeniedHandlerImpl;
import com.prgrms.be02slack.security.AuthenticationEntryPointImpl;
import com.prgrms.be02slack.security.CustomUserDetailsService;
import com.prgrms.be02slack.security.TokenAuthenticationFilter;
import com.prgrms.be02slack.security.TokenProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final TokenProvider tokenProvider;
  private final CustomUserDetailsService customUserDetailsService;

  public SecurityConfig(TokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
    this.tokenProvider = tokenProvider;
    this.customUserDetailsService = customUserDetailsService;
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
          .accessDeniedHandler(accessDeniedHandlerImpl())
          .and()
        .exceptionHandling()
          .authenticationEntryPoint(authenticationEntryPointImpl())
          .and()
        .authorizeRequests()
          .anyRequest().permitAll()
          .and()
        .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
  }

  @Bean
  public AccessDeniedHandlerImpl accessDeniedHandlerImpl() {
    return new AccessDeniedHandlerImpl();
  }

  @Bean
  public AuthenticationEntryPointImpl authenticationEntryPointImpl() {
    return new AuthenticationEntryPointImpl();
  }

  @Bean
  public TokenAuthenticationFilter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter(tokenProvider, customUserDetailsService);
  }

  @Override
  public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
    authenticationManagerBuilder.userDetailsService(customUserDetailsService);
  }
}

