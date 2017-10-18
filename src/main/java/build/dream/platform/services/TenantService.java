package build.dream.platform.services;

import build.dream.common.api.ApiRest;
import build.dream.common.saas.domains.Tenant;
import build.dream.common.utils.SearchModel;
import build.dream.platform.constants.Constants;
import build.dream.platform.mappers.TenantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
public class TenantService {
    @Autowired
    private TenantMapper tenantMapper;

    @Transactional(readOnly = true)
    public ApiRest findTenantInfoById(BigInteger tenantId) {
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("id", Constants.SQL_OPERATION_SYMBOL_EQUALS, tenantId);
        Tenant tenant = tenantMapper.find(searchModel);

        ApiRest apiRest = new ApiRest();
        apiRest.setClassName(Tenant.class.getName());
        apiRest.setData(tenant);
        apiRest.setMessage("查询商户信息成功！");
        apiRest.setSuccessful(true);
        return apiRest;
    }
}
