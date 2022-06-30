package com.prgrms.be02slack.util;

import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomLoginUserSecurityContextFactory implements
    WithSecurityContextFactory<WithMockCustomLoginUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockCustomLoginUser annotation) {

    final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

    final UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            annotation.username(),
        "password",
        Arrays.asList(new SimpleGrantedAuthority(annotation.role()))
    );

    securityContext.setAuthentication(authenticationToken);
    return securityContext;
  }
}
