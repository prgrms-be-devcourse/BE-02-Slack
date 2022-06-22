package com.prgrms.be02slack.workspace.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

@WebMvcTest(
    controllers = WorkspaceApiController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@AutoConfigureRestDocs
class WorkspaceApiControllerTest {

  private static final String API_URL = "/api/v1/workspaces";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private WorkspaceService workspaceService;

  static class NameSourceOutOfRange implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of((Object)null),
          Arguments.of(""),
          Arguments.of("\t"),
          Arguments.of("\n"),
          Arguments.of("a".repeat(51))
      );
    }
  }

  static class UrlSourceOutOfRange implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of((Object)null),
          Arguments.of(""),
          Arguments.of("\t"),
          Arguments.of("\n"),
          Arguments.of("a".repeat(22))
      );
    }
  }

  @Nested
  @DisplayName("update 메서드는")
  class DescribeUpdate {

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {

      @Test
      @DisplayName("Ok 를 응답한다")
      void ItResponseOk() throws Exception {
        //given
        final var workspaceKey = "testKey";
        final var requestMap = Map.of(
            "name", "testName",
            "url", "testUrl"
        );
        final var requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        final var request =
            RestDocumentationRequestBuilders.put(API_URL + "/{key}", workspaceKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final var response = mockMvc.perform(request);

        //then
        verify(workspaceService).update(anyString(), any(Workspace.class));
        response.andExpect(status().isOk())
            .andDo(document("Update workspace",
                pathParameters(
                    parameterWithName("key").description("workspace key")
                ),
                requestFields(
                    fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("workspace name"),
                    fieldWithPath("url")
                        .type(JsonFieldType.STRING)
                        .description("work space url")
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
        final var workspaceKey = "testKey";

        final var requestMap = new HashMap<>();
        requestMap.put("name", name);
        requestMap.put("url", "testUrl");
        final var requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        final var request =
            RestDocumentationRequestBuilders.put(API_URL + "/{key}", workspaceKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final var response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("Url 의 길이가 범위를 벗어나면")
    class ContextWithUrlOutOfRange {

      @ParameterizedTest
      @ArgumentsSource(UrlSourceOutOfRange.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String url) throws Exception {
        //given
        final var workspaceKey = "testKey";

        final var requestMap = new HashMap<>();
        requestMap.put("name", "testName");
        requestMap.put("url", url);
        final var requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        final var request =
            RestDocumentationRequestBuilders.put(API_URL + "/{key}", workspaceKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        final var response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }
  }
}
