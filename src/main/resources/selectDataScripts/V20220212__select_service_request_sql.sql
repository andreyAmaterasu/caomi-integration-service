select           rt.code                                                                                 as serviceRequest_serviceId
     , ds.directionstatusid                                                         as serviceRequest_serviceStatusCode
     , lrt.code                                                                                 as serviceRequest_serviceIntentCode
     , case when lr.Comment like '%планово%' then 1
            when lr.comment like '%экстренно%' then 2
            else  3 end                                                                 as serviceRequest_servicePriorityCode
     , ns.code                                                                                 as serviceRequest_serviceCode
     , lr.Date_Direction                                                         as serviceRequest_authoredOn
     , cast(cast(lr.Date_Direction+1 as date) as datetime) as serviceRequest_desiredPeriod_start
     , cast(cast(DATEADD(month, 1, lr.Date_Direction) as date) as datetime) as serviceRequest_desiredPeriod_end
     , p.C_PRVS                                                                                 as serviceRequest_performerTypeCode
     , NULL                                                                                         as serviceRequest_performerDeviceId
     , l3.lic                                                                                 as serviceRequest_performerMoOid
     , mkb.ds                                                                                 as serviceRequest_reasonCode
from lbr_LaboratoryResearch         lr
         join hlt_docPRVD                                 dp         on lr.rf_docprvdid=dp.docprvdid
         join lbr_research                                 r         on r.rf_LaboratoryResearchGUID=lr.GUID
         join lbr_researchtype                         rt         on r.rf_ResearchTypeUGUID=rt.UGUID
         join hlt_DirectionStatus                 ds         on lr.rf_directionstatusid=ds.directionstatusid
         join lbr_labresearchtarget                 lrt on lr.rf_LabResearchTargetID=lrt.LabResearchTargetID
         join oms_kl_NomService                         ns         on rt.rf_kl_NomServiceID=ns.kl_NomServiceID
         join oms_prvs                                         p         on dp.rf_PRVSID=p.PRVSID
         join oms_lpu                                         l3         on lr.rf_LPUID=l3.LPUID
         join oms_MKB                                         mkb on lr.rf_mkbid=mkb.mkbid
where ds.code='WriteOff'
  and lr.LaboratoryResearchID = ?
  and (lr.Comment like '%планово%' or lr.Comment like '%экстренно%' or lr.Comment like '%неотложно%')