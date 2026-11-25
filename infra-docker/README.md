# 本地共享中间件底座（infra-docker）

面向 32 GB 内存开发机的 `bop` 本地共享中间件编排，所有容器统一使用 `Asia/Shanghai` 时区、bridge 网络 `bop-net`，并通过 `.env` 集中管理宿主机端口与本地开发密码。

## 1. 服务端口与默认凭据

| 服务 | 容器名 | 宿主机端口 | 访问地址 / 连接方式 | 默认账号密码 / Token |
| :--- | :--- | :--- | :--- | :--- |
| MySQL 8.0 | `bop-mysql` | `3306` | `localhost:3306` | `root` / `bop_root_123456`（见 `.env`） |
| Redis 7 | `bop-redis` | `6379` | `localhost:6379` | 无密码（仅本地开发） |
| ActiveMQ 5.18.6 | `bop-activemq` | `61616` / `8161` | OpenWire: `tcp://localhost:61616`<br>控制台: `http://localhost:8161/admin` | `admin` / `admin` |
| Nacos 2.3.2 | `bop-nacos` | `8848` / `9848` | `http://localhost:8848/nacos` | 已关闭鉴权（`NACOS_AUTH_ENABLE=false`） |
| XXL-JOB 2.4.1 | `bop-xxl-job` | `18088` | `http://localhost:18088/xxl-job-admin` | 控制台：`admin` / `123456`<br>AccessToken：`bop_local_token` |

首次启动 MySQL 时会自动创建五个 `utf8mb4` 数据库：`bop_workorder`、`bop_notify`、`bop_workflow`、`bop_settlement`、`xxl_job`，并初始化 XXL-JOB 2.4.1 全部数据表。

## 2. 常用启停命令

首次启动前，在 `infra-docker/` 目录下复制环境变量模板（`.env` 已被 Git 忽略，可按需修改端口或本地密码）：

```bash
# Windows (PowerShell / CMD)
copy .env.example .env

# Linux / macOS
cp .env.example .env
```

随后执行启停命令：

```bash
# 启动全部中间件
docker compose up -d

# 查看容器状态与健康检查结果
docker compose ps

# 查看指定容器日志
docker compose logs -f activemq
docker compose logs -f xxl-job-admin

# 停止并移除容器（保留数据卷）
docker compose down
```

> 注意：MySQL 仅在数据卷为空时执行 `init-sql/` 下的初始化脚本。如果修改了 `init-sql/` 或需要重新触发数据库初始化，必须先清理数据卷再启动：
>
> ```bash
> docker compose down -v
> docker compose up -d
> ```

## 3. 关键功能验证

### 3.1 验证 ActiveMQ 延迟调度（`schedulerSupport`）

1. 检查启动日志，确认 JobScheduler 已初始化：
   ```bash
   docker compose logs activemq | findstr /i "JobScheduler"
   ```
   （Linux/macOS 可用 `grep -i "JobScheduler"`），输出包含 `JobSchedulerStore` 或 `Scheduler` 启动日志即表示生效。
2. 浏览器打开 `http://localhost:8161/admin`（账号 `admin`，密码 `admin`），顶部导航栏点击 **Scheduled** 页面：
   - 若页面正常打开并展示延迟任务列表（而非提示未开启 `schedulerSupport`），说明延迟队列已就绪。
   - 若需手工测试，可在 **Queues** 中新建队列并点击 **Send To**，在 `Scheduled delay (ms)` 填入 `10000` 后发送，即可在 **Scheduled** 页观察到待投递消息并在 10 秒后进入目标队列。

### 3.2 验证 XXL-JOB 控制台

1. 确认 `bop-mysql` 状态为 `healthy` 且 `bop-xxl-job` 已启动完成。
2. 浏览器打开 `http://localhost:18088/xxl-job-admin`，使用 `admin` / `123456` 登录。
3. 依次打开 **任务管理** 与 **执行器管理**，能看到初始化的 `示例执行器`（`xxl-job-executor-sample`）与 `测试任务1`，即说明 `xxl_job` 库连接、字符集及表结构初始化均正常。
4. 后续业务服务接入执行器时，在配置中填写 `xxl.job.admin.addresses=http://127.0.0.1:18088/xxl-job-admin` 与 `xxl.job.accessToken=bop_local_token`。
