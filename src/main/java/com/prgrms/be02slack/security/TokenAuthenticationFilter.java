package com.prgrms.be02slack.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.prgrms.be02slack.member.entity.Role;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final TokenProvider tokenProvider;
  private final DefaultUserDetailsService customUserDetailsService;

  public TokenAuthenticationFilter(TokenProvider tokenProvider,
      DefaultUserDetailsService customUserDetailsService) {
    this.tokenProvider = tokenProvider;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    final String token = tokenProvider.resolveToken(request);

    if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
      UsernamePasswordAuthenticationToken authentication;
      final String email = tokenProvider.getEmailFromToken(token);
      final String tokenType = tokenProvider.getTypeFromToken(token);

      if (tokenType.equals(TokenType.Login.name())) {
        authentication = new UsernamePasswordAuthenticationToken(
            email,
            null,
            Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_GUEST.name()))
        );
      } else {
        String tokenPayloadStr = email + " " + tokenType;
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(tokenPayloadStr);
        authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
      }

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}
