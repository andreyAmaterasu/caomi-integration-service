-------------------------------
-- Author:		������ �����
-- Create date: 06.05.2022
-- Update date: 01.11.2022
-- Description:	������� �������� ���������� �� ������������ ��� ���������� � ���� caomi-fhir(����).
-- Region:		��
 -------------------------------
IF OBJECT_ID('hst_caomiDevice', 'U') IS NOT NULL DROP TABLE hst_caomiDevice;
GO
CREATE TABLE hst_caomiDevice (caomiDeviceID int IDENTITY(1, 1) PRIMARY KEY CLUSTERED
							,hltEquipmentGUID uniqueidentifier DEFAULT '00000000-0000-0000-0000-000000000000' --���������� ������������� ������������
							,dateCreate	datetime DEFAULT '1900-01-01T00:00:00.000' --���� c������� ����� ������
							,dateChange	datetime DEFAULT '1900-01-01T00:00:00.000' --���� �������� ��������� � ������
							,caomiID int DEFAULT 0 --���������� ������������� ������������ � ���� caomi-fhir
							,statusID int DEFAULT 0 --������ ������ (0 - �������(���������), 1 - ���������� � ���� caomi_fhir
							,dateStatus	datetime DEFAULT '1900-01-01T00:00:00.000' --���� ������ �� ���� caomi-fhir
							,httpStatus	int	DEFAULT 0 --������, ������� �������� ���� caomi-fhir
							,errorCode varchar(100) DEFAULT '' --��� ������, ������� �������� ���� caomi-fhir
							,errorText varchar(100) DEFAULT '' --����� ������, ������� �������� ���� caomi-fhir
							);
--�������� �������
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', default, default))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '������� �������� ���������� �� ������������ ��� ���������� � ���� caomi-fhir(����).', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice'; 
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'hltEquipmentGUID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '���������� ������������� ������������', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'hltEquipmentGUID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'dateCreate'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '���� c������� ����� ������', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'dateCreate';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'dateChange'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '���� �������� ��������� � ������', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'dateChange';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'caomiID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '���������� ������������� ������������ � ���� caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'caomiID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'statusID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '������ ������ (0 - �������(���������), 1 - ���������� � ���� caomi_fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'statusID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'dateStatus'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '���� ������ �� ���� caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'dateStatus';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'httpStatus'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '������, ������� �������� ���� caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'httpStatus';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'errorCode'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '��� ������, ������� �������� ���� caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'errorCode';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'errorText'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = '����� ������, ������� �������� ���� caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'errorText';