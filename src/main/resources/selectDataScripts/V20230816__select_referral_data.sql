select case when l.rf_MainLPUID = 0 then l.Lic 
			    else STUFF(SUBSTRING(l.Lic, 1, charindex('.0', l.Lic) ), len(SUBSTRING(l.Lic, 1, charindex('.0', l.Lic) )), 1, '') 
					end as moOid 
					, lr.Number 	as referralNumber
					, rd.personDoctorID as practitioner
					, rd.specialistDoctorID as practitionerRole
					 from lbr_LaboratoryResearch lr
		join oms_LPU l   on l.LPUID = lr.rf_LPUSenderID
		join hlt_docPRVD dp  on dp.DocPRVDID = lr.rf_DocPRVDID
		join hst_rimsDoctor rd  on rd.PRVDGUID = dp.GUID 
		where lr.LaboratoryResearchID = :labResearchId		