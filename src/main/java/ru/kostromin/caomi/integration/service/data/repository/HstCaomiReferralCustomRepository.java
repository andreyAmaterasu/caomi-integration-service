package ru.kostromin.caomi.integration.service.data.repository;

import ru.kostromin.caomi.integration.service.data.dto.PatientDto;
import ru.kostromin.caomi.integration.service.data.dto.ReferralDto;
import ru.kostromin.caomi.integration.service.data.dto.ServiceRequestDto;

public interface HstCaomiReferralCustomRepository {

  ReferralDto getReferralDataByLaboratoryResearchId(String referralQuery, Integer labResId);

  PatientDto getPatientDataByLaboratoryResearchId(String patientQuery, Integer labResId);

  ServiceRequestDto getServiceRequestDataByLaboratoryResearchId(String referralRequestQuery, Integer labResId);

}
