package com.prgrms.be02slack.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import com.prgrms.be02slack.member.entity.Role;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomLoginOwnerSecurityContextFactory.class)
public @interface WithMockCustomLoginOwner {

  long id() default 1L;

  String email() default "test@test.com";

  String name() default "test";

  String displayName() default "test";

  Role role() default Role.ROLE_OWNER;

  long workspaceId() default 1L;
}
