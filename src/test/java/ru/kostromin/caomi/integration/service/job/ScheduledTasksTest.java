package ru.kostromin.caomi.integration.service.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.kostromin.caomi.integration.service.service.job.AppointmentAcceptReferralService;
import ru.kostromin.caomi.integration.service.service.job.CaomiDeviceService;
import ru.kostromin.caomi.integration.service.service.job.CaomiReferralService;
import ru.kostromin.caomi.integration.service.service.job.CaomiReconciliationReferralService;

@ExtendWith(SpringExtension.class)
class ScheduledTasksTest {

  @Mock
  private CaomiDeviceService deviceService;
  @Mock
  private CaomiReferralService referralService;
  @Mock
  private CaomiReconciliationReferralService reconciliationService;
  @Mock
  private AppointmentAcceptReferralService acceptReferralService;

  private ScheduledTasks scheduledTasks;

  @BeforeEach
  void init(){
    scheduledTasks = new ScheduledTasks(deviceService, referralService,
        reconciliationService, acceptReferralService);
  }

  @Test
  void runAddDeviceTask(){
    scheduledTasks.runAddDeviceTask();

    Mockito.verify(deviceService, Mockito.times(1))
        .findAndSendDataToCaomi();
  }

  @Test
  void runAcceptReferralJob(){
    scheduledTasks.runAcceptReferralJob();

    Mockito.verify(referralService, Mockito.times(1))
        .findAndSendReferrals();
  }

  @Test
  void runReconciliationReferralJob() {
    scheduledTasks.runReconciliationReferralJob();

    Mockito.verify(reconciliationService, Mockito.times(1))
        .findReconciliationReferralsScheduledAndSendToCaomi();
    Mockito.verify(reconciliationService, Mockito.times(1))
        .findReconciliationReferralsNoSlotsAvailableAndSendToCaomi();
  }

  @Test
  public void runAcceptAppointmentReferralJob() {
    scheduledTasks.runAcceptAppointmentReferralJob();

    Mockito.verify(acceptReferralService, Mockito.times(1))
        .findAcceptReferralsAndSendToAppointment();
  }
}