package com.prgrms.be02slack.workspace.entity;

import static org.apache.logging.log4j.util.Strings.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.util.Assert;

import com.prgrms.be02slack.common.entity.BaseTime;

@Entity
public class Workspace extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 1, max = 50)
  private String name;

  @NotNull
  @Size(min = 1, max = 21)
  private String url;

  protected Workspace() {/*no-op*/}

  public Workspace(String name, String url) {
    Assert.isTrue(isNotBlank(name), "Name must be provided");
    Assert.isTrue(isNotBlank(url), "Url must be provided");

    this.name = name;
    this.url = url;
  }

  public void update(Workspace updateWorkspace) {
    Assert.notNull(updateWorkspace, "Workspace must be provided");

    this.name = updateWorkspace.url;
    this.url = updateWorkspace.url;
  }
}
