package build.dream.platform.mappers;

import build.dream.common.saas.domains.TenantGoods;
import build.dream.common.utils.SearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Mapper
public interface TenantGoodsMapper {
    long insert(TenantGoods tenantGoods);
    long update(TenantGoods tenantGoods);
    TenantGoods find(SearchModel searchModel);
    List<TenantGoods> findAll(SearchModel searchModel);
    List<Map<String, Object>> findAllGoodses(@Param("tenantId") BigInteger tenantId, @Param("branchId") BigInteger branchId);
    Map<String, Object> findGoods(@Param("tenantId") BigInteger tenantId, @Param("branchId") BigInteger branchId, @Param("goodsId") BigInteger goodsId);
}
