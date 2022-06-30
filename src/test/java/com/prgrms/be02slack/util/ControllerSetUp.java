package com.prgrms.be02slack.util;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.prgrms.be02slack.security.DefaultUserDetailsService;
import com.prgrms.be02slack.security.TokenProvider;

@ExtendWith(RestDocumentationExtension.class)
public class ControllerSetUp {

  protected MockMvc mockMvc;

  @MockBean
  private TokenProvider tokenProvider;

  @MockBean
  private DefaultUserDetailsService defaultUserDetailsService;

  @BeforeEach
  public void setUp(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentationContextProvider) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .apply(documentationConfiguration(restDocumentationContextProvider))
        .apply(springSecurity())
        .build();
  }
}
