-- Author:        Maslov Ivan
-- Create date:   16-03-2023
-- Description:   скрипт на добавление полей в таблице hst_caomiReferral и их комментари
-- Task           #113668
-- Region:        ПК
IF COL_LENGTH ('dbo.hst_caomiReferral','agreedReferral') IS NULL
BEGIN
ALTER TABLE dbo.hst_caomiReferral
    ADD agreedReferral bit DEFAULT NULL NULL
EXEC sys.sp_addextendedproperty 'MS_Description', N'Направление согласовано (да/нет)', 'schema', N'dbo', 'table', N'hst_caomiReferral', 'column', N'agreedReferral';
END;
IF COL_LENGTH ('dbo.hst_caomiReferral','rejectionReason') IS NULL
BEGIN
ALTER TABLE dbo.hst_caomiReferral
    ADD rejectionReason varchar(200) DEFAULT '' NULL
EXEC sys.sp_addextendedproperty 'MS_Description', N'Причина отказа (для несогласованных направлений)', 'schema', N'dbo', 'table', N'hst_caomiReferral', 'column', N'rejectionReason';
END;
IF COL_LENGTH ('dbo.hst_caomiReferral','dateStart') IS NULL
BEGIN
ALTER TABLE dbo.hst_caomiReferral
    ADD dateStart datetime DEFAULT '1/1/1900' NULL
EXEC sys.sp_addextendedproperty 'MS_Description', N'Дата начала согласованного приема на инструментальное исследование', 'schema', N'dbo', 'table', N'hst_caomiReferral', 'column', N'dateStart';
END;
IF COL_LENGTH ('dbo.hst_caomiReferral','dateEnd') IS NULL
BEGIN
ALTER TABLE dbo.hst_caomiReferral
    ADD dateEnd datetime DEFAULT '1/1/1900' NULL
EXEC sys.sp_addextendedproperty 'MS_Description', N'Дата окончания согласованного приема на инструментальное исследование', 'schema', N'dbo', 'table', N'hst_caomiReferral', 'column', N'dateEnd';
END;            