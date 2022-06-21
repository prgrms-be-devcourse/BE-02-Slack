package com.prgrms.be02slack.common.entity;

import java.time.LocalDateTime;
import javax.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

@MappedSuperclass
public class BaseTime {

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedBy
  private LocalDateTime updatedAt;

}
