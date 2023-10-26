-------------------------------
-- Author:                Юлдашева Лейла
-- Create date: 10.10.2022
-- Description:        Таблица хранения информации о направлениях на исследование для интеграции с шины caomi-fhir(ЦАМИ).
-- Region:                ПК
-------------------------------
IF OBJECT_ID('hst_caomiReferral', 'U') IS NOT NULL DROP TABLE hst_caomiReferral;
GO
CREATE TABLE hst_caomiReferral (caomiReferralID int IDENTITY(1, 1) PRIMARY KEY CLUSTERED
    ,lbrLaboratoryResearchID int DEFAULT 0 --Уникальный идентификатор направления
    ,DateCreate        datetime DEFAULT '1900-01-01T00:00:00.000' --        Дата cоздания новой записи
    ,DateChange datetime DEFAULT '1900-01-01T00:00:00.000' --        Дата фиксации изменений в записи
    ,caomiID uniqueidentifier DEFAULT '00000000-0000-0000-0000-000000000000' --Уникальный идентификатор направления в шине caomi-fhir
    ,statusID int DEFAULT 0 --Статус записи (0 - создана(обновлена), 1 - отослана в шинуcaomi_fhir
    ,DateStatus        datetime DEFAULT '1900-01-01T00:00:00.000' --Дата ответа из шины caomi-fhir
    ,HttpStatus        int        DEFAULT 0 -- Статус, который прислала шина caomi-fhir
    ,errorCode varchar(100) DEFAULT '' --Код ошибки, который прислала шина caomi-fhir
    ,errorText varchar(200) DEFAULT '' --Текст ошибки, который прислала шина caomi-fhir
);

--описание таблицы
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', default, default))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Таблица хранения информации о направлениях на исследование для интеграции с шины caomi-fhir(ЦАМИ)'
                                                          ,@level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'lbrLaboratoryResearchID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Уникальный идентификатор направления', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'lbrLaboratoryResearchID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'DateCreate'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Дата cоздания новой записи', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'DateCreate';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'DateChange'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Дата фиксации изменений в записи', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'DateChange';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'caomiID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Уникальный идентификатор оборудования в шине caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'caomiID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'statusID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Статус записи (0 - создана(обновлена), 1 - отослана в шинуcaomi_fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'statusID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'DateStatus'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Дата ответа из шины caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'DateStatus';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'HttpStatus'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Статус, который прислала шина caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'HttpStatus';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'errorCode'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Код ошибки, который прислала шина caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'errorCode';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiReferral', 'COLUMN', 'errorText'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Текст ошибки, который прислала шина caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'errorText';