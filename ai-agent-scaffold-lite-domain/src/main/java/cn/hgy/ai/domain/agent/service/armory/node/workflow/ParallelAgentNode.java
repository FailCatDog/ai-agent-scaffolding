package cn.hgy.ai.domain.agent.service.armory.node.workflow;

import cn.hgy.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.hgy.ai.domain.agent.model.valobj.enums.AgentTypeEnum;
import cn.hgy.ai.domain.agent.service.armory.AbstractArmorySupport;
import cn.hgy.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.ParallelAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 并行工作流节点
 * 对应 AgentTypeEnum.Parallel，负责执行并行类型的 Agent 编排逻辑
 * doApply 完成本节点业务后，由 get() 根据下一个工作流配置决定继续路由到哪个节点
 *
 * 处理并行操作，如我们有一些场景，需要同步并行的一起完成数据处理任务，
 * 多条链路一起完成数据的获取、分析和决策，这样可以对复杂的流程显著的提高执行效率。
 */
@Slf4j
@Service("parallelAgentNode")
public class ParallelAgentNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - ParallelAgentNode");

        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = dynamicContext.getAgentWorkflows();
        // remove(0) 消费队列头部，处理完后队列缩短，后续节点处理剩余工作流
        AiAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.remove(0);

        // 按配置中的 subAgents 名称列表，从 agentGroup 中取出已构建的 LlmAgent 实例
        List<String> subAgentNames = agentWorkflow.getSubAgents();
        List<BaseAgent> subAgents = dynamicContext.queryAgentList(subAgentNames);

        // 构建 ParallelAgent，subAgents 中的所有 Agent 将被并发调度执行
        ParallelAgent parallelAgent =
                ParallelAgent.builder()
                        .name(agentWorkflow.getName())
                        .description(agentWorkflow.getDescription())
                        .subAgents(subAgents)
                        .build();

        // 将构建好的 ParallelAgent 也存入 agentGroup，后续工作流可将其作为子 Agent 引用
        dynamicContext.getAgentGroup().put(agentWorkflow.getName(), parallelAgent);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = dynamicContext.getAgentWorkflows();

        if (null == agentWorkflows || agentWorkflows.isEmpty()){
            return defaultStrategyHandler;
        }

        // 取队列头部工作流，按类型路由到下一个子工作流节点
        AiAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.get(0);

        String type = agentWorkflow.getType();
        AgentTypeEnum agentTypeEnum = AgentTypeEnum.formType(type);

        if (null == agentTypeEnum){
            throw new RuntimeException("agentWorkflow type is error!");
        }

        String node = agentTypeEnum.getNode();

        return switch (node){
            case "loopAgentNode" -> getBean("loopAgentNode");
            case "sequentialAgentNode" -> getBean("sequentialAgentNode");
            default -> defaultStrategyHandler;
        };
    }
}
