-------------------------------
-- Author:                Юлдашева Лейла
-- Create date: 27.10.2022
-- Description:        Создать триггер при создании новой записи по услуге оборудования в hlt_DocPrvdNomService для всех hlt_DocPrvd dp с условием join hlt_resourceType rt on dp.rf_resourcetypeid=rt.resourcetypeid and rt.code='3' (.
--                                 Через связку таблиц hlt_DocPRVD и hlt_Equipment определяем идентификатор оборудования, по которому добавили услугу и
--                                Обновляем сведения в hst_caomiDevice для строки с этим hltEquipmentGUID и ставим DateChange=текущее дата/время, statusID = 0 - переводим статус строки обратно в создан если строка не нашлась
-- Region:                ПК
-------------------------------
IF OBJECT_ID ('TRG_DocPrvdNomService_IU_add_up_caomiDevice', 'TR') IS NOT NULL DROP TRIGGER dbo.TRG_DocPrvdNomService_IU_add_up_caomiDevice;
GO
CREATE TRIGGER dbo.TRG_DocPrvdNomService_IU_add_up_caomiDevice
    on dbo.hlt_DocPrvdNomService
    after insert, update
                      as
begin
if update(EquipmentID)
   INSERT INTO hst_caomiDevice (hltEquipmentGUID)
select he.GUID
from inserted i
         join hlt_DocPrvd dp on i.rf_DocPRVDID = dp.DocPRVDID
         join hlt_resourceType rt on dp.rf_resourcetypeid=rt.resourcetypeid and rt.code='3'
         join hlt_Equipment he on he.EquipmentID = dp.rf_EquipmentID
         LEFT JOIN hst_caomiDevice cd (NOLOCK) ON cd.hltEquipmentGUID = he.GUID
WHERE cd.hltEquipmentGUID IS NULL;
else update up
     set hltEquipmentGUID = i.guid
       , DateChange = GETDATE()
         from hst_caomiDevice up (NOLOCK)
join hlt_Equipment he on up.hltEquipmentGUID = he.GUID
         join hlt_DocPrvd dp on he.EquipmentID = dp.rf_EquipmentID
         join inserted i on i.rf_DocPRVDID = dp.DocPRVDID
         join deleted d on d.rf_DocPRVDID = dp.DocPRVDID
     WHERE up.hltEquipmentGUID <> '00000000-0000-0000-0000-000000000000'
end;