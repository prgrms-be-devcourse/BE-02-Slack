package com.prgrms.be02slack.workspace.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.repository.WorkspaceRepository;

@ExtendWith(MockitoExtension.class)
class DefaultWorkspaceServiceTest {

  @Mock
  private IdEncoder idEncoder;

  @Mock
  private WorkspaceRepository workspaceRepository;

  @InjectMocks
  private DefaultWorkspaceService defaultWorkspaceService;

  @Nested
  @DisplayName("update 메서드는")
  class DescribeUpdate {

    @Nested
    @DisplayName("빈 키 값이 전달되면")
    class ContextWithBlankKey {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", "", " "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String src) {
        final var testWorkspace = new Workspace("test", "test");
        assertThrows(IllegalArgumentException.class,
            () -> defaultWorkspaceService.update(src, testWorkspace));
      }
    }

    @Nested
    @DisplayName("workspace 파라미터에 null 값이 전달되면")
    class ContextWithWorkspaceNull {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> defaultWorkspaceService.update("testKey", null));
      }
    }

    @Nested
    @DisplayName("key 값에 해당하는 workspace 가 존재하지 않는 경우")
    class ContextWithNotExistWorkspace {

      @Test
      @DisplayName("NotFoundException 에러를 발생시킨다")
      void ItThrowsNotfoundException() {
        //given
        final var workspace = new Workspace("test", "test");
        given(idEncoder.decode(anyString())).willReturn(1L);
        given(workspaceRepository.findById(anyLong())).willReturn(Optional.empty());

        //when, then
        assertThrows(NotFoundException.class,
            () -> defaultWorkspaceService.update("test", workspace));
      }
    }

    @Nested
    @DisplayName("key 값에 해당하는 workspace 가 존재하는 경우")
    class ContextWithExistWorkspace {

      @Test
      @DisplayName("해당 workspace 내용을 업데이트 한다")
      void It() {
        //given
        final var updateWorkspace = new Workspace("update", "update");
        final var existWorkspace = new Workspace("exist", "exist");

        given(idEncoder.decode(anyString())).willReturn(1L);
        given(workspaceRepository.findById(anyLong())).willReturn(Optional.of(existWorkspace));

        //when
        defaultWorkspaceService.update("test", updateWorkspace);

        //then
        final var name = (String)ReflectionTestUtils.getField(existWorkspace, "name");
        final var url = (String)ReflectionTestUtils.getField(existWorkspace, "url");

        assertEquals("update", name);
        assertEquals("update", url);
      }
    }
  }
}
