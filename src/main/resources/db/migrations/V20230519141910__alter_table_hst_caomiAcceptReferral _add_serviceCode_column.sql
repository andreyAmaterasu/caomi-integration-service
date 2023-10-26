-- Author:        Reshetov Kirill
-- Create date:   19-05-2023
-- Description:   скрипт на добавление поля serviceCode в таблицу hst_caomiAcceptReferral и их комментарии
-- Task           #119847
-- Region:        ПК
IF COL_LENGTH ('dbo.hst_caomiAcceptReferral','serviceCode') IS NULL
BEGIN
ALTER TABLE dbo.hst_caomiAcceptReferral
    ADD serviceCode varchar(50) DEFAULT NULL NULL
EXEC sys.sp_addextendedproperty 'MS_Description', N'Код мед.услуги', 'schema', N'dbo', 'table', N'hst_caomiAcceptReferral', 'column', N'serviceCode';
END;

IF NOT EXISTS (SELECT value FROM fn_listextendedproperty (NULL, 'schema', 'dbo', 'table', 'hst_caomiAcceptReferral', 'COLUMN', 'serviceCode'))
EXEC sys.sp_addextendedproperty @name = 'MS_Description'
                                                          ,@value = 'Код мед.услуги', @level0type = 'SCHEMA', @level0name = 'dbo', @level1type = 'TABLE'
                                                          ,@level1name = 'hst_caomiAcceptReferral', @level2type = 'COLUMN'
                                                          ,@level2name = 'serviceCode';
