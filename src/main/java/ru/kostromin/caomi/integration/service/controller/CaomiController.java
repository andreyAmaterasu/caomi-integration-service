package ru.kostromin.caomi.integration.service.controller;

import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.AcceptReferralRequest;
import ru.kostromin.caomi.integration.service.controller.request.refreconciliation.ReconciliationReferralRequest;
import ru.kostromin.caomi.integration.service.controller.request.refresult.ResultReferralRequest;
import ru.kostromin.caomi.integration.service.controller.response.Response;
import ru.kostromin.caomi.integration.service.service.controller.AcceptReferralService;
import ru.kostromin.caomi.integration.service.service.controller.ReconciliationReferralService;
import ru.kostromin.caomi.integration.service.service.controller.ResultReferralService;

@RestController
@Slf4j
@RequestMapping
@RequiredArgsConstructor
public class CaomiController {

  private final ResultReferralService resultReferralService;
  private final AcceptReferralService acceptReferralService;
  private final ReconciliationReferralService reconciliationReferralService;

  /**
   * Сохранение исследования
   */
  @PostMapping("/api-v1/resultReferral")
  public ResponseEntity<Response> postApiV1ResultReferral(
      @Valid @RequestBody ResultReferralRequest request, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.of(Optional.of(Response.builder().errorText(
          "Не переданы поля направления: " + bindingResult.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.joining(", "))).errorCode(Response.ErrorCode.E001.getCode()).build()));

    }
    log.info("----- НАЧАЛО метод /api-v1/resultReferral получен запрос с idReferral = '{}' ------", request.getIdReferral());
    try{
      Response response = resultReferralService.processResultReferralRequest(request);
      log.info("----- метод /api-v1/resultReferral КОНЕЦ ------");
      return ResponseEntity.of(Optional.of(response));
    } catch (Exception e){
      log.error("Непредвиденное исключение при работа метода: ", e);
      return ResponseEntity.internalServerError().body(Response.builder()
          .errorText("Непредвиденное исключение при работе метода: " + e.getMessage())
          .build());
    }
  }

  /**
   * Сохранение направления
   */
  @PostMapping("/api-v1/acceptReferral")
  public ResponseEntity<Response> postApiV1AcceptReferral(
      @Valid @RequestBody AcceptReferralRequest request, BindingResult bindingResult){
    if (bindingResult.hasErrors()){
      return ResponseEntity.of(Optional.of(Response.builder().errorText(
          "Не переданы поля направления: " + bindingResult.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.joining(", "))).errorCode(Response.ErrorCode.E001.getCode()).build()));
    }
    log.info("----- НАЧАЛО метод /api-v1/acceptReferral получен запрос с idReferral = '{}' ------", request.getIdReferral());
    try{
      Response response = acceptReferralService.processAcceptReferralRequest(request);
      log.info("----- метод /api-v1/acceptReferral КОНЕЦ ------");
      return ResponseEntity.of(Optional.of(response));
    } catch (Exception e){
      log.error("Непредвиденное исключение при работа метода: ", e);
      return ResponseEntity.internalServerError().body(Response.builder()
          .errorText("Непредвиденное исключение при работе метода: " + e.getMessage())
          .build());
    }
  }

  /**
   * Публикация информации о согласовании направления
   */
  @PostMapping("/api-v1/reconciliationReferral")
  public ResponseEntity<Response> postApiV1reconciliationReferral(
      @Valid @RequestBody ReconciliationReferralRequest request, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.of(Optional.of(Response.builder().errorText(
          "Не переданы поля направления: " + bindingResult.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.joining(", "))).errorCode(Response.ErrorCode.E001.getCode()).build()));
    }
    log.info("----- НАЧАЛО метод /api-v1/reconciliationReferral получен запрос с idReferral = '{}' ------", request.getIdReferral());
    try {
      final Response response = reconciliationReferralService.processReconciliationReferralRequest(request);
      log.info("----- метод /api-v1/reconciliationReferral КОНЕЦ ------");
      return ResponseEntity.of(Optional.of(response));
    } catch (Exception e) {
      log.error("Непредвиденное исключение при работа метода: ", e);
      return ResponseEntity.internalServerError().body(Response.builder()
          .errorText("При обработке запроса произошла непредвиденная ошибка сервиса")
          .build());
    }
  }
}
