package com.prgrms.be02slack.message.controller.dto;

import com.prgrms.be02slack.message.entity.Message;

public class MessageWebsocketResponse {

  private Author author;
  private String content;

  public MessageWebsocketResponse() {/*no-op*/}

  public MessageWebsocketResponse(Author author, String content) {
    this.author = author;
    this.content = content;
  }

  public static MessageWebsocketResponse from(Message message) {
    final var member = message.getMember();
    final var author = new Author(member.getDisplayName(), member.getEmail());
    return new MessageWebsocketResponse(author, message.getContent());
  }

  public static MessageWebsocketResponse error(String message) {
    return new MessageWebsocketResponse(null, message);
  }

  public Author getAuthor() {
    return author;
  }

  public String getContent() {
    return content;
  }

  public static class Author {
    private String displayName;
    private String email;

    public Author() {
    }

    public Author(String displayName, String email) {
      this.displayName = displayName;
      this.email = email;
    }

    public String getDisplayName() {
      return displayName;
    }

    public String getEmail() {
      return email;
    }

  }
}
