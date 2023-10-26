package ru.kostromin.caomi.integration.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.kostromin.caomi.integration.service.service.job.AppointmentAcceptReferralService;
import ru.kostromin.caomi.integration.service.service.job.CaomiDeviceService;
import ru.kostromin.caomi.integration.service.service.job.CaomiReferralService;
import ru.kostromin.caomi.integration.service.service.job.CaomiReconciliationReferralService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

  private final CaomiDeviceService deviceService;

  private final CaomiReferralService referralService;

  private final CaomiReconciliationReferralService reconciliationService;

  private final AppointmentAcceptReferralService appointmentService;

  @Scheduled(cron = "${jobs.addDeviceJob.cron}")
  public void runAddDeviceTask(){
    log.info("------Start addDeviceJob ------");
    try{
      deviceService.findAndSendDataToCaomi();
    } catch (Exception e){
      log.error("Произошла непредвиденная ошибка при работе job: ", e);
    }
    log.info("------End addDeviceJob ------");
  }

  @Scheduled(cron = "${jobs.acceptReferralJob.cron}")
  public void runAcceptReferralJob(){
    log.info("------Start acceptReferralJob ------");
    try{
      referralService.findAndSendReferrals();
    } catch (Exception e){
      log.error("Произошла непредвиденная ошибка при работе job: ", e);
    }
    log.info("------End acceptReferralJob ------");
  }

  @Scheduled(cron = "${jobs.reconciliationReferralJob.cron}")
  public void runReconciliationReferralJob(){
    log.info("------Start reconciliationReferralJob ------");
    try{
      reconciliationService.findReconciliationReferralsScheduledAndSendToCaomi();
      reconciliationService.findReconciliationReferralsNoSlotsAvailableAndSendToCaomi();
    } catch (Exception e){
      log.error("Произошла непредвиденная ошибка при работе job: ", e);
    }
    log.info("------End reconciliationReferralJob ------");
  }

  @Scheduled(cron = "${jobs.acceptAppointmentReferralJob.cron}")
  public void runAcceptAppointmentReferralJob() {
    log.info("------Start acceptAppointmentReferralJob ------");
    try{
      appointmentService.findAcceptReferralsAndSendToAppointment();
    } catch (Exception e){
      log.error("Произошла непредвиденная ошибка при работе job: ", e);
    }
    log.info("------End acceptAppointmentReferralJob ------");
  }
}
