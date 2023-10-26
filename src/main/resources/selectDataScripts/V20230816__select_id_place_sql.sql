select
                   TOP 1 case
                   when cv2.CodeNSI <>'' then cv2.CodeNSI
                   when cv1.CodeNSI <> '' then cv1.CodeNSI
                   when cv3.CodeNSI <> '' then cv3.CodeNSI
                   else null end as idPlace
                   from hlt_DoctorTimeTable t
                   inner join hlt_DocPRVD prvd on prvd.DocPRVDID = t.rf_DocPRVDID
                   inner join oms_Department d on d.DepartmentID = prvd.rf_DepartmentID
                   inner join oms_LPU lpu on lpu.LPUID = d.rf_LPUID
                   left join oms_LPU lpu2 on lpu.rf_MainLPUID = lpu2.LPUID
                   left join hl7_catalogvalue cv1 on cv1.OID = lpu.Lic AND cv1.rf_CatalogGUID = 'BBC1D494-A604-40CA-8B8C-2A00538028EF'
                   left join hl7_catalogvalue cv2 on cv2.OID = d.OID AND cv2.rf_CatalogGUID = 'BBC1D494-A604-40CA-8B8C-2A00538028EF'
                   left join hl7_catalogvalue cv3 on cv3.OID = lpu2.Lic AND cv3.rf_CatalogGUID = 'BBC1D494-A604-40CA-8B8C-2A00538028EF'
                   where t.DoctorTimeTableID = :doctor_time_table_id