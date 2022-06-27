package com.prgrms.be02slack.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.service.MemberService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberService memberService;
  private final IdEncoder idEncoder;

  public CustomUserDetailsService(MemberService memberService, IdEncoder idEncoder) {
    this.memberService = memberService;
    this.idEncoder = idEncoder;
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
