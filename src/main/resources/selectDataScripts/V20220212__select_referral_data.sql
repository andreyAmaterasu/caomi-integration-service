select l.lic                 as moOid
     , lr.Number         as referralNumber
     , lr.DOCT_PCOD as practitioner
     , dp.docprvdid as practitionerRole
from lbr_LaboratoryResearch lr
         join oms_LPU                                 l  on lr.rf_LPUSenderID=l.LPUID
         join hlt_docPRVD                         dp on lr.rf_docprvdid=dp.docprvdid
where lr.LaboratoryResearchID = ?