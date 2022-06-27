package com.prgrms.be02slack.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.prgrms.be02slack.common.configuration.security.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
public class TokenProviderTest {

  @Mock
  JwtConfig jwtConfig;

  @InjectMocks
  TokenProvider tokenProvider;

  @Test
  void createLoginTokenTest() {
    //given
    final String email = "test@test.com";
    given(jwtConfig.getTokenSecret()).willReturn("testTokenSecretKey");
    given(jwtConfig.getTokenExpirationMsec()).willReturn(1000000L);

    //when, then
    String createdToken = tokenProvider.createLoginToken(email);
    Claims claims = Jwts.parser()
        .setSigningKey(jwtConfig.getTokenSecret())
        .parseClaimsJws(createdToken)
        .getBody();

    assertThat(claims.getSubject()).isEqualTo(email);
    assertThat(claims.get("type")).isEqualTo("Login");
  }
}
