GRANT SELECT ON SCHEMA :: [dbo] TO caomi_integration_srv;
GRANT INSERT, UPDATE ON hst_caomiDevice to caomi_integration_srv;
GRANT INSERT, UPDATE ON hst_caomiReferral to caomi_integration_srv;
GRANT INSERT, UPDATE ON hst_caomiAcceptReferral to caomi_integration_srv;
GRANT INSERT, UPDATE ON lbr_Research to caomi_integration_srv;
GRANT INSERT, UPDATE ON lbr_LaboratoryResearch to caomi_integration_srv;