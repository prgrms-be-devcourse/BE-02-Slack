package com.prgrms.be02slack.member.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.util.ControllerSetUp;
import com.prgrms.be02slack.util.WithMockCustomLoginUser;

@WebMvcTest(controllers = MemberApiController.class)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
public class MemberControllerTest extends ControllerSetUp {

  private static final String API_URL = "/api/v1/members";

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private MemberService memberService;

  @Nested
  @DisplayName("verfiy 메서드는")
  class DescribeVerify {

    private static final String VERIFY_URI = "/verify";

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
        final VerificationRequest requestMap = new VerificationRequest(email, "test");
        final String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(API_URL + VERIFY_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("email이 존재하지 않거나 빈 값이면")
    class ContextWithNullOrEmptyEmail {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", "", ""})
      @DisplayName("BadRequest를 반환한다.")
      void ItResponseBadRequest(String email) throws Exception {
        //given
        final VerificationRequest requestMap = new VerificationRequest(email, "test");
        final String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(API_URL + VERIFY_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("verificationCode가 존재하지 않거나 빈 값이면")
    class ContextWithNullOrEmptyVerificationCode {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", "", ""})
      @DisplayName("BadRequest를 반환한다.")
      void ItResponseBadRequest(String verificationCode) throws Exception {
        //given
        final VerificationRequest requestMap =
            new VerificationRequest("test@test.com", verificationCode);
        final String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(API_URL + VERIFY_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {

      @Test
      @DisplayName("토큰을 전달한다")
      void ItResponseToken() throws Exception {
        //given
        final VerificationRequest requestMap =
            new VerificationRequest("test@test.com", "test");
        final String requestBody = objectMapper.writeValueAsString(requestMap);
        final AuthResponse authResponse = new AuthResponse("testToken");
        given(memberService.verify(any(VerificationRequest.class))).willReturn(authResponse);

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(API_URL + VERIFY_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isOk())
            .andDo(document("Verify email",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("email")
                        .type(JsonFieldType.STRING)
                        .description("이메일명"),
                    fieldWithPath("verificationCode")
                        .type(JsonFieldType.STRING)
                        .description("인증 코드")
                ),
                responseFields(
                    fieldWithPath("token").type(JsonFieldType.STRING).description("토큰"),
                    fieldWithPath("tokenType").type(JsonFieldType.STRING).description("토큰 타입")
                )));
      }
    }
  }

  @Nested
  @DisplayName("enterWorkspace 메서드는")
  @WithMockCustomLoginUser
  class DescribeEnterWorkspace {

    private static final String ENTER_URI = "/api/v1/workspaces/{encodedWorkspaceId}/enter";

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {

      @Test
      @DisplayName("토큰을 전달한다")
      void ItResponseToken() throws Exception {
        final String encodedWorkspaceId = "TESTID";
        final AuthResponse authResponse = new AuthResponse("testToken");
        given(memberService.enterWorkspace(anyString(), anyString())).willReturn(authResponse);

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(ENTER_URI, encodedWorkspaceId)
                .contentType(MediaType.APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isOk())
            .andDo(document("Enter workspace",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                    parameterWithName("encodedWorkspaceId")
                        .description("인코딩된 워크스페이스 id")
                ),
                responseFields(
                    fieldWithPath("token")
                        .type(JsonFieldType.STRING)
                        .description("토큰"),
                    fieldWithPath("tokenType")
                        .type(JsonFieldType.STRING)
                        .description("토큰 타입")
                )));
      }
    }
  }
}
