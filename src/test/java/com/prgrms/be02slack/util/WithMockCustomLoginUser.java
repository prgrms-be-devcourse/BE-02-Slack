package com.prgrms.be02slack.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomLoginUserSecurityContextFactory.class)
public @interface WithMockCustomLoginUser {

  String username() default "test@test.com";

  String role() default "GUEST";

}
