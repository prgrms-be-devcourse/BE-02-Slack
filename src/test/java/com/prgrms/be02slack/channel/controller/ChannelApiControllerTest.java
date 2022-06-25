package com.prgrms.be02slack.channel.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.service.ChannelService;

@WebMvcTest(
    controllers = ChannelApiController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@AutoConfigureRestDocs
class ChannelApiControllerTest {
  private static final String API_URL = "/api/v1/channels";
  private static final String CREATE_CHANNEL_URL = API_URL;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ChannelService channelService;

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

  static class WorkspaceBlank implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of((Object)null),
          Arguments.of(""),
          Arguments.of("\t"),
          Arguments.of("\n")
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
        given(channelService.create(any(ChannelSaveRequest.class)))
            .willReturn("testId");

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("name", "testName");
        requestMap.put("description", "testDescription");
        requestMap.put("isPrivate", false);
        requestMap.put("workspaceId", "workspaceId");

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                CREATE_CHANNEL_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        verify(channelService).create(any(ChannelSaveRequest.class));
        response.andExpect(status().isOk())
            .andDo(document("Create channel",
                requestFields(
                    fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("channel name"),
                    fieldWithPath("description")
                        .type(JsonFieldType.STRING)
                        .description("channel description"),
                    fieldWithPath("isPrivate")
                        .type(JsonFieldType.BOOLEAN)
                        .description("whether the channel is open to the public"),
                    fieldWithPath("workspaceId")
                        .type(JsonFieldType.STRING)
                        .description("workspace of channel")
                )
            ));
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
        requestMap.put("workspaceId", "workspaceId");

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                CREATE_CHANNEL_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("workspaceId 가 null 이거나 공백 또는 빈 값이라면")
    class ContextWithWorkspaceIdBlank {

      @ParameterizedTest
      @ArgumentsSource(WorkspaceBlank.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String workspaceId) throws Exception {
        //given
        HashMap<Object, Object> requestMap = new HashMap<>();
        requestMap.put("name", "testName");
        requestMap.put("description", "testDescription");
        requestMap.put("isPrivate", false);
        requestMap.put("workspaceId", workspaceId);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                CREATE_CHANNEL_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }
  }
}
