package ru.kostromin.caomi.integration.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.kostromin.caomi.integration.service.feign.request.appointment.AppointmentRequest;
import ru.kostromin.caomi.integration.service.feign.response.AppointmentResponse;

@FeignClient(name = "appointmentFeignClient", url = "${app.appointment.url}")
public interface AppointmentFeignClient {

  @PostMapping(value = "/appointment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<AppointmentResponse> postAppointment(@RequestBody AppointmentRequest request);

}
