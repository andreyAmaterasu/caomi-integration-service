-------------------------------
-- Author:		������ �����
-- Create date: 06.05.2022
-- Description:	������� ������� ��� ���������� ���� ShownInSchedule � ������������ ������ ������������ � hlt_DocPRVD, 
--				�� ���������������� rf_equipmentid �������� ������ � hst_caomiDevice ��� ������ � ���� hltEquipmentID � ������ DateChange = ������� ����/�����
-- Region:		��
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