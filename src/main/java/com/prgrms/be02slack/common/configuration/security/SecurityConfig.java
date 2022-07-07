package com.prgrms.be02slack.common.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.prgrms.be02slack.security.AccessDeniedHandlerImpl;
import com.prgrms.be02slack.security.AuthenticationEntryPointImpl;
import com.prgrms.be02slack.security.DefaultUserDetailsService;
import com.prgrms.be02slack.security.TokenAuthenticationFilter;
import com.prgrms.be02slack.security.TokenProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String GUEST = "GUEST";
  private static final String OWNER = "OWNER";
  private static final String USER = "USER";

  private final TokenProvider tokenProvider;
  private final DefaultUserDetailsService customUserDetailsService;

  public SecurityConfig(TokenProvider tokenProvider, DefaultUserDetailsService customUserDetailsService) {
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
          .antMatchers(HttpMethod.POST, "/api/v1/workspaces/{encodedWorkspaceId}/token")
            .hasAnyRole(GUEST)
        .antMatchers(HttpMethod.GET, "/api/v1/members/{encodedMemberId}")
          .hasAnyRole(USER, OWNER)
        .antMatchers(HttpMethod.GET, "/api/v1/channels/{encodedChannelId}/members")
          .hasAnyRole(USER, OWNER)
        .antMatchers(HttpMethod.PUT,"/api/v1/workspaces/**")
          .hasRole(OWNER)
        .antMatchers(HttpMethod.POST, "/api/v1/workspaces/{encodedWorkspaceId}/members/invite")
          .hasAnyRole(USER, OWNER)
        .antMatchers("/api/v1/workspaces/{encodedWorkspaceId}/channels/{encodedChannelId}/participate")
          .hasAnyRole(GUEST)
        .antMatchers("/api/v1/workspaces/{encodedWorkspaceId}/channels/**")
          .hasAnyRole(USER, OWNER)
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
}
