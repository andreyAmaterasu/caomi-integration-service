select top 1 dtt.Date as date, dtt.Begin_Time as beginTime,
                          dtt.ExternalScheduleID as externalScheduleId, dtt.UGUID as uguid,
                          dtt.DoctorTimeTableID as doctorTimeTableId, lr.rf_MKABID as rfMkabId,
                          eq.GUID as equipmentGuid, lr.Number as number
from lbr_LaboratoryResearch lr
    join lbr_Research lr2 on lr2.rf_LaboratoryResearchGUID = lr.GUID
    join lbr_ResearchType rt on lr2.rf_ResearchTypeUGUID = rt.UGUID
    join oms_kl_NomService ns on rt.rf_kl_NomServiceID = ns.kl_NomServiceID
    join hlt_DocPrvdNomService pn on ns.kl_NomServiceID = pn.rf_kl_NomServiceID
    join hlt_DocPRVD pr on pn.rf_DocPRVDID = pr.DocPRVDID
    join hlt_DoctorTimeTable dtt on dtt.rf_DocPRVDID = pn.rf_DocPRVDID and dtt.LastStubNum = 0
    and dtt.Begin_Time between getdate() and getdate() + 30
    join hlt_Equipment eq on pr.rf_EquipmentID = eq.EquipmentID and pr.rf_EquipmentID <> 0
where lr.LaboratoryResearchID = :laboratory_research_id
group by eq.GUID, eq.Code, eq.Name, dtt.DoctorTimeTableID, Begin_Time, dtt.Date, dtt.ExternalScheduleID,
    dtt.UGUID, lr.LaboratoryResearchID, lr.rf_MKABID, eq.equipmentid, eq.InventoryNumber, lr.Number
order by eq.GUID, dtt.Begin_Time