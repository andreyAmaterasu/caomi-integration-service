select distinct
    cd.caomiDeviceID
              ,e.GUID
              ,e.equipmentid + e.InventoryNumber as deviceMisId
              , dp.ShownInSchedule as is_active_device
              , e.Name as deviceName
              , l.lic as owner
              , ns.CODE as serviceCode
              , ns.NAME as serviceName
              , 1 as is_active_service
from hlt_Equipment e
         join hst_caomiDevice cd on e.GUID = cd.hltEquipmentGUID
         join hlt_DocPRVD dp on e.EquipmentID = dp.rf_EquipmentID
         left join hlt_DocPrvdNomService dpn on dp.DocPRVDID = dpn.rf_DocPRVDID
         left join oms_kl_NomService ns on dpn.rf_kl_NomServiceID = ns.kl_NomServiceID
         join hlt_resourceType rt on dp.rf_resourcetypeid=rt.resourcetypeid and rt.code='3'
         join oms_department d on e.rf_departmentid=d.departmentid
         join oms_LPU l on d.rf_LPUID=l.LPUID
    and cd.statusID = 0
order by cd.caomiDeviceID
OFFSET :offset ROWS
FETCH NEXT :limit ROWS ONLY