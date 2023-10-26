-------------------------------
-- Author:		Скоков Денис
-- Create date: 06.05.2022
-- Description:	Создать триггер при обновлении поля ShownInSchedule у существующей записи оборудования в hlt_DocPRVD, 
--				по соответствующему rf_equipmentid находить запись в hst_caomiDevice для строки с этим hltEquipmentID и ставим DateChange = текущее дата/время
-- Region:		ПК
 -------------------------------
IF OBJECT_ID ('TRG_DocPRVD_U_up_caomiDevice', 'TR') IS NOT NULL DROP TRIGGER dbo.TRG_DocPRVD_U_up_caomiDevice;
GO
CREATE TRIGGER dbo.TRG_DocPRVD_U_up_caomiDevice
ON dbo.hlt_DocPRVD
AFTER UPDATE
AS
BEGIN
IF UPDATE(ShownInSchedule) 
UPDATE up
SET dateChange = GETDATE()
   ,statusID = 0
FROM deleted d
JOIN hlt_Equipment e (NOLOCK) ON e.EquipmentID = d.rf_EquipmentID
JOIN inserted i ON i.DocPRVDID = d.DocPRVDID
JOIN hst_caomiDevice up (NOLOCK) ON up.hltEquipmentGUID = e.GUID
WHERE e.GUID <> '00000000-0000-0000-0000-000000000000'
AND i.ShownInSchedule <> d.ShownInSchedule;
END;