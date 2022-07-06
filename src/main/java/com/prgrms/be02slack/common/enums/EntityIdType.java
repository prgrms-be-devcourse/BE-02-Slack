package com.prgrms.be02slack.common.enums;

import java.util.Arrays;

public enum EntityIdType {
  TEAM,
  MEMBER,
  CHANNEL,
  DMCHANNEL;

  public static String filter(EntityIdType type) {
    return Arrays.stream(EntityIdType.values())
        .filter(v -> v.equals(type))
        .findFirst()
        .map(v -> v.toString().substring(0,1))
        .orElseThrow(IllegalArgumentException::new);
  }
}
