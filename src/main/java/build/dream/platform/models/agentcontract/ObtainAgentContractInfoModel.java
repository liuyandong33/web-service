package build.dream.platform.models.agentcontract;

import build.dream.common.models.BasicModel;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;

public class ObtainAgentContractInfoModel extends BasicModel {
    @NotNull
    private BigInteger agentId;
    @NotNull
    private BigInteger agentContractId;

    public BigInteger getAgentId() {
        return agentId;
    }

    public void setAgentId(BigInteger agentId) {
        this.agentId = agentId;
    }

    public BigInteger getAgentContractId() {
        return agentContractId;
    }

    public void setAgentContractId(BigInteger agentContractId) {
        this.agentContractId = agentContractId;
    }
}
