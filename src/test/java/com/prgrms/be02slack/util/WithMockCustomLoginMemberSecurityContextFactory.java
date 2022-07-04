package com.prgrms.be02slack.util;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.security.MemberDetails;
import com.prgrms.be02slack.workspace.entity.Workspace;

public class WithMockCustomLoginMemberSecurityContextFactory implements
    WithSecurityContextFactory<WithMockCustomLoginMember> {

  @Override
  public SecurityContext createSecurityContext(WithMockCustomLoginMember annotation) {

    final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

    Workspace workspace = Workspace.createDefaultWorkspace();
    ReflectionTestUtils.setField(workspace, "id", annotation.workspaceId());

    Member member = Member.builder()
        .email(annotation.email())
        .name(annotation.name())
        .displayName(annotation.displayName())
        .role(annotation.role())
        .workspace(workspace)
        .build();
    ReflectionTestUtils.setField(member, "id", annotation.id());

    final MemberDetails memberDetails = MemberDetails.create(member);

    final UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            memberDetails,
            "password",
            memberDetails.getAuthorities());

    securityContext.setAuthentication(authenticationToken);
    return securityContext;
  }
}
