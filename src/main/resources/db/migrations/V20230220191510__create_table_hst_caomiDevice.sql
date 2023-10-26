-------------------------------
-- Author:		Скоков Денис
-- Create date: 06.05.2022
-- Update date: 01.11.2022
-- Description:	Таблица хранения информации об оборудовании для интеграции с шины caomi-fhir(ЦАМИ).
-- Region:		ПК
 -------------------------------
IF OBJECT_ID('hst_caomiDevice', 'U') IS NOT NULL DROP TABLE hst_caomiDevice;
GO
CREATE TABLE hst_caomiDevice (caomiDeviceID int IDENTITY(1, 1) PRIMARY KEY CLUSTERED
							,hltEquipmentGUID uniqueidentifier DEFAULT '00000000-0000-0000-0000-000000000000' --Уникальный идентификатор оборудования
							,dateCreate	datetime DEFAULT '1900-01-01T00:00:00.000' --Дата cоздания новой записи
							,dateChange	datetime DEFAULT '1900-01-01T00:00:00.000' --Дата фиксации изменений в записи
							,caomiID int DEFAULT 0 --Уникальный идентификатор оборудования в шине caomi-fhir
							,statusID int DEFAULT 0 --Статус записи (0 - создана(обновлена), 1 - отправлена в шину caomi_fhir
							,dateStatus	datetime DEFAULT '1900-01-01T00:00:00.000' --Дата ответа из шины caomi-fhir
							,httpStatus	int	DEFAULT 0 --Статус, который прислала шина caomi-fhir
							,errorCode varchar(100) DEFAULT '' --Код ошибки, который прислала шина caomi-fhir
							,errorText varchar(100) DEFAULT '' --Текст ошибки, который прислала шина caomi-fhir
							);
--описание таблицы
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', default, default))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Таблица хранения информации об оборудовании для интеграции с шины caomi-fhir(ЦАМИ).', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice'; 
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'hltEquipmentGUID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Уникальный идентификатор оборудования', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'hltEquipmentGUID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'dateCreate'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Дата cоздания новой записи', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'dateCreate';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'dateChange'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Дата фиксации изменений в записи', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'dateChange';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'caomiID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Уникальный идентификатор оборудования в шине caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'caomiID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'statusID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Статус записи (0 - создана(обновлена), 1 - отправлена в шину caomi_fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'statusID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'dateStatus'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Дата ответа из шины caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'dateStatus';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'httpStatus'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Статус, который прислала шина caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'httpStatus';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'errorCode'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Код ошибки, который прислала шина caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'errorCode';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiDevice', 'COLUMN', 'errorText'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
							  ,@value = 'Текст ошибки, который прислала шина caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
							  ,@level1name = 'hst_caomiDevice', @level2type = 'COLUMN'
							  ,@level2name = 'errorText';