package com.prgrms.be02slack.common.configuration.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
  private String tokenSecret;
  private long tokenExpirationMsec;

  public String getTokenSecret() {
    return tokenSecret;
  }

  public void setTokenSecret(String tokenSecret) {
    this.tokenSecret = tokenSecret;
  }

  public long getTokenExpirationMsec() {
    return tokenExpirationMsec;
  }

  public void setTokenExpirationMsec(long tokenExpirationMsec) {
    this.tokenExpirationMsec = tokenExpirationMsec;
  }
}
