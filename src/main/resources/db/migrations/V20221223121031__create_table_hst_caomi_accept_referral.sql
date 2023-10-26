-------------------------------
-- Author:                Юлдашева Лейла
-- Create date: 15.12.2022
-- Description:        Таблица хранения информации о направлениях на исследование для интеграции с шины caomi-fhir(ЦАМИ)
-- Region:                ПК
-------------------------------
IF OBJECT_ID('hst_caomiAcceptReferral ', 'U') IS NOT NULL DROP TABLE hst_caomiAcceptReferral ;
CREATE TABLE hst_caomiAcceptReferral  (caomiAcceptReferralID int IDENTITY(1, 1) PRIMARY KEY CLUSTERED
    ,caomiID uniqueidentifier DEFAULT '00000000-0000-0000-0000-000000000000' --Уникальный идентификатор направления в шине caomi-fhir
    ,dateCreate datetime DEFAULT '1900-01-01T00:00:00.000' --        Дата cоздания новой записи
    ,recipientMoOID varchar(100) DEFAULT '' -- оид МО-услоголучателя
    ,performerMoOID varchar(100) DEFAULT '' -- оид МО-услугодателя
    ,MKABID int DEFAULT 0 -- Идентификатор МКАБ (пациента)
    ,performerDeviceID varchar(100) default '' --        Идентификатор оборудования
    ,lbrLaboratoryResearchID int DEFAULT 0 --Уникальный идентификатор направления
    ,statusID int DEFAULT 0 --Статус записи (0 - создана запись, 1 - поставлено в расписание, 2 - информация отослана в шину caomi_fhir)
    ,errorCode varchar(100) DEFAULT '' -- Код ошибки для ответа шине caomi-fhir
    ,errorText varchar(200) DEFAULT '' -- Текст ошибки для ответа шине caomi-fhir
);

--описание таблицы
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral ', default, default))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Таблица хранения информации о направлениях на исследование для интеграции с шины caomi-fhir(ЦАМИ)'
                                                          ,@level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'caomiID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Уникальный идентификатор направления в шине caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'caomiID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'dateCreate'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Дата cоздания новой записи', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'dateCreate';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'recipientMoOID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'оид МО-услоголучателя', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'recipientMoOID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'performerMoOID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'оид МО-услугодателя', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'performerMoOID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'MKABID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Идентификатор МКАБ (пациента)', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'MKABID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'performerDeviceID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Идентификатор оборудования', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'performerDeviceID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'lbrLaboratoryResearchID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Уникальный идентификатор направления', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'lbrLaboratoryResearchID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'statusID'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Статус записи (0 - создана запись, 1 - поставлено в расписание, 2 - информация отослана в шину caomi_fhir)', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'statusID';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'errorCode'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Код ошибки для ответа шине caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'errorCode';
IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'errorText'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Текст ошибки для ответа шине caomi-fhir', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'errorText';