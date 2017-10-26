package build.dream.platform.services;

import build.dream.common.api.ApiRest;
import build.dream.common.saas.domains.AppAuthority;
import build.dream.common.saas.domains.SystemUser;
import build.dream.common.saas.domains.Tenant;
import build.dream.common.utils.*;
import build.dream.platform.constants.Constants;
import build.dream.platform.mappers.AppAuthorityMapper;
import build.dream.platform.mappers.SystemUserMapper;
import build.dream.platform.mappers.TenantMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
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
    private AppAuthorityMapper appAuthorityMapper;

    @Transactional(readOnly = true)
    public ApiRest obtainUserInfo(String loginName) {
        SystemUser systemUser = systemUserMapper.findByLoginNameOrEmailOrMobile(loginName);
        Validate.notNull(systemUser, "用户不存在！");

        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("id", Constants.SQL_OPERATION_SYMBOL_EQUALS, systemUser.getTenantId());
        Tenant tenant = tenantMapper.find(searchModel);
        Validate.notNull(tenant, "商户不存在！");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("user", systemUser);
        data.put("tenant", tenant);
        ApiRest apiRest = new ApiRest();
        apiRest.setData(data);
        apiRest.setSuccessful(true);
        return apiRest;
    }

    @Transactional(readOnly = true)
    public ApiRest findAllUsers(Map<String, String> parameters) {
        String userIds = parameters.get("userIds");
        SearchModel searchModel = new SearchModel(true);
        ApiRest apiRest = null;
        if (StringUtils.isNotBlank(userIds)) {
            String[] userIdArray = StringUtils.split(userIds, ",");
            List<BigInteger> bigIntegerUserIds = new ArrayList<BigInteger>();
            for (String userId : userIdArray) {
                bigIntegerUserIds.add(BigInteger.valueOf(Long.valueOf(userId)));
            }
            searchModel.addSearchCondition("id", "IN", bigIntegerUserIds);
            List<SystemUser> systemUsers = systemUserMapper.findAll(searchModel);
            apiRest = new ApiRest(systemUsers, "查询成功！");
        }
        return apiRest;
    }

    @Transactional(readOnly = true)
    public ApiRest findAllAppAuthorities(BigInteger userId) throws IOException {
        List<AppAuthority> appAuthorities = appAuthorityMapper.findAllAppAuthorities(userId);
        ApiRest apiRest = new ApiRest();
        apiRest.setData(appAuthorities);
        apiRest.setMessage("查询APP权限成功！");
        apiRest.setSuccessful(true);
        return apiRest;
    }
}
