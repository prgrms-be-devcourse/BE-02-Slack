package com.prgrms.be02slack.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import com.prgrms.be02slack.member.entity.Role;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomLoginMemberSecurityContextFactory.class)
public @interface WithMockCustomLoginMember {

  long id() default 1L;

  String email() default "test@test.com";

  String name() default "test";

  String displayName() default "test";

  Role role() default Role.ROLE_USER;

  long workspaceId() default 1L;
}
