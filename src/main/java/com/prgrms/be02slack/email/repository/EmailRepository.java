package com.prgrms.be02slack.email.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class EmailRepository {
  private final Map<String, String> map = new ConcurrentHashMap<>();

  public void saveCode(String email, String code) {
    map.put(email, code);
  }

  public String findCodeByEmail(String email) {
    return map.get(email);
  }
}
