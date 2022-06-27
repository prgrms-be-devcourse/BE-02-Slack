package com.prgrms.be02slack.common.dto;

public class ApiResponse {
  private final String result;
  private final String message;

  private ApiResponse(String result, String message) {
    this.result = result;
    this.message = message;
  }

  public static ApiResponse success() {
    return new ApiResponse("ok", "success");
  }

  public static ApiResponse fail(String message) {
    return new ApiResponse("fail", message);
  }

  public String getResult() {
    return result;
  }

  public String getMessage() {
    return message;
  }
}
