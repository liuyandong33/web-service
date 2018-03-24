package build.dream.platform.services;

import build.dream.common.api.ApiRest;
import build.dream.common.saas.domains.SystemUser;
import build.dream.common.saas.domains.Tenant;
import build.dream.common.saas.domains.TenantSecretKey;
import build.dream.common.utils.CommonUtils;
import build.dream.common.utils.ProxyUtils;
import build.dream.common.utils.SearchModel;
import build.dream.common.utils.UpdateModel;
import build.dream.platform.constants.Constants;
import build.dream.platform.mappers.*;
import build.dream.platform.models.user.BatchDeleteUserModel;
import build.dream.platform.models.user.BatchGetUsersModel;
import build.dream.platform.models.user.ObtainAllPrivilegesModel;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private SystemUserMapper systemUserMapper;
    @Autowired
    private TenantMapper tenantMapper;
    @Autowired
    private BackgroundPrivilegeMapper backgroundPrivilegeMapper;
    @Autowired
    private AppPrivilegeMapper appPrivilegeMapper;
    @Autowired
    private PosPrivilegeMapper posPrivilegeMapper;
    @Autowired
    private TenantSecretKeyMapper tenantSecretKeyMapper;
    @Autowired
    private UniversalMapper universalMapper;

    /**
     * 获取用户信息
     *
     * @param loginName
     * @return
     * @throws IOException
     */
    @Transactional(readOnly = true)
    public ApiRest obtainUserInfo(String loginName) throws IOException {
        SystemUser systemUser = systemUserMapper.findByLoginNameOrEmailOrMobile(loginName);
        Validate.notNull(systemUser, "用户不存在！");

        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("id", Constants.SQL_OPERATION_SYMBOL_EQUALS, systemUser.getTenantId());
        Tenant tenant = tenantMapper.find(searchModel);
        Validate.notNull(tenant, "商户不存在！");

        SearchModel tenantSecretKeySearchModel = new SearchModel(true);
        tenantSecretKeySearchModel.addSearchCondition("tenant_id", Constants.SQL_OPERATION_SYMBOL_EQUALS, tenant.getId());
        TenantSecretKey tenantSecretKey = tenantSecretKeyMapper.find(tenantSecretKeySearchModel);
        Validate.notNull(tenantSecretKey, "未检索到商户秘钥！");

        Map<String, String> obtainBranchInfoRequestParameters = new HashMap<String, String>();
        obtainBranchInfoRequestParameters.put("tenantId", tenant.getId().toString());
        obtainBranchInfoRequestParameters.put("userId", systemUser.getId().toString());
        ApiRest obtainBranchInfoApiRest = ProxyUtils.doGetWithRequestParameters(tenant.getPartitionCode(), CommonUtils.getServiceName(tenant.getBusiness()), "branch", "obtainBranchInfo", obtainBranchInfoRequestParameters);
        Validate.isTrue(obtainBranchInfoApiRest.isSuccessful(), obtainBranchInfoApiRest.getError());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("user", systemUser);
        data.put("tenant", tenant);
        data.put("branch", obtainBranchInfoApiRest.getData());
        data.put("tenantSecretKey", tenantSecretKey);
        ApiRest apiRest = new ApiRest();
        apiRest.setData(data);
        apiRest.setSuccessful(true);
        return apiRest;
    }

    /**
     * 批量获取用户信息
     *
     * @param batchGetUsersModel
     * @return
     */
    @Transactional(readOnly = true)
    public ApiRest batchObtainUser(BatchGetUsersModel batchGetUsersModel) {
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("id", "IN", batchGetUsersModel.getUserIds());
        List<SystemUser> systemUsers = systemUserMapper.findAll(searchModel);
        return new ApiRest(systemUsers, "批量获取用户信息成功！");
    }

    /**
     * 获取用户所有权限
     *
     * @param obtainAllPrivilegesModel
     * @return
     */
    @Transactional(readOnly = true)
    public ApiRest obtainAllPrivileges(ObtainAllPrivilegesModel obtainAllPrivilegesModel) {
        Object data = null;
        if (Constants.PRIVILEGE_TYPE_BACKGROUND.equals(obtainAllPrivilegesModel.getType())) {
            data = backgroundPrivilegeMapper.findAllBackgroundPrivileges(obtainAllPrivilegesModel.getUserId());
        } else if (Constants.PRIVILEGE_TYPE_APP.equals(obtainAllPrivilegesModel.getType())) {
            data = appPrivilegeMapper.findAllAppPrivileges(obtainAllPrivilegesModel.getUserId());
        } else if (Constants.PRIVILEGE_TYPE_POS.equals(obtainAllPrivilegesModel.getType())) {
            data = posPrivilegeMapper.findAllPosPrivileges(obtainAllPrivilegesModel.getUserId());
        }
        ApiRest apiRest = new ApiRest();
        apiRest.setData(data);
        apiRest.setMessage("查询权限成功！");
        apiRest.setSuccessful(true);
        return apiRest;
    }

    /**
     * 批量删除用户
     *
     * @param batchDeleteUserModel
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiRest batchDeleteUser(BatchDeleteUserModel batchDeleteUserModel) {
        UpdateModel updateModel = new UpdateModel(true);
        updateModel.setTableName("system_user");
        updateModel.addContentValue("deleted", 1);
        updateModel.addContentValue("last_update_user_id", batchDeleteUserModel.getUserId());
        updateModel.addContentValue("last_update_remark", "删除用户信息！");
        updateModel.addSearchCondition("id", Constants.SQL_OPERATION_SYMBOL_IN, batchDeleteUserModel.getUserIds());
        universalMapper.universalUpdate(updateModel);

        ApiRest apiRest = new ApiRest();
        apiRest.setMessage("批量删除用户成功！");
        apiRest.setSuccessful(true);
        return apiRest;
    }
}
