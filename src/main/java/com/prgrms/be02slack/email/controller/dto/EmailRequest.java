package com.prgrms.be02slack.email.controller.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;

public class EmailRequest {

	@Email
	@NotBlank
	private final String email;

	@JsonCreator
	public EmailRequest(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
}
