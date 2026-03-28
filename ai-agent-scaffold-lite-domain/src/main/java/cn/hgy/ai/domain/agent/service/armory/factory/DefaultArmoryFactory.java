package cn.hgy.ai.domain.agent.service.armory.factory;

import cn.bugstack.wrench.design.framework.tree.DynamicContext;
import cn.hgy.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.hgy.ai.domain.agent.service.armory.node.RootNode;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.SequentialAgent;
import lombok.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 默认的装配工厂
 *
 * @author HGY 2823480928@qq.com @黄光宇
 * 2025/12/17 08:16
 */
@Service
public class DefaultArmoryFactory {

    @Resource
    private RootNode rootNode;

    public StrategyHandler<ArmoryCommandEntity, DynamicContext, AiAgentRegisterVO> armoryStrategyHandler() {
        return rootNode;
    }

    /**
     * 定义一个上下文对象，用于各个节点串联的时候，写入数据和使用数据
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext extends cn.bugstack.wrench.design.framework.tree.DynamicContext {

        /**
         * LLM api
         */
        private OpenAiApi openAiApi;

        /**
         * LLM chatModel
         */
        private ChatModel chatModel;

        /**
         * 已构建的 LlmAgent 集合，key 为 Agent 名称
         */
        private Map<String, BaseAgent> agentGroup = new HashMap<>();

        /**
         * 待编排的工作流配置列表，AgentWorkflowNode 按顺序消费
         */
        private List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = new ArrayList<>();

        /**
         * 通用数据容器，各节点可按 key 存取任意数据
         */
        private Map<String, Object> dataObjects = new HashMap<>();

        /**
         * 当做最后一个智能体节点
         */
        private SequentialAgent sequentialAgent;

        public List<BaseAgent> queryAgentList(List<String> agentNames) {
            if (agentNames == null || agentNames.isEmpty() || agentGroup == null) {
                return Collections.emptyList();
            }

            List<BaseAgent> agents = new ArrayList<>();
            for (String name : agentNames) {
                BaseAgent agent = agentGroup.get(name);
                if (agent!=null){
                    agents.add(agent);
                }
            }

            return agents;
        }
        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }

    }
}
