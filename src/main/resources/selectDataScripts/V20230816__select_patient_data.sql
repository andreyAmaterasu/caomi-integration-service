select   m.UGUID  						as patient_patientId
				, s.code		 				as patient_gender
				, convert(date,m.DATE_BD,103) 	as patient_birthDate
				, case when l2.rf_MainLPUID = 0 then l2.Lic 
			  		 else STUFF(SUBSTRING(l2.Lic, 1, charindex('.0', l2.Lic) ), len(SUBSTRING(l2.Lic, 1, charindex('.0', l2.Lic) )), 1, '') 
			   		end as patient_generalPractitionerMoOid
				, case when t.IDDOC=3 then 2 
					   when t.IDDOC=2 then 4 
					   else 1 end 				as coverage_policyTypeCode
	  		  , case when t.IDDOC=1 then pm.s_pol+':'+pm.n_pol 
	  				   else pm.n_pol end 		as coverage_policyNumber
	   		 , pm.isActive 					as coverage_policyStatus
	   		 , pm.DatePolBegin 				as coverage_validityPeriod_start
	   		 , pm.DatePolEnd 				as coverage_validityPeriod_end
	   		 , smo.cod 						as coverage_medicalInsuranceOrganizationCode
		from lbr_LaboratoryResearch lr
		join hlt_MKAB 				m 	on lr.rf_mkabid=m.mkabid
		join oms_kl_sex 			s 	on m.rf_kl_SexID=s.kl_SexID
		join oms_LPU 				l2 	on m.rf_LPUID=l2.lpuid
		join oms_kl_TipOMS 			t 	on m.rf_kl_TipOMSID=t.kl_TipOMSID
		join hlt_PolisMKAB 			pm 	on pm.rf_MKABID=m.mkabid and pm.rf_kl_profittypeid=(select kl_profittypeid from oms_kl_profittype where code=1)
		left join oms_SMO 				smo on pm.rf_SMOID=smo.SMOID
		where lr.LaboratoryResearchID = :labResearchId and m.mkabid>0 and t.IDDOC in (1,2,3)