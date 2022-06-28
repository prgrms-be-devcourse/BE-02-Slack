package com.prgrms.be02slack.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.service.MemberService;

@Service
public class DefaultUserDetailsService implements UserDetailsService {

  private final MemberService memberService;

  public DefaultUserDetailsService(MemberService memberService) {
    this.memberService = memberService;
  }

  @Override
  public UserDetails loadUserByUsername(String tokenPayloadStr) throws UsernameNotFoundException {
    final String[] tokenPayloads = tokenPayloadStr.split(" ");
    final String email = tokenPayloads[0];
    final String encodedWorkspaceId = tokenPayloads[1];

    final Member member = memberService.findByEmailAndWorkspaceKey(email, encodedWorkspaceId);

    return MemberDetails.create(member);
  }
}
