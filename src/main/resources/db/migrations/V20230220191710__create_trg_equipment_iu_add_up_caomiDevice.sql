-------------------------------
-- Author:		������ �����
-- Create date: 06.05.2022
-- Description:	������� ������� ��� �������� ����� ������ �� ������������ (�������� � hlt_Equipment). ��������� �������� � hst_caomiDevice ���� hltEquipmentID, ��������� ���� ��������� �������.
--				��� ���������� ���� Name, GUID � ������������ ������ �� ������������ � hlt_Equipment. ��������� �������� � hst_caomiDevice ��� ������ � ���� hltEquipmentID � ������ DateChange=������� ����/�����.
-- Region:		��
 -------------------------------
IF OBJECT_ID ('TRG_Equipment_IU_add_up_caomiDevice', 'TR') IS NOT NULL DROP TRIGGER dbo.TRG_Equipment_IU_add_up_caomiDevice;
GO
CREATE TRIGGER dbo.TRG_Equipment_IU_add_up_caomiDevice
ON dbo.hlt_Equipment
AFTER INSERT, UPDATE
AS
BEGIN
IF UPDATE(EquipmentID) 
INSERT INTO hst_caomiDevice (hltEquipmentGUID, dateCreate)
SELECT i.GUID, GETDATE()
FROM inserted i
LEFT JOIN hst_caomiDevice cd (NOLOCK) ON cd.hltEquipmentGUID = i.GUID
WHERE cd.hltEquipmentGUID IS NULL;
ELSE
UPDATE up
SET hltEquipmentGUID = i.GUID
   ,dateChange = GETDATE()
   ,statusID = 0
FROM hst_caomiDevice up (NOLOCK) 
JOIN deleted d ON d.GUID = up.hltEquipmentGUID
JOIN inserted i ON i.EquipmentID = d.EquipmentID
WHERE up.hltEquipmentGUID <> '00000000-0000-0000-0000-000000000000'
AND (d.Name <> i.Name OR d.GUID <> i.GUID);
END;