package cn.hgy.ai.domain.agent.service.armory.node.workflow;

import cn.hgy.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.hgy.ai.domain.agent.service.armory.AbstractArmorySupport;
import cn.hgy.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.domain.agent.service.armory.node.RunnerNode;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.SequentialAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 串行工作流节点
 * 对应 AgentTypeEnum.Sequential，负责执行串行类型的 Agent 编排逻辑
 * 当前为责任链末端，无后续节点，返回 defaultStrategyHandler 终止链路
 *
 * 处理串行操作，主要用于编排子智能体，和 loop 循环、parallel 并行，
 * 组合出复杂的智能体流程。当然，你也可以使用 loop 组合 sequence 或者 parallel 等处理过程。
 */
@Slf4j
@Service("sequentialAgentNode")
public class SequentialAgentNode extends AbstractArmorySupport {

    @Resource
    private RunnerNode runnerNode;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - SequentialAgentNode");

        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = dynamicContext.getAgentWorkflows();
        // remove(0) 消费队列头部，处理完后队列缩短，后续节点处理剩余工作流
        AiAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.remove(0);

        // 按配置中的 subAgents 名称列表，从 agentGroup 中取出已构建的 Agent 实例
        List<String> subAgentNames = agentWorkflow.getSubAgents();
        List<BaseAgent> subAgents = dynamicContext.queryAgentList(subAgentNames);

        // 构建 SequentialAgent，subAgents 中的 Agent 将按列表顺序串行执行，前一个输出作为后一个输入
        SequentialAgent sequentialAgent =
                SequentialAgent.builder()
                        .name(agentWorkflow.getName())
                        .description(agentWorkflow.getDescription())
                        .subAgents(subAgents)
                        .build();

        // 将构建好的 SequentialAgent 也存入 agentGroup，后续工作流可将其作为子 Agent 引用
        dynamicContext.getAgentGroup().put(agentWorkflow.getName(), sequentialAgent);

        // 将最终编排好的 SequentialAgent 动态注册到 Spring 容器，供外部通过 beanName 注入使用
        registerBean(agentWorkflow.getName(), SequentialAgent.class, sequentialAgent);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

}
