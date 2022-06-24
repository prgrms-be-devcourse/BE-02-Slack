package com.prgrms.be02slack.workspace.entity;

import static org.apache.logging.log4j.util.Strings.*;

import javax.persistence.Column;
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

  private static final String DEFAULT_WORKSPACE_NAME = "Slack";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 1, max = 50)
  private String name;

  @Size(min = 1, max = 21)
  @Column(unique = true)
  private String url;

  protected Workspace() {/*no-op*/}

  public Workspace(String name) {
    Assert.isTrue(isNotBlank(name), "Name must be provided");

    this.name = name;
  }

  public Workspace(String name, String url) {
    Assert.isTrue(isNotBlank(name), "Name must be provided");

    this.name = name;
    this.url = url;
  }

  public void update(Workspace updateWorkspace) {
    Assert.notNull(updateWorkspace, "Workspace must be provided");

    this.name = updateWorkspace.url;
    this.url = updateWorkspace.url;
  }

  public static Workspace createDefaultWorkspace() {
    return new Workspace(DEFAULT_WORKSPACE_NAME);
  }

  public void makeDefaultUrl() {
    this.url = this.name + this.id;
  }

  public String getName() {
    return name;
  }

  public Long getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }
}
