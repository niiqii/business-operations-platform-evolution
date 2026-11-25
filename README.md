# Business Operations Platform Evolution (BOP)

面向外包运维与结算场景的业务运营平台演进工程，围绕“工单流转、外部流程集成、消息预警、结算对账”四条核心链路，展示存量 JDK 8 微服务与新一代 JDK 21 微服务协同演进的完整实践。

## 1. 微服务边界与端口规划

仓库内各工程采用物理隔离的独立 Maven 结构，不建立共享根父 POM，避免 JDK 8 与 JDK 21 依赖交叉污染。

| 模块 | 端口 | 数据库 | JDK / 框架基线 | 职责边界 |
| :--- | :--- | :--- | :--- | :--- |
| `mock-external-gateway` | `18080` | 内存态 | JDK 21 + Spring Boot 3.5.x | 仿真外部 Lander 流程引擎、通知渠道与 SCM 供应链接口，支持延迟、失败与乱序回调等故障注入 |
| `workorder-service` | `8081` | `bop_workorder` | JDK 8 + Spring Boot 2.7.18 + Cloud 2021.0.8 + SCA 2021.0.5.0 | 工时核算单（`WORKHOUR`）与日常考核单（`ASSESSMENT`）动态表单校验、状态流转与基础查询 |
| `notify-service` | `8082` | `bop_notify` | JDK 21 + Spring Boot 3.5.x + Cloud 2025.0.x + SCA 2025.0.0.0 | 异步消息路由、模板渲染、重试与发送审计（后续演进阶段接入） |
| `workflow-service` | `8083` | `bop_workflow` | JDK 21 + Spring Boot 3.5.x + Cloud 2025.0.x + SCA 2025.0.0.0 | Lander 流程防腐层（ACL）、回调验签、幂等去重与乱序拦截（后续演进阶段接入） |
| `settlement-service` | `8084` | `bop_settlement` | JDK 21 + Spring Boot 3.5.x + Cloud 2025.0.x + SCA 2025.0.0.0 | 订单额度校验、预结算汇总、SCM 请款推送与结算对账（后续演进阶段接入） |
| `bop-gateway` | `8080` | 无 | JDK 21 + Spring Boot 3.5.x + Cloud 2025.0.x + SCA 2025.0.0.0 | 统一入口路由、TraceId 透传与限流熔断（后续演进阶段接入） |

## 2. 技术栈与公共约定

- **基础设施**：MySQL 8.0、Redis 7、ActiveMQ 5.18.6（OpenWire，兼容 JDK 8 ActiveMQ 5.16.x 与 JDK 21 ActiveMQ 6.1.x 客户端）、Nacos 2.x、XXL-JOB。
- **统一契约**：所有微服务统一使用 `ApiResult<T>(code, message, data, traceId)`、`PageResult<T>` 及全局异常处理；`X-Trace-Id` 在 HTTP Filter `finally` 中严格清理并向下游透传。
- **金额与时间**：金额、单价、扣款计算一律使用 `BigDecimal`；数据库、容器与 JSON 序列化统一采用 `Asia/Shanghai` 时区、`UTF-8` 编码和 24 小时制（`yyyy-MM-dd HH:mm:ss`）。
- **workorder-service 关键依赖版本**：
  - `com.baomidou:mybatis-plus-boot-starter:3.5.3.2`
  - `com.networknt:json-schema-validator:1.0.87`
  - `com.github.xiaoymin:knife4j-openapi3-spring-boot-starter:4.3.0`

## 3. 本地启动步骤

### 3.1 启动基础设施

```bash
cd infra-docker
cp .env.example .env
docker compose up -d
```

基础设施就绪后，向 `bop_workorder` 库导入初始化脚本：

```bash
mysql -h 127.0.0.1 -P 3306 -u root -pbop_root_123456 < workorder-service/src/main/resources/db/schema.sql
```

### 3.2 启动外部仿真网关（JDK 21）

```bash
cd mock-external-gateway
mvn spring-boot:run
```

- 仿真网关地址：`http://127.0.0.1:18080`
- 混沌控制台：`http://127.0.0.1:18080/chaos.html`

### 3.3 启动工单服务（JDK 8）

确保当前终端 `JAVA_HOME` 指向 JDK 8 后执行：

```bash
cd workorder-service
mvn clean compile
mvn spring-boot:run
```

- 服务地址：`http://127.0.0.1:8081`
- 接口文档：`http://127.0.0.1:8081/doc.html`
- 如需脱离 Nacos 单独启动，可追加参数 `-DNACOS_ENABLED=false`。
