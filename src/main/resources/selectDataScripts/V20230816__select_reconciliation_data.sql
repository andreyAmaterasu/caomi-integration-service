select
    lr.AccessionNumber as idReferral,
    iif(DoctorTimeTableID > 0, 1, 0)  as agreedReferral,
    iif(DoctorTimeTableID > 0, '', 'Нет свободных слотов в расписании') as rejectionReason,
    concat(convert(date,timetab.date,103),' ',left(convert(time ,timetab.Begin_Time,104),8)) as occurencePeriodStart,
    concat(convert(date,timetab.date,103),' ',left(convert(time,timetab.End_Time,104),8)) as occurencePeriodEnd,
    hcar.performerDeviceID as deviceMisId,
    case when he.equipmentid is not null then 
        case when  lpu.rf_MainLPUID =0  then lpu.Lic else lpu2.Lic end end as owner,
    he.Name  as deviceName
from lbr_LaboratoryResearch lr
         join lbr_Research r  on lr.guid = r.rf_LaboratoryResearchGUID
         join lbr_ResearchType rt  on r.rf_ResearchTypeUGUID = rt.UGUID
         join hlt_DocPRVD dp  on lr.rf_DocPRVDID=dp.DocPRVDID
         join oms_lpu lpu on lr.rf_LPUID= lpu.LPUID
         left join oms_lpu lpu2 on lpu2.LPUID = lpu.rf_MainLPUID
         join hlt_ResourceType rst on dp.rf_ResourceTypeID = rst.ResourceTypeID
         join hst_caomiAcceptReferral hcar on hcar.lbrLaboratoryResearchID  = lr.LaboratoryResearchID
         left join hlt_Equipment he on he.GUID = hcar.performerDeviceID
    cross apply (
 select
  dt.DoctorTimeTableID,
  dt.date,
  dt.Begin_Time,
  dt.End_Time , dt.rf_DocPRVDID as dd
 from hlt_DoctorTimeTable dt
 join hlt_DoctorVisitTable dv  on dv.rf_DoctorTimeTableID = dt.DoctorTimeTableID
 join hlt_DocPrvdNomService hdpns on hdpns.rf_DocPRVDID = dt.rf_DocPRVDID
 where  rt.rf_kl_NomServiceID  = hdpns.rf_kl_NomServiceID
   and cast(dt.Date as date) >= cast(lr.Date_direction as date)
   and lr.rf_MKABID = dv.rf_MKABID
) timetab
       
where lr.LaboratoryResearchID = :laboratory_research_id