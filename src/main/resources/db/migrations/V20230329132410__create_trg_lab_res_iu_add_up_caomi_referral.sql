IF OBJECT_ID ('TRG_LaboratoryResearch_IU_add_up_caomiReferral', 'TR') IS NOT NULL DROP TRIGGER dbo.TRG_LaboratoryResearch_IU_add_up_caomiReferral;
GO
CREATE OR ALTER TRIGGER dbo.TRG_LaboratoryResearch_IU_add_up_caomiReferral
ON dbo.lbr_LaboratoryResearch
AFTER INSERT, UPDATE
                                  AS
BEGIN
IF UPDATE(LaboratoryResearchID)
   INSERT INTO hst_caomiReferral (lbrLaboratoryResearchID, DateCreate, statusID)
SELECT     i.LaboratoryResearchID as [lbrLaboratoryResearchID],
        GETDATE() as [DateCreate],
        0 as [statusID]
FROM inserted i
    LEFT JOIN hst_caomiReferral cd (NOLOCK) ON cd.lbrLaboratoryResearchID = i.LaboratoryResearchID
WHERE cd.lbrLaboratoryResearchID IS NULL;
/*ELSE
UPDATE up
SET lbrLaboratoryResearchID = i.LaboratoryResearchID
   ,DateChange = GETDATE()
   ,statusID = 0
FROM hst_caomiReferral up (NOLOCK)
join inserted i  on up.lbrLaboratoryResearchID = i.LaboratoryResearchID
WHERE up.lbrLaboratoryResearchID <> 0; */
END;