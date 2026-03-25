package cn.hgy.ai.domain.agent.service.armory.factory;

import cn.bugstack.wrench.design.framework.tree.DynamicContext;
import cn.hgy.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.hgy.ai.domain.agent.service.armory.node.RootNode;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

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

        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }

    }

}
