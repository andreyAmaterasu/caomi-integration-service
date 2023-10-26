package ru.kostromin.caomi.integration.service.feign.request.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class AppointmentRequest {

  @JsonProperty("medicalServiceId")
  private String medicalServiceId;

  @JsonProperty("referralNum")
  private String referralNum;

  @JsonProperty("id")
  private String id;

  @JsonProperty("idPlace")
  private String idPlace;

  @JsonProperty("idSlot")
  private String idSlot;

  @JsonProperty("idSchedule")
  private String idSchedule;

  @JsonProperty("timeBegin")
  private LocalDateTime timeBegin;

  @JsonProperty("dateSlot")
  private LocalDate dateSlot;

  @JsonProperty("patient")
  private Patient patient;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Patient {

    @JsonProperty("oms")
    private String oms;

    @JsonProperty("birthday")
    private LocalDate birthDate;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("name")
    private String name;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("patronymic")
    private String patronymic;
  }
}
