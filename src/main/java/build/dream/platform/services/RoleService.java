package build.dream.platform.services;

import build.dream.common.api.ApiRest;
import build.dream.common.domains.saas.AppRole;
import build.dream.common.domains.saas.BackgroundRole;
import build.dream.common.domains.saas.PosRole;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.PagedSearchModel;
import build.dream.common.utils.SearchModel;
import build.dream.platform.constants.Constants;
import build.dream.platform.mappers.AppRoleMapper;
import build.dream.platform.mappers.BackgroundRoleMapper;
import build.dream.platform.mappers.PosRoleMapper;
import build.dream.platform.models.role.ListRolePrivilegesModel;
import build.dream.platform.models.role.ListRolesModel;
import build.dream.platform.models.role.SaveRolePrivilegesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleService {
    @Autowired
    private BackgroundRoleMapper backgroundRoleMapper;
    @Autowired
    private AppRoleMapper appRoleMapper;
    @Autowired
    private PosRoleMapper posRoleMapper;

    @Transactional(readOnly = true)
    public ApiRest listRoles(ListRolesModel listRolesModel) {
        SearchModel countSearchModel = new SearchModel(true);
        countSearchModel.addSearchCondition("tenant_id", Constants.SQL_OPERATION_SYMBOL_EQUAL, listRolesModel.getTenantId());

        PagedSearchModel pagedSearchModel = new PagedSearchModel(true);
        pagedSearchModel.addSearchCondition("tenant_id", Constants.SQL_OPERATION_SYMBOL_EQUAL, listRolesModel.getTenantId());
        pagedSearchModel.setPage(listRolesModel.getPage());
        pagedSearchModel.setRows(listRolesModel.getRows());

        Map<String, Object> data = new HashMap<String, Object>();
        if (Constants.PRIVILEGE_TYPE_BACKGROUND.equals(listRolesModel.getType())) {
            long total = DatabaseHelper.count(BackgroundRole.class, countSearchModel);
            List<BackgroundRole> backgroundRoles = new ArrayList<BackgroundRole>();
            if (total > 0) {
                backgroundRoles = DatabaseHelper.findAllPaged(BackgroundRole.class, pagedSearchModel);
            }
            data.put("total", total);
            data.put("rows", backgroundRoles);
        } else if (Constants.PRIVILEGE_TYPE_APP.equals(listRolesModel.getType())) {
            long total = DatabaseHelper.count(AppRole.class, countSearchModel);
            List<AppRole> appRoles = new ArrayList<AppRole>();
            if (total > 0) {
                appRoles = DatabaseHelper.findAllPaged(AppRole.class, pagedSearchModel);
            }
            data.put("total", total);
            data.put("rows", appRoles);
        } else if (Constants.PRIVILEGE_TYPE_POS.equals(listRolesModel.getType())) {
            long total = DatabaseHelper.count(PosRole.class, countSearchModel);
            List<PosRole> posRoles = new ArrayList<PosRole>();
            if (total > 0) {
                posRoles = DatabaseHelper.findAllPaged(PosRole.class, pagedSearchModel);
            }
            data.put("total", total);
            data.put("rows", posRoles);
        }
        return ApiRest.builder().data(data).message("查询权限列表成功！").successful(true).build();
    }

    @Transactional(readOnly = true)
    public ApiRest listRolePrivileges(ListRolePrivilegesModel listRolePrivilegesModel) {
        Object data = null;
        if (Constants.ROLE_TYPE_BACKGROUND.equals(listRolePrivilegesModel.getType())) {
            data = backgroundRoleMapper.listRolePrivileges(listRolePrivilegesModel.getRoleId());
        } else if (Constants.ROLE_TYPE_APP.equals(listRolePrivilegesModel.getType())) {
            data = backgroundRoleMapper.listRolePrivileges(listRolePrivilegesModel.getRoleId());
        } else if (Constants.ROLE_TYPE_POS.equals(listRolePrivilegesModel.getType())) {
            data = backgroundRoleMapper.listRolePrivileges(listRolePrivilegesModel.getRoleId());
        }
        return ApiRest.builder().data(data).message("查询角色权限列表成功！").successful(true).build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiRest saveRolePrivileges(SaveRolePrivilegesModel saveRolePrivilegesModel) {
        if (Constants.PRIVILEGE_TYPE_BACKGROUND.equals(saveRolePrivilegesModel.getType())) {
            backgroundRoleMapper.deleteRolePrivileges(saveRolePrivilegesModel.getRoleId());
            backgroundRoleMapper.saveRolePrivileges(saveRolePrivilegesModel.getRoleId(), saveRolePrivilegesModel.getPrivilegeIds());
        } else if (Constants.PRIVILEGE_TYPE_APP.equals(saveRolePrivilegesModel.getType())) {
            appRoleMapper.deleteRolePrivileges(saveRolePrivilegesModel.getRoleId());
            appRoleMapper.saveRolePrivileges(saveRolePrivilegesModel.getRoleId(), saveRolePrivilegesModel.getPrivilegeIds());
        } else if (Constants.PRIVILEGE_TYPE_POS.equals(saveRolePrivilegesModel.getType())) {
            posRoleMapper.deleteRolePrivileges(saveRolePrivilegesModel.getRoleId());
            posRoleMapper.saveRolePrivileges(saveRolePrivilegesModel.getRoleId(), saveRolePrivilegesModel.getPrivilegeIds());
        }
        return ApiRest.builder().message("保存角色权限成功！").successful(true).build();
    }
}
