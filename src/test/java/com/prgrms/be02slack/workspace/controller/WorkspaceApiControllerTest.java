package com.prgrms.be02slack.workspace.controller;

import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

@WebMvcTest(controllers = WorkspaceApiController.class)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@AutoConfigureRestDocs
class WorkspaceApiControllerTest {

  private static final String API_URL = "/api/v1/workspace";

}
