package cn.hgy.ai.domain.agent.service.armory.node.workflow;

import cn.hgy.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.hgy.ai.domain.agent.model.valobj.enums.AgentTypeEnum;
import cn.hgy.ai.domain.agent.service.armory.AbstractArmorySupport;
import cn.hgy.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LoopAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 循环工作流节点
 * 对应 AgentTypeEnum.Loop，负责执行循环类型的 Agent 编排逻辑
 *
 * 处理循环操作，如一个用户请求，要进行分析、执行、检测，也可以是一段git提交的代码，
 * 进行 diff 获取差异，检索代码匹配召回、做出执行review计划，后面在依次执行分析。这些都是可以做循环处理的。
 *
 * doApply 完成本节点业务后，由 get() 根据下一个工作流配置决定继续路由到哪个节点
 */
@Slf4j
@Service("loopAgentNode")
public class LoopAgentNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - LoopAgentNode");
        AiAgentConfigTableVO.Module.AgentWorkflow currentAgentWorkflow = dynamicContext.getCurrentAgentWorkflow();
        // remove(0) 消费队列头部，处理完后队列缩短，后续节点处理剩余工作流
//        AiAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.remove(0);

        // 按配置中的 subAgents 名称列表，从 agentGroup 中取出已构建的 LlmAgent 实例
        List<String> subAgentNames = currentAgentWorkflow.getSubAgents();
        List<BaseAgent> subAgents = dynamicContext.queryAgentList(subAgentNames);

        // 构建 LoopAgent，maxIterations 控制最大循环次数，防止无限循环
        LoopAgent loopAgent =
                LoopAgent.builder()
                        .name(currentAgentWorkflow.getName())
                        .description(currentAgentWorkflow.getDescription())
                        .subAgents(subAgents)
                        .maxIterations(currentAgentWorkflow.getMaxIterations())
                        .build();

        // 将构建好的 LoopAgent 也存入 agentGroup，后续工作流可将其作为子 Agent 引用
        dynamicContext.getAgentGroup().put(currentAgentWorkflow.getName(), loopAgent);
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("agentWorkflowNode");
    }

}
