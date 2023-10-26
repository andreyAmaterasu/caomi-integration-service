package ru.kostromin.caomi.integration.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.kostromin.caomi.integration.service.feign.request.device.CaomiDeviceRequest;
import ru.kostromin.caomi.integration.service.feign.request.reconciliation.CaomiReconciliationReferralRequest;
import ru.kostromin.caomi.integration.service.feign.request.referral.CaomiReferralRequest;
import ru.kostromin.caomi.integration.service.feign.response.CaomiBasicResponse;

@FeignClient(name = "caomiFeignClient", url = "${app.caomi.url}")
public interface CaomiFeignClient {

  @PostMapping(value = "/addDevice", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<CaomiBasicResponse> addDevice(@RequestBody CaomiDeviceRequest request);

  @PostMapping(value = "/acceptReferral", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<CaomiBasicResponse> acceptReferral(@RequestBody CaomiReferralRequest request);

  @PostMapping(value = "/reconciliationReferral", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<CaomiBasicResponse> postReconciliationReferral(@RequestBody
  CaomiReconciliationReferralRequest request);
}
