package cn.hgy.ai.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.hgy.ai.domain.agent.service.IArmoryService;
import cn.hgy.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-25
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArmoryService implements IArmoryService {

    private final DefaultArmoryFactory defaultArmoryFactory;

    @Override
    public void acceptArmoryAgents(List<AiAgentConfigTableVO> tables) throws Exception {
        for (AiAgentConfigTableVO table : tables) {
            StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> handler = defaultArmoryFactory.armoryStrategyHandler();
            handler.apply(
                    ArmoryCommandEntity.builder()
                            .aiAgentConfigTableVO(table)
                            .build(),
                    new DefaultArmoryFactory.DynamicContext());
        }
    }


}
