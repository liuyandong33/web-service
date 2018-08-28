package build.dream.platform.jobs;

import build.dream.common.beans.ComponentAccessToken;
import build.dream.common.saas.domains.WeiXinAuthorizerToken;
import build.dream.common.saas.domains.WeiXinOpenPlatformApplication;
import build.dream.common.utils.*;
import build.dream.platform.constants.Constants;
import build.dream.platform.services.WeiXinService;
import org.apache.commons.collections.MapUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefreshWeiXinAuthorizerTokenJob implements Job {
    private static final String CLASS_NAME = RefreshWeiXinAuthorizerTokenJob.class.getName();
    @Autowired
    private WeiXinService weiXinService;

    @Override
    public void execute(JobExecutionContext context) {
        List<WeiXinAuthorizerToken> weiXinAuthorizerTokens = weiXinService.findAllWeiXinAuthorizerTokens();
        Map<String, String> weiXinAuthorizerTokenMap = new HashMap<String, String>();
        Map<String, ComponentAccessToken> componentAccessTokenMap = new HashMap<String, ComponentAccessToken>();
        for (WeiXinAuthorizerToken weiXinAuthorizerToken : weiXinAuthorizerTokens) {
            try {
                String componentAppId = weiXinAuthorizerToken.getComponentAppId();
                ComponentAccessToken componentAccessToken = componentAccessTokenMap.get(componentAppId);
                if (componentAccessToken == null) {
                    WeiXinOpenPlatformApplication weiXinOpenPlatformApplication = weiXinService.findWeiXinOpenPlatformApplication(componentAppId);
                    ValidateUtils.notNull(weiXinOpenPlatformApplication, "微信开放平台应用不存在！");

                    componentAccessToken = WeiXinUtils.obtainComponentAccessToken(weiXinOpenPlatformApplication.getAppId(), weiXinOpenPlatformApplication.getAppSecret());
                    componentAccessTokenMap.put(componentAppId, componentAccessToken);
                }
                WeiXinAuthorizerToken newWeiXinAuthorizerToken = weiXinService.refreshWeiXinAuthorizerToken(componentAccessToken.getComponentAccessToken(), weiXinAuthorizerToken);
                weiXinAuthorizerTokenMap.put(newWeiXinAuthorizerToken.getComponentAppId() + "_" + newWeiXinAuthorizerToken.getAuthorizerAppId(), GsonUtils.toJson(newWeiXinAuthorizerToken));
            } catch (Exception e) {
                deleteWeiXinAuthorizerTokenSafe(weiXinAuthorizerToken, e);
            }
        }

        CacheUtils.delete(Constants.KEY_WEI_XIN_AUTHORIZER_TOKENS);
        if (MapUtils.isNotEmpty(weiXinAuthorizerTokenMap)) {
            CacheUtils.hmset(Constants.KEY_WEI_XIN_AUTHORIZER_TOKENS, weiXinAuthorizerTokenMap);
        }
    }

    private void deleteWeiXinAuthorizerTokenSafe(WeiXinAuthorizerToken weiXinAuthorizerToken, Exception exception) {
        try {
            String lastUpdateRemark = "刷新token失败，删除本条记录-" + exception.getMessage();
            if (lastUpdateRemark.length() > 255) {
                lastUpdateRemark = lastUpdateRemark.substring(0, 255);
            }
            weiXinAuthorizerToken.setLastUpdateRemark(lastUpdateRemark);
            weiXinAuthorizerToken.setDeleted(true);
            weiXinService.updateWeiXinAuthorizerToken(weiXinAuthorizerToken);
        } catch (Exception e) {
            LogUtils.error("删除微信授权token失败", CLASS_NAME, "deleteWeiXinAuthorizerTokenSafe", e);
        }
    }
}
