select CONVERT(varchar, r.ResearchID) as serviceRequest_serviceId,
                                 ds.directionstatusid as serviceRequest_serviceStatusCode,
                                 lrt.code as serviceRequest_serviceIntentCode,
                                 case
                                     when lr.Comment like '%планово%'
                                         then 1
                                     when lr.comment like '%экстренно%'
                                         then 2
                                     else 3
                                     end as serviceRequest_servicePriorityCode,
                                 ns.code as serviceRequest_serviceCode,
                                 lr.DateCreate as serviceRequest_authoredOn,
                                 cast(cast(lr.Date_Direction + 1 as date) as datetime) as serviceRequest_desiredPeriod_start,
                                 cast(cast(DATEADD(month, 1, lr.Date_Direction) as date) as datetime) as serviceRequest_desiredPeriod_end,
                                 p.C_PRVS as serviceRequest_performerTypeCode,
                                 NULL as serviceRequest_performerDeviceId,
                                 case when l3.rf_MainLPUID = 0 then l3.Lic
                                      else STUFF(SUBSTRING (l3.Lic, 1, charindex('.0', l3.Lic) ), len(SUBSTRING(l3.Lic, 1, charindex('.0', l3.Lic) )), 1, '')
                                     end as serviceRequest_performerMoOid,
                                 mkb.ds as serviceRequest_reasonCode,
                                 cc.CodeNSI
                          from lbr_LaboratoryResearch lr
                                   join hlt_docPRVD dp
                                        on lr.rf_docprvdid = dp.docprvdid
                                   join lbr_research r
                                        on r.rf_LaboratoryResearchGUID = lr.GUID
                                   join lbr_ResearchType rt
                                        on r.rf_ResearchTypeUGUID = rt.UGUID
                                   join oms_ServiceMedical sm
                                        on rt.rf_ServiceMedicalID = sm.ServiceMedicalID
                                   join oms_kl_DepartmentProfile dpp
                                        on dpp.kl_DepartmentProfileID = sm.rf_kl_DepartmentProfileID
                              outer apply (select cv.CodeNSI
                                          from hl7_CatalogValue cv
                                          join hl7_Catalog c
                                            on c.UGUID = cv.rf_CatalogGUID
                                         where c.OID = '1.2.643.5.1.13.13.11.1119'
                                           and c.isActual = 1
                                           and cv.InternalCode = dpp.code) cc
                            join hlt_DirectionStatus ds
                          on lr.rf_directionstatusid = ds.directionstatusid
                              join lbr_labresearchtarget lrt
                              on lr.rf_LabResearchTargetID = lrt.LabResearchTargetID
                              join oms_kl_NomService ns
                              on rt.rf_kl_NomServiceID = ns.kl_NomServiceID
                              join oms_prvs p
                              on dp.rf_PRVSID = p.PRVSID
                              join oms_lpu l3
                              on lr.rf_LPUID = l3.LPUID
                              join oms_MKB mkb
                              on lr.rf_mkbid = mkb.mkbid
                          where ds.code = 'WriteOff'
                            and lr.LaboratoryResearchID = :labResearchId