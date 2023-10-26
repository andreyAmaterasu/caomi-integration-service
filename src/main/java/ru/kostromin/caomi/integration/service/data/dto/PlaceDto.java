package ru.kostromin.caomi.integration.service.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PlaceDto {

  @Column("idPlace")
  private String idPlace;

}
