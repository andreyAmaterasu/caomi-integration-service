package ru.kostromin.caomi.integration.service.data.repository;

import ru.kostromin.caomi.integration.service.data.dto.ReconciliationDto;

public interface ReconciliationReferralRepository {

  ReconciliationDto getReconciliationReferralByLabResId(String query, Integer labResId);

}
