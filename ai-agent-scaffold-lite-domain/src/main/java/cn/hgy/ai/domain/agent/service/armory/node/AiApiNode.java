package cn.hgy.ai.domain.agent.service.armory.node;

import cn.hgy.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.hgy.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.hgy.ai.domain.agent.service.armory.AbstractArmorySupport;
import cn.hgy.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

/**
 * AI API 装配节点
 * 负责从配置中读取 OpenAI API 连接参数，构建 OpenAiApi 实例并写入上下文，供后续节点使用
 */
@Slf4j
@Service
public class AiApiNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - AiApiNode");

        AiAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        AiAgentConfigTableVO.Module.AiApi aiApiConfig = aiAgentConfigTableVO.getModule().getAiApi();

        // 根据配置构建 OpenAiApi，completionsPath/embeddingsPath 为空时使用默认路径
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(aiApiConfig.getBaseUrl())
                .apiKey(aiApiConfig.getApiKey())
                .completionsPath(StringUtils.isNoneBlank(aiApiConfig.getCompletionsPath()) ? aiApiConfig.getCompletionsPath() : "v1/chat/completions")
                .embeddingsPath(StringUtils.isNoneBlank(aiApiConfig.getEmbeddingsPath()) ? aiApiConfig.getEmbeddingsPath() : "v1/embeddings")
                .build();

        dynamicContext.setOpenAiApi(openAiApi);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
