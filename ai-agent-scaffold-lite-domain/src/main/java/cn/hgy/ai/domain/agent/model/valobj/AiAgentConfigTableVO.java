package cn.hgy.ai.domain.agent.model.valobj;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Ai Agent 智能体配置表值对象
 *
 * @author HGY 2823480928@qq.com @黄光宇
 * 2025/11/29 10:54
 */
@Data
public class AiAgentConfigTableVO {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 顶层智能体基础信息（ID、名称、描述）
     */
    private Agent agent;

    /**
     * 智能体运行模块（API连接、模型、子Agent、工作流）
     */
    private Module module;

    @Data
    public static class Agent {

        /**
         * 智能体唯一标识
         */
        private String agentId;

        /**
         * 智能体名称
         */
        private String agentName;

        /**
         * 智能体描述
         */
        private String agentDesc;

    }

    @Data
    public static class Module {

        /**
         * AI 接口连接配置（baseUrl、apiKey、路径等）
         */
        private AiApi aiApi;

        /**
         * 对话模型配置（模型名称、MCP工具列表）
         */
        private ChatModel chatModel;

        /**
         * 工作流中参与编排的子 Agent 列表
         */
        private List<Agent> agents;

        /**
         * 工作流编排列表（顺序/并行/循环）
         */
        private List<AgentWorkflow> agentWorkflows;

        private Runner runner;

        @Data
        public static class AiApi {
            /**
             * AI 服务 base URL
             */
            private String baseUrl;

            /**
             * API 鉴权密钥
             */
            private String apiKey;

            /**
             * Chat 补全接口路径
             */
            private String completionsPath = "/v1/chat/completions";

            /**
             * Embeddings 接口路径
             */
            private String embeddingsPath = "/v1/embeddings";

        }

        @Data
        public static class ChatModel {

            /**
             * 模型名称，如 gpt-4o、gpt-4.1
             */
            private String model;

            /**
             * MCP 工具列表，支持 SSE 和 Stdio 两种接入方式
             */
            private List<ToolMcp> toolMcpList;

            @Data
            public static class ToolMcp {

                /**
                 * SSE 方式接入的 MCP 服务参数
                 */
                private SSEServerParameters sse;

                /**
                 * Stdio 方式接入的 MCP 服务参数
                 */
                private StdioServerParameters stdio;

                @Data
                public static class SSEServerParameters {
                    /**
                     * 服务名称
                     */
                    private String name;

                    /**
                     * SSE 服务 base URI
                     */
                    private String baseUri;

                    /**
                     * SSE 连接端点路径
                     */
                    private String sseEndpoint;

                    /**
                     * 请求超时时间（毫秒），默认 3000
                     */
                    private Integer requestTimeout = 3000;

                }

                @Data
                public static class StdioServerParameters {
                    /**
                     * 服务名称
                     */
                    private String name;

                    /**
                     * 请求超时时间（毫秒），默认 3000
                     */
                    private Integer requestTimeout = 3000;

                    /**
                     * 子进程启动参数（命令、参数列表、环境变量）
                     */
                    private ServerParameters serverParameters;

                    @Data
                    public static class ServerParameters {
                        /**
                         * 启动命令，如 node、python
                         */
                        private String command;

                        /**
                         * 命令行参数列表
                         */
                        private List<String> args;

                        /**
                         * 进程环境变量
                         */
                        private Map<String, String> env;

                    }
                }

            }
        }

        @Data
        public static class Agent {
            /**
             * 子 Agent 名称
             */
            private String name;

            /**
             * 系统指令（system prompt）
             */
            private String instruction;

            /**
             * 子 Agent 描述
             */
            private String description;

            /**
             * 输出结果的 key 名
             */
            private String outputKey;

        }

        @Data
        public static class AgentWorkflow {
            /**
             * 工作流类型；loop、parallel、sequential
             */
            private String type;

            /**
             * 工作流名称
             */
            private String name;

            /**
             * 参与该工作流的子 Agent 名称列表
             */
            private List<String> subAgents;

            /**
             * 工作流描述
             */
            private String description;

            /**
             * loop 类型的最大迭代次数，默认 3
             */
            private Integer maxIterations = 3;

        }

        @Data
        public static class Runner {
            private String agentName;
        }
    }

}
