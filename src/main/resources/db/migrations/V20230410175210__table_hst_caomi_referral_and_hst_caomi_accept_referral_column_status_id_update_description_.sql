--обновление description для statusID в hst_caomiReferral
exec sp_updateextendedproperty MS_Description
	, 'Статус записи (0 - Направление создано, 1 - Ожидание согласования, 2 - Получение результата согласования, 3 - Получение результата исследования)'
	,@level0type = N'Schema', @level0name = dbo
    ,@level1type = N'Table',  @level1name = hst_caomiReferral
    ,@level2type = N'Column', @level2name = statusID;

--обновление description для statusID в hst_caomiAcceptReferral
exec sp_updateextendedproperty MS_Description
	, 'Статус записи (0 - создана запись, 1 - поставлено в расписание, 2 - информация отослана в шину caomi_fhir, 5 - Согласовано, отправлено в шину, 6 - Не согласовано, отправлено в шину)'
	,@level0type = N'Schema', @level0name = dbo
    ,@level1type = N'Table',  @level1name = hst_caomiAcceptReferral
    ,@level2type = N'Column', @level2name = statusID; 