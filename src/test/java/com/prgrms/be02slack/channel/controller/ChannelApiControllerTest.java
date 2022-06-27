package com.prgrms.be02slack.channel.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.service.ChannelService;
import com.prgrms.be02slack.common.dto.ApiResponse;

@WebMvcTest(
    controllers = ChannelApiController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@AutoConfigureRestDocs
class ChannelApiControllerTest {
  private static final String API_URL = "/api/v1/workspaces/";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ChannelService channelService;

  static class WorkspaceIdSourceBlank implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(" "),
          Arguments.of("\t"),
          Arguments.of("\n")
      );
    }
  }

  static class NameSourceOutOfRange implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of((Object)null),
          Arguments.of(""),
          Arguments.of("\t"),
          Arguments.of("\n"),
          Arguments.of("a".repeat(81))
      );
    }
  }

  @Nested
  @DisplayName("create 메서드는")
  class DescribeCreate {

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {
      @Test
      @DisplayName("인코딩된 id를 응답한다")
      void ItResponseOk() throws Exception {
        //given
        given(channelService.create(anyString(), any(ChannelSaveRequest.class)))
            .willReturn("testId");

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("name", "testName");
        requestMap.put("description", "testDescription");
        requestMap.put("isPrivate", false);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "{workspaceId}/channels", "testWorkspaceId")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        verify(channelService).create(anyString(), any(ChannelSaveRequest.class));
        response.andExpect(status().isOk())
            .andDo(document("Create channel",
                pathParameters(
                    parameterWithName("workspaceId").description("workspace id")
                ),
                requestFields(
                    fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("channel name"),
                    fieldWithPath("description")
                        .type(JsonFieldType.STRING)
                        .description("channel description"),
                    fieldWithPath("isPrivate")
                        .type(JsonFieldType.BOOLEAN)
                        .description("whether the channel is open to the public")
                )
            ));
      }
    }

    @Nested
    @DisplayName("workspaceId 가 빈 값 이거나 공백이라면")
    class ContextWithWorkspaceIdBlank {

      @ParameterizedTest
      @ArgumentsSource(WorkspaceIdSourceBlank.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String workspaceId) throws Exception {
        //given
        HashMap<Object, Object> requestMap = new HashMap<>();
        requestMap.put("name", "testName");
        requestMap.put("description", "testDescription");
        requestMap.put("isPrivate", false);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "{workspaceId}/channels", workspaceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("name 의 길이가 범위를 벗어나면")
    class ContextWithNameOutOfRange {

      @ParameterizedTest
      @ArgumentsSource(NameSourceOutOfRange.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String name) throws Exception {
        //given
        HashMap<Object, Object> requestMap = new HashMap<>();
        requestMap.put("name", name);
        requestMap.put("description", "testDescription");
        requestMap.put("isPrivate", false);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "{workspaceId}/channels", "testWorkspaceId")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }
  }

  @Nested
  @DisplayName("verifyName 메서드는")
  class DescribeVerifyName {

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {
      @Test
      @DisplayName("성공 ApiResponse 를 응답한다")
      void ItResponseSuccessApiResponse() throws Exception {
        //given
        given(channelService.verifyName(anyString(), anyString()))
            .willReturn(ApiResponse.success());

        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.get(
                API_URL + "{workspaceId}/channels/exists", "testWorkspaceId")
            .param("name", "testName");

        ResultActions response = mockMvc.perform(request);

        //then
        verify(channelService).verifyName(anyString(), anyString());
        response.andExpect(status().isOk())
            .andDo(document("Verify channel name",
                pathParameters(
                    parameterWithName("workspaceId").description("workspace id")
                ),
                requestParameters(
                    parameterWithName("name").description("channel name")
                )
            ));
      }
    }

    @Nested
    @DisplayName("workspaceId 가 빈 값 이거나 공백이라면")
    class ContextWithWorkspaceIdBlank {

      @ParameterizedTest
      @ArgumentsSource(WorkspaceIdSourceBlank.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String workspaceId) throws Exception {
        //given
        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.get(
                API_URL + "{workspaceId}/channels/exists", workspaceId)
            .param("name", "testName");

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("name 의 길이가 범위를 벗어나면")
    class ContextWithNameOutOfRange {

      @ParameterizedTest
      @ArgumentsSource(NameSourceOutOfRange.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String name) throws Exception {
        //given
        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.get(
                API_URL + "{workspaceId}/channels/exists", "testWorkspaceId")
            .param("name", name);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("name 이 같은 워크스페이스내에 다른 채널의 이름과 같다면")
    class ContextWithNameIsDuplicatedWithTheNameOfAnotherChannelInTheSameWorkspace {
      @Test
      @DisplayName("실패 ApiResponse 를 응답한다")
      void ItResponseFailApiResponse() throws Exception {
        //given
        ApiResponse expectedResponse = ApiResponse.fail(
            "Name is duplicated with the name of another channel in the same workspace");
        given(channelService.verifyName(anyString(), anyString()))
            .willReturn(expectedResponse);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.get(
                API_URL + "{workspaceId}/channels/exists", "testWorkspaceId")
            .param("name", "testName");

        MvcResult mvcResult = mockMvc.perform(request)
            .andReturn();

        //then
        ApiResponse response = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ApiResponse.class);
        assertThat(response.getResult()).isEqualTo(expectedResponse.getResult());
        assertThat(response.getMessage()).isEqualTo(expectedResponse.getMessage());
      }
    }

    @Nested
    @DisplayName("name 이 같은 워크스페이스내에 다른 멤버의 이름과 같다면")
    class ContextWithNameIsDuplicatedWithTheNameOfAnotherMemberInTheSameWorkspace {
      @Test
      @DisplayName("실패 ApiResponse 를 응답한다")
      void ItResponseFailApiResponse() throws Exception {
        //given
        ApiResponse expectedResponse = ApiResponse.fail(
            "Name is duplicated with the name of another member in the same workspace");
        given(channelService.verifyName(anyString(), anyString()))
            .willReturn(expectedResponse);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.get(
                API_URL + "{workspaceId}/channels/exists", "testWorkspaceId")
            .param("name", "testName");

        MvcResult mvcResult = mockMvc.perform(request)
            .andReturn();

        //then
        ApiResponse response = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ApiResponse.class);
        assertThat(response.getResult()).isEqualTo(expectedResponse.getResult());
        assertThat(response.getMessage()).isEqualTo(expectedResponse.getMessage());
      }
    }
  }
}
