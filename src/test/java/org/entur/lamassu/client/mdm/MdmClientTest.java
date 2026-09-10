package org.entur.lamassu.client.mdm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.entur.lamassu.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class MdmClientTest {

  private static final String BASE_URL = "http://localhost:7878/api/v1/";

  @Mock
  private TokenService tokenService;

  private MockRestServiceServer mockServer;
  private MdmClient mdmClient;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    mockServer = MockRestServiceServer.bindTo(builder).build();
    mdmClient = new MdmClient(builder.build(), tokenService);
  }

  @Test
  void createOrganisationPostsOriginalIdAndReturnsBody() {
    when(tokenService.getToken()).thenReturn("token");

    mockServer
      .expect(requestTo(BASE_URL + "organisations"))
      .andExpect(method(HttpMethod.POST))
      .andExpect(header("Authorization", "Bearer token"))
      .andExpect(content().json("{\"dataset\":\"org-1\",\"originalId\":\"org-1\"}"))
      .andRespond(
        withSuccess(
          "{\"dataset\":\"org-1\",\"originalId\":\"org-1\",\"superId\":42}",
          MediaType.APPLICATION_JSON
        )
      );

    var result = mdmClient.createOrganisation("org-1");

    assertEquals("org-1", result.getOriginalId());
    assertEquals(42L, result.getSuperId());
    mockServer.verify();
  }
}
