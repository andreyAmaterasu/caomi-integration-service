package ru.kostromin.caomi.integration.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.AcceptReferralRequest;
import ru.kostromin.caomi.integration.service.controller.request.refreconciliation.ReconciliationReferralRequest;
import ru.kostromin.caomi.integration.service.controller.request.refresult.ResultReferralRequest;
import ru.kostromin.caomi.integration.service.controller.response.Response;
import ru.kostromin.caomi.integration.service.controller.response.Response.ErrorCode;
import ru.kostromin.caomi.integration.service.service.controller.AcceptReferralService;
import ru.kostromin.caomi.integration.service.service.controller.ReconciliationReferralService;
import ru.kostromin.caomi.integration.service.service.controller.ResultReferralService;

@ContextConfiguration(classes = CaomiController.class)
@WebMvcTest(CaomiController.class)
@AutoConfigureMockMvc
class CaomiControllerTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @MockBean
  private ResultReferralService resultReferralService;
  @MockBean
  private AcceptReferralService acceptReferralService;
  @MockBean
  private ReconciliationReferralService reconciliationReferralService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Метод /api-v1/resultReferral 'Сохранение исследования'."
      + "В запросе присутсвуют все необходимые поля."
      + "Ответ от сервиса возвращен.")
  void postApiV1ResultReferralRequestHasAllRequiredFields_responseReturned()
      throws Exception {
    // (1) prepare mocks:
    Response expResponse = Response.builder()
        .errorCode("anyErrorCode")
        .errorText("anyErrorText")
        .build();
    Mockito.when(resultReferralService
        .processResultReferralRequest(Mockito.any(ResultReferralRequest.class)))
            .thenReturn(expResponse);

    String allRequiredFieldsPresent = readFileAsString("src/test/resources/resultReferralValid.json");


    // (2) start test (3) and check:
    mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/resultReferral")
        .content(allRequiredFieldsPresent)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(expResponse.getErrorCode()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorText").value(expResponse.getErrorText()));
  }

  @Test
  @DisplayName("Метод /api-v1/resultReferral 'Сохранение исследования'."
      + "В запросе отсутсвуют необходимые поля."
      + "Ответ с недостающими полями возвращен.")
  void postApiV1ResultReferralRequiredFieldsAbsent_errorResponseReturned()
      throws Exception {
    // (1) prepare mocks:
    String allRequiredFieldsNotPresent = readFileAsString("src/test/resources/resultReferralNotValid.json");

    // (2) start test:
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/resultReferral")
            .content(allRequiredFieldsNotPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    // (3) check:
    Response response = objectMapper.readValue(result.getResponse().getContentAsString(
        StandardCharsets.UTF_8), Response.class);
    Assertions.assertEquals(ErrorCode.E001.getCode(), response.getErrorCode());
    Assertions.assertTrue(response.getErrorText().contains("idReferral"));
    Assertions.assertTrue(response.getErrorText().contains("serviceId"));
    Assertions.assertTrue(response.getErrorText().contains("patientId"));
    Assertions.assertTrue(response.getErrorText().contains("medStaffId"));
    Assertions.assertTrue(response.getErrorText().contains("firstName"));
    Assertions.assertTrue(response.getErrorText().contains("lastName"));
    Assertions.assertTrue(response.getErrorText().contains("birthDate"));
    Assertions.assertTrue(response.getErrorText().contains("medStaffRoleId"));
    Assertions.assertTrue(response.getErrorText().contains("active"));
    Assertions.assertTrue(response.getErrorText().contains("moOid"));
    Assertions.assertTrue(response.getErrorText().contains("positionCode"));
    Assertions.assertTrue(response.getErrorText().contains("specialityCode"));
  }

  @Test
  @DisplayName("Метод /api-v1/resultReferral 'Сохранение исследования'."
      + "Произошла непредвиденная ошибка."
      + "500 статус и ответ с ошибкой возвращены.")
  void postApiV1ResultReferralRequestHasAllRequiredFieldsButServiceThrewError_errorResponseReturned()
      throws Exception {
    // (1) prepare mocks:
    RuntimeException thrownException = new RuntimeException("что-то пошло не так");
    Mockito.doThrow(thrownException).when(resultReferralService)
            .processResultReferralRequest(Mockito.any(ResultReferralRequest.class));

    String allRequiredFieldsPresent = readFileAsString("src/test/resources/resultReferralValid.json");

    // (2) start test (3) and check:
    mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/resultReferral")
            .content(allRequiredFieldsPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isInternalServerError())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorText")
            .value("Непредвиденное исключение при работе метода: " + thrownException.getMessage()));
  }

  @Test
  @DisplayName("Метод /api-v1/acceptReferral 'Сохранение направления'."
      + "В запросе присутсвуют все необходимые поля."
      + "Ответ от сервиса возвращен.")
  void postApiV1AcceptReferralRequestHasAllRequiredFields_responseReturned()
      throws Exception {
    // (1) prepare mocks:
    Response expResponse = Response.builder()
        .errorCode("anyErrorCode")
        .errorText("anyErrorText")
        .build();
    Mockito.when(acceptReferralService
            .processAcceptReferralRequest(Mockito.any(AcceptReferralRequest.class)))
        .thenReturn(expResponse);

    String allRequiredFieldsPresent = readFileAsString("src/test/resources/acceptReferralValid.json");


    // (2) start test (3) and check:
    mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/acceptReferral")
            .content(allRequiredFieldsPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(expResponse.getErrorCode()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorText").value(expResponse.getErrorText()));
  }

  @Test
  @DisplayName("Метод /api-v1/acceptReferral 'Сохранение направления'."
      + "В запросе отсутсвуют необходимые поля."
      + "Ответ с недостающими полями возвращен.")
  void postApiV1AcceptReferralRequiredFieldsAbsent_errorResponseReturned()
      throws Exception {
    // (1) prepare mocks:
    String allRequiredFieldsNotPresent = readFileAsString("src/test/resources/acceptReferralNotValid.json");

    // (2) start test:
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/acceptReferral")
            .content(allRequiredFieldsNotPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    // (3) check:
    Response response = objectMapper.readValue(result.getResponse().getContentAsString(
        StandardCharsets.UTF_8), Response.class);
    Assertions.assertEquals(ErrorCode.E001.getCode(), response.getErrorCode());
    Assertions.assertTrue(response.getErrorText().contains("moOid"));
    Assertions.assertTrue(response.getErrorText().contains("referralNumber"));
    Assertions.assertTrue(response.getErrorText().contains("idReferral"));
    Assertions.assertTrue(response.getErrorText().contains("medStaffId"));
    Assertions.assertTrue(response.getErrorText().contains("firstName"));
    Assertions.assertTrue(response.getErrorText().contains("lastName"));
    Assertions.assertTrue(response.getErrorText().contains("birthDate"));
    Assertions.assertTrue(response.getErrorText().contains("medStaffRoleId"));
    Assertions.assertTrue(response.getErrorText().contains("active"));
    Assertions.assertTrue(response.getErrorText().contains("moOid"));
    Assertions.assertTrue(response.getErrorText().contains("positionCode"));
    Assertions.assertTrue(response.getErrorText().contains("specialityCode"));
    Assertions.assertTrue(response.getErrorText().contains("patientId"));
    Assertions.assertTrue(response.getErrorText().contains("snils"));
    Assertions.assertTrue(response.getErrorText().contains("gender"));
    Assertions.assertTrue(response.getErrorText().contains("firstName"));
    Assertions.assertTrue(response.getErrorText().contains("lastName"));
    Assertions.assertTrue(response.getErrorText().contains("birthDate"));
    Assertions.assertTrue(response.getErrorText().contains("generalPractitionerMoOid"));
    Assertions.assertTrue(response.getErrorText().contains("policyTypeCode"));
    Assertions.assertTrue(response.getErrorText().contains("policyNumber"));
    Assertions.assertTrue(response.getErrorText().contains("policyStatus"));
    Assertions.assertTrue(response.getErrorText().contains("policyNumber"));
    Assertions.assertTrue(response.getErrorText().contains("serviceId"));
    Assertions.assertTrue(response.getErrorText().contains("serviceStatusCode"));
    Assertions.assertTrue(response.getErrorText().contains("serviceIntentCode"));
    Assertions.assertTrue(response.getErrorText().contains("serviceCode"));
    Assertions.assertTrue(response.getErrorText().contains("start"));
    Assertions.assertTrue(response.getErrorText().contains("performerTypeCode"));
    Assertions.assertTrue(response.getErrorText().contains("performerMoOid"));
    Assertions.assertTrue(response.getErrorText().contains("reasonCode"));
  }

  @Test
  @DisplayName("Метод /api-v1/acceptReferral 'Сохранение направления'."
      + "Произошла непредвиденная ошибка."
      + "500 статус и ответ с ошибкой возвращены.")
  void postApiV1AcceptReferralRequestHasAllRequiredFieldsButServiceThrewError_errorResponseReturned()
      throws Exception {
    // (1) prepare mocks:
    RuntimeException thrownException = new RuntimeException("что-то пошло не так");
    Mockito.doThrow(thrownException).when(acceptReferralService)
        .processAcceptReferralRequest(Mockito.any(AcceptReferralRequest.class));

    String allRequiredFieldsPresent = readFileAsString("src/test/resources/acceptReferralValid.json");

    // (2) start test (3) and check:
    mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/acceptReferral")
            .content(allRequiredFieldsPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isInternalServerError())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorText")
            .value("Непредвиденное исключение при работе метода: " + thrownException.getMessage()));
  }

  @Test
  @DisplayName("Метод /api-v1/reconciliationReferral 'Публикация информации о согласовании направления'."
      + "В запросе присутствуют все необходимые поля (rejectionReason is present, agreedReferral is false)."
      + "Ответ от сервиса возвращен.")
  void postApiV1reconciliationReferralRequestHasAllRequiredFields_responseReturned()
      throws Exception {
    // (1) prepare mocks:
    final Response expResponse = Response.builder()
        .errorCode("anyErrorCode")
        .errorText("anyErrorText")
        .build();
    Mockito.when(reconciliationReferralService
            .processReconciliationReferralRequest(Mockito.any(ReconciliationReferralRequest.class)))
        .thenReturn(expResponse);

    final String allRequiredFieldsPresent = readFileAsString("src/test/resources/reconciliationReferralValid.json");

    // (2) start test (3) and check:
    mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/reconciliationReferral")
            .content(allRequiredFieldsPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(expResponse.getErrorCode()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorText").value(expResponse.getErrorText()));
  }

  @Test
  @DisplayName("Метод /api-v1/reconciliationReferral 'Публикация информации о согласовании направления'."
      + "В запросе присутствуют все необходимые поля (rejectionReason is absent, agreedReferral is true)."
      + "Ответ от сервиса возвращен.")
  void postApiV1reconciliationReferralAgreedReferralIsTrueRejectionReasonAbsent_responseReturned()
      throws Exception {
    // (1) prepare mocks:
    final Response expResponse = Response.builder()
        .errorCode("anyErrorCode")
        .errorText("anyErrorText")
        .build();
    Mockito.when(reconciliationReferralService
            .processReconciliationReferralRequest(Mockito.any(ReconciliationReferralRequest.class)))
        .thenReturn(expResponse);

    final String allRequiredFieldsPresent = readFileAsString("src/test/resources/reconciliationReferralValid_2.json");

    // (2) start test (3) and check:
    mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/reconciliationReferral")
            .content(allRequiredFieldsPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(expResponse.getErrorCode()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorText").value(expResponse.getErrorText()));
  }

  @Test
  @DisplayName("Метод /api-v1/reconciliationReferral 'Публикация информации о согласовании направления'."
      + "В запросе отсутствуют необходимые поля."
      + "Ответ с недостающими полями возвращен (rejectionReason не указан, т.к. agreedReferral = null).")
  void postApiV1reconciliationReferralRequiredFieldsAbsent_errorResponseReturned()
      throws Exception {
    // (1) prepare mocks:
    final String allRequiredFieldsNotPresent = readFileAsString("src/test/resources/reconciliationReferralNotValid.json");

    // (2) start test:
    final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/reconciliationReferral")
            .content(allRequiredFieldsNotPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    // (3) check:
    final Response response = objectMapper.readValue(result.getResponse().getContentAsString(
        StandardCharsets.UTF_8), Response.class);
    Assertions.assertEquals(ErrorCode.E001.getCode(), response.getErrorCode());
    Assertions.assertTrue(response.getErrorText().contains("idReferral"));
    Assertions.assertTrue(response.getErrorText().contains("agreedReferral"));
    Assertions.assertTrue(response.getErrorText().contains("occurencePeriod.start"));
    Assertions.assertTrue(response.getErrorText().contains("occurencePeriod.end"));
  }

  @Test
  @DisplayName("Метод /api-v1/reconciliationReferral 'Публикация информации о согласовании направления'."
      + "В запросе отсутствует поле rejectionReason (rejectionReason is absent, agreedReferral is false)."
      + "Ответ с недостающим полем возвращен.")
  void postApiV1reconciliationReferralAgreedReferralIsFalseRejectionReasonAbsent_errorResponseReturned()
      throws Exception {
    // (1) prepare mocks:
    final String allRequiredFieldsNotPresent = readFileAsString("src/test/resources/reconciliationReferralNotValid_2.json");

    // (2) start test:
    final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/reconciliationReferral")
            .content(allRequiredFieldsNotPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    // (3) check:
    final Response response = objectMapper.readValue(result.getResponse().getContentAsString(
        StandardCharsets.UTF_8), Response.class);
    Assertions.assertEquals(ErrorCode.E001.getCode(), response.getErrorCode());
    Assertions.assertTrue(response.getErrorText().contains("rejectionReason"));
  }

  @Test
  @DisplayName("Метод /api-v1/reconciliationReferral 'Публикация информации о согласовании направления'."
      + "Произошла непредвиденная ошибка."
      + "500 статус и ответ с ошибкой возвращены.")
  void postApiV1reconciliationReferralRequestHasAllRequiredFieldsButServiceThrewError_errorResponseReturned()
      throws Exception {
    // (1) prepare mocks:
    final RuntimeException thrownException = new RuntimeException("что-то пошло не так");
    Mockito.doThrow(thrownException).when(reconciliationReferralService)
        .processReconciliationReferralRequest(Mockito.any(ReconciliationReferralRequest.class));

    final String allRequiredFieldsPresent = readFileAsString("src/test/resources/reconciliationReferralValid.json");

    // (2) start test (3) and check:
    mockMvc.perform(MockMvcRequestBuilders.post("/api-v1/reconciliationReferral")
            .content(allRequiredFieldsPresent)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isInternalServerError())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorText")
            .value("При обработке запроса произошла непредвиденная ошибка сервиса"));
  }

  private String readFileAsString(String path) throws IOException {
    try {
      byte[] allBytes = Files.readAllBytes(Paths.get(path));
      return new String(allBytes, StandardCharsets.UTF_8);
    } catch (IOException var2) {
      throw new IOException("Unable to read file " + path, var2);
    }
  }
}