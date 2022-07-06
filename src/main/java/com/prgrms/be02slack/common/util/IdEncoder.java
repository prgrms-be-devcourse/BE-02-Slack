package com.prgrms.be02slack.common.util;

import static org.apache.logging.log4j.util.Strings.*;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.prgrms.be02slack.common.enums.EntityIdType;

@Component
public class IdEncoder {
  private static final String CODEC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final String CODEC_PATTERN = "^[ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789]*$";
  private final int RADIX = 36;

  public String encode(long id, EntityIdType type) {
    Assert.isTrue(id > 0, "Id must be positive");

    long param = id == Long.MAX_VALUE ? id : Long.MAX_VALUE - id;
    StringBuilder sb = new StringBuilder();
    while (param > 0) {
      sb.append(CODEC.charAt((int)(param % RADIX)));
      param /= RADIX;
    }

    sb.insert(0, EntityIdType.filter(type));
    return sb.toString();
  }

  public long decode(String encodedId) {
    Assert.isTrue(isNotBlank(encodedId), "EncodedId must be provided");
    Assert.isTrue(isValidHashVal(encodedId), "Invalid hash value");

    final String param = encodedId.substring(1);
    long sum = 0;
    long power = 1;
    for (int i = 0; i < param.length(); i++) {
      sum += CODEC.indexOf(param.charAt(i)) * power;
      power *= RADIX;
    }
    return Long.MAX_VALUE - sum;
  }

  private boolean isValidHashVal(String param) {
    return Pattern.matches(CODEC_PATTERN, param);
  }
}

