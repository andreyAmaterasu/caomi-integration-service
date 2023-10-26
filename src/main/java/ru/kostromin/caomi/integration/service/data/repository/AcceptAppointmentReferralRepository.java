package ru.kostromin.caomi.integration.service.data.repository;

import ru.kostromin.caomi.integration.service.data.dto.EquipmentSlotsDto;
import ru.kostromin.caomi.integration.service.data.dto.PlaceDto;

public interface AcceptAppointmentReferralRepository {
  EquipmentSlotsDto findEquipmentDataByLaboratoryResearchId(String query, Integer labResId);

  PlaceDto findIdPlaceByDoctorTimeTableId(String query, Integer doctorTimeTableId);
}
