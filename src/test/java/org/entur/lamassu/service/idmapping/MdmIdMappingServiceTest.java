package org.entur.lamassu.service.idmapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.entur.lamassu.client.mdm.MdmClient;
import org.entur.lamassu.client.mdm.dto.OkinaIdenfierDto;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ExtendWith(MockitoExtension.class)
class MdmIdMappingServiceTest {

  private static final String MDM_ID_PREFIX = "MOBIITI";

  @Mock
  private MdmClient mdmClient;

  @Mock
  private FeedProvider feedProvider;

  private MdmIdMappingService service;

  @BeforeEach
  void setUp() {
    service = new MdmIdMappingService(mdmClient, MDM_ID_PREFIX);
  }

  @Test
  void getSystemIdOriginalToSuperReturnsMappedIdWhenOrganisationExists() {
    OkinaIdenfierDto dto = new OkinaIdenfierDto();
    dto.setOriginalId("org-1");
    dto.setSuperId(42L);
    when(mdmClient.findSystemIdByOriginalId("org-1")).thenReturn(dto);

    String result = service.getSystemIdOriginalToSuper("org-1", feedProvider);

    assertEquals("MOBIITI:Organisation:42", result);
    verify(mdmClient, never()).createOrganisation(any());
  }

  @Test
  void getSystemIdOriginalToSuperCreatesOrganisationWhenNotFound() {
    when(mdmClient.findSystemIdByOriginalId("org-2")).thenThrow(newNotFound());

    OkinaIdenfierDto created = new OkinaIdenfierDto();
    created.setOriginalId("org-2");
    created.setSuperId(99L);
    when(mdmClient.createOrganisation("org-2")).thenReturn(created);

    String result = service.getSystemIdOriginalToSuper("org-2", feedProvider);

    assertEquals("MOBIITI:Organisation:99", result);
    verify(mdmClient, times(1)).createOrganisation("org-2");
  }

  @Test
  void getSystemIdOriginalToSuperFallsBackToUnmappedIdOnOtherErrors() {
    when(mdmClient.findSystemIdByOriginalId("org-3"))
      .thenThrow(
        HttpServerErrorException.create(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Internal Server Error",
          HttpHeaders.EMPTY,
          new byte[0],
          null
        )
      );

    String result = service.getSystemIdOriginalToSuper("org-3", feedProvider);

    assertEquals("org-3", result);
    verify(mdmClient, never()).createOrganisation(any());
  }

  @Test
  void getSystemIdOriginalToSuperFallsBackToUnmappedIdWhenCreationFails() {
    when(mdmClient.findSystemIdByOriginalId("org-4")).thenThrow(newNotFound());
    when(mdmClient.createOrganisation("org-4"))
      .thenThrow(
        HttpServerErrorException.create(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Internal Server Error",
          HttpHeaders.EMPTY,
          new byte[0],
          null
        )
      );

    String result = service.getSystemIdOriginalToSuper("org-4", feedProvider);

    assertEquals("org-4", result);
  }

  @Test
  void getSystemIdSuperToOriginalReturnsOriginalIdWhenPrefixMatches() {
    OkinaIdenfierDto dto = new OkinaIdenfierDto();
    dto.setOriginalId("org-5");
    when(mdmClient.findSystemIdBySuperId(eq(42L))).thenReturn(dto);

    String result = service.getSystemIdSuperToOriginal(
      "MOBIITI:Organisation:42",
      feedProvider
    );

    assertEquals("org-5", result);
  }

  @Test
  void getSystemIdSuperToOriginalFallsBackWhenPrefixDoesNotMatch() {
    String result = service.getSystemIdSuperToOriginal(
      "OTHER:Organisation:42",
      feedProvider
    );

    assertEquals("OTHER:Organisation:42", result);
  }

  private static HttpClientErrorException newNotFound() {
    return HttpClientErrorException.create(
      HttpStatus.NOT_FOUND,
      "Not Found",
      HttpHeaders.EMPTY,
      new byte[0],
      null
    );
  }
}
