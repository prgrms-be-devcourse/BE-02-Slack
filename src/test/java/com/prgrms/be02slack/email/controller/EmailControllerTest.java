package com.prgrms.be02slack.email.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.be02slack.email.controller.dto.EmailRequest;
import com.prgrms.be02slack.email.service.EmailService;

@WebMvcTest(
    controllers = EmailController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@AutoConfigureRestDocs
public class EmailControllerTest {

  private static final String API_URL = "/api/v1/email";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private EmailService emailService;

  @Nested
  @DisplayName("Send 메서드는")
  class DescribeSend {

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {

      @Test
      @DisplayName("Ok 를 응답한다")
      void ItResponseOk() throws Exception {
        //given
        final var email = "test@test.com";
        final var requestMap = new EmailRequest(email);
        final var requestBody = objectMapper.writeValueAsString(requestMap);
        doNothing().when(emailService).sendMail(any(EmailRequest.class));

        //when
        final var request =
            RestDocumentationRequestBuilders.post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final var response = mockMvc.perform(request);

        //then
        verify(emailService).sendMail(any(EmailRequest.class));
        response.andExpect(status().isOk())
            .andDo(document("Send email",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("email").type(JsonFieldType.STRING).description("이메일명")
                )));
      }
    }

    @Nested
    @DisplayName("유효하지 않은 email이 전달 되면")
    class ContextInvalidEmail {

      @ParameterizedTest
      @ValueSource(strings = {
          "apple",
          "Hello.com"
      })
      @DisplayName("BadRequest를 반환한다.")
      void itReturnBadRequest(String email) throws Exception {
        //given
        final var requestMap = new EmailRequest(email);
        final var requestBody = objectMapper.writeValueAsString(requestMap);
        doNothing().when(emailService).sendMail(any(EmailRequest.class));

        //when
        final var request =
            RestDocumentationRequestBuilders.post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final var response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("email이 존재하지 않거나 빈 값이면")
    class ContextNullOrEmptyContent {

      @ParameterizedTest
      @NullAndEmptySource
      @DisplayName("BadRequest를 응답한다.")
      void itReturnBadRequest(String email) throws Exception {
        //given
        final var requestMap = new EmailRequest(email);
        final var requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        final var request =
            RestDocumentationRequestBuilders.post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final var response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }
  }
}
