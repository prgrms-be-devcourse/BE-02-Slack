package com.prgrms.be02slack.common.configuration.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "jwt")
@ConstructorBinding
public class JwtConfig {
  private final String tokenSecret;
  private final long tokenExpirationMsec;

  public JwtConfig(String tokenSecret, long tokenExpirationMsec) {
    this.tokenSecret = tokenSecret;
    this.tokenExpirationMsec = tokenExpirationMsec;
  }

  public String getTokenSecret() {
    return tokenSecret;
  }

  public long getTokenExpirationMsec() {
    return tokenExpirationMsec;
  }
}
