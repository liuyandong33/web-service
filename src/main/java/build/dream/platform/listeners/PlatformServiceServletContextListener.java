package build.dream.platform.listeners;

import build.dream.common.listeners.BasicServletContextListener;
import build.dream.common.saas.domains.*;
import build.dream.common.utils.CacheUtils;
import build.dream.common.utils.GsonUtils;
import build.dream.platform.constants.Constants;
import build.dream.platform.jobs.JobScheduler;
import build.dream.platform.mappers.CommonMapper;
import build.dream.platform.services.*;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebListener
public class PlatformServiceServletContextListener extends BasicServletContextListener {
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private TenantSecretKeyService tenantSecretKeyService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private JobScheduler jobScheduler;
    @Autowired
    private WeiXinService weiXinService;
    @Autowired
    private NewLandService newLandService;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        super.previousInjectionBean(servletContextEvent.getServletContext(), CommonMapper.class);

        // 缓存支付宝账号
        List<AlipayAccount> alipayAccounts = alipayService.findAllAlipayAccounts();
        Map<String, String> alipayAccountMap = new HashMap<String, String>();
        for (AlipayAccount alipayAccount : alipayAccounts) {
            alipayAccountMap.put(alipayAccount.getTenantId() + "_" + alipayAccount.getBranchId(), GsonUtils.toJson(alipayAccount));
        }
        CacheUtils.delete(Constants.KEY_ALIPAY_ACCOUNTS);
        if (MapUtils.isNotEmpty(alipayAccountMap)) {
            CacheUtils.hmset(Constants.KEY_ALIPAY_ACCOUNTS, alipayAccountMap);
        }

        // 缓存微信支付账号
        List<WeiXinPayAccount> weiXinPayAccounts = weiXinService.findAllWeiXinPayAccounts();
        Map<String, String> weiXinPayAccountMap = new HashMap<String, String>();
        for (WeiXinPayAccount weiXinPayAccount : weiXinPayAccounts) {
            weiXinPayAccountMap.put(weiXinPayAccount.getTenantId() + "_" + weiXinPayAccount.getBranchId(), GsonUtils.toJson(weiXinPayAccount));
        }
        CacheUtils.delete(Constants.KEY_WEI_XIN_PAY_ACCOUNTS);
        if (MapUtils.isNotEmpty(weiXinPayAccountMap)) {
            CacheUtils.hmset(Constants.KEY_WEI_XIN_PAY_ACCOUNTS, weiXinPayAccountMap);
        }

        // 缓存商户信息
        List<Tenant> tenants = tenantService.obtainAllTenantInfos();
        Map<String, String> tenantInfos = new HashMap<String, String>();
        for (Tenant tenant : tenants) {
            String tenantInfo = GsonUtils.toJson(tenant);
            tenantInfos.put(tenant.getId().toString(), tenantInfo);
            tenantInfos.put(tenant.getCode(), tenantInfo);
        }
        CacheUtils.delete(Constants.KEY_TENANT_INFOS);
        if (MapUtils.isNotEmpty(tenantInfos)) {
            CacheUtils.hmset(Constants.KEY_TENANT_INFOS, tenantInfos);
        }

        // 缓存微信授权token
        List<WeiXinAuthorizerToken> weiXinAuthorizerTokens = weiXinService.findAllWeiXinAuthorizerTokens();
        Map<String, String> weiXinAuthorizerTokenMap = new HashMap<String, String>();
        for (WeiXinAuthorizerToken weiXinAuthorizerToken : weiXinAuthorizerTokens) {
            weiXinAuthorizerTokenMap.put(weiXinAuthorizerToken.getComponentAppId() + "_" + weiXinAuthorizerToken.getAuthorizerAppId(), GsonUtils.toJson(weiXinAuthorizerToken));
        }

        CacheUtils.delete(Constants.KEY_WEI_XIN_AUTHORIZER_TOKENS);
        if (MapUtils.isNotEmpty(weiXinAuthorizerTokenMap)) {
            CacheUtils.hmset(Constants.KEY_WEI_XIN_AUTHORIZER_TOKENS, weiXinAuthorizerTokenMap);
        }

        // 缓存新大陆账号
        List<NewLandAccount> newLandAccounts = newLandService.obtainAllNewLandAccounts();
        Map<String, String> newLandAccountMap = new HashMap<String, String>();
        for (NewLandAccount newLandAccount : newLandAccounts) {
            newLandAccountMap.put(newLandAccount.getTenantId() + "_" + newLandAccount.getBranchId(), GsonUtils.toJson(newLandAccount));
        }
        CacheUtils.delete(Constants.KEY_NEW_LAND_ACCOUNTS);
        if (MapUtils.isNotEmpty(newLandAccountMap)) {
            CacheUtils.hmset(Constants.KEY_NEW_LAND_ACCOUNTS, newLandAccountMap);
        }

        // 启动所有定时任务
        jobScheduler.scheduler();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
