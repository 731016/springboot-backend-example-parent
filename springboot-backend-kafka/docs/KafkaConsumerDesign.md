# Kafka 消费者扩展与多点位设计

当前模块使用**固定 topic、固定分区数、固定 groupId**。以下说明在「消费者不足」或「需要新增消费点位」时如何设计与扩展。

---

## 一、概念约定

| 概念 | 说明 |
|------|------|
| **同一消费组（相同 groupId）** | 同一 topic 下分区在组内消费者之间分配，每条消息只被组内一个消费者处理（负载均衡） |
| **不同消费组（不同 groupId）** | 同一 topic 可被多个组独立消费，每条消息会被每个组各消费一次（广播式、多点位） |
| **分区数（partition）** | 决定同一 groupId 下**最多**有多少个并发消费者能同时拉取到分区（多余消费者会空闲） |
| **单实例并发数（concurrency）** | 每个应用实例内启动的消费者线程数，总消费能力 ≈ 实例数 × concurrency，但受分区数上限约束 |

---

## 二、消费者不足时（同一业务、需要提速）

目标：**同一 groupId**，通过加实例或加并发，提高吞吐。

### 2.1 规则

- 同一 **groupId** 下，Kafka 会把各 **partition** 分配给组内不同消费者（同一 partition 只给一个消费者）。
- **有效并发** = min(topic 分区数, 组内消费者总数)。  
  例如：topic 有 3 个分区，组里共有 5 个消费者线程 → 只有 3 个线程能拿到分区，另外 2 个空闲。

### 2.2 做法

1. **先保证分区数够用**  
   - 当前代码中 `DEFAULT_PARTITION_NUM = 3`，创建 topic 时建议分区数 ≥ 期望的「组内同时工作的消费者数」。
   - 若计划：2 个实例 × 每实例 3 线程 = 6 个消费者，则 topic 分区数建议 ≥ 6（否则会有线程空闲）。
   - 分区数可在创建 topic 时指定，后续也可扩容（新分区只对新消息生效，旧消息仍在旧分区）。

2. **水平扩容实例（推荐）**  
   - 多部署几个应用实例，**保持 groupId 不变**（如 `raw-data-process-group`）。
   - 不改代码，只加机器/容器，Kafka 会做 **rebalance**，把分区重新分配给所有实例的消费者。
   - 每个实例的 `concurrency` 可继续用配置控制（见下文「配置说明」）。

3. **单机加并发**  
   - 在**分区数足够**的前提下，提高单实例的 `concurrency`（例如从 3 调到 6）。
   - 同一实例内多个线程会作为同一 group 的多个消费者参与分区分配，同样受「分区数」上限约束。

4. **配置建议**  
   - 分区数：按预期总消费者数设定，例如「实例数 × 每实例 concurrency」。
   - `concurrency`：通过配置文件设置（如 `app.kafka.consumer-concurrency`），便于按环境调优，无需改代码。

---

## 三、需要增加「新点位」时（新业务、新用途）

目标：**同一 topic** 被多套逻辑消费（例如：一套写库、一套写搜索引擎、一套做实时统计），互不抢占分区，各自独立 offset。

### 3.1 规则

- **不同 groupId = 不同消费组**。  
  同一 topic 的每条消息会被**每个 groupId** 各消费一次，互不影响。
- 每个 groupId 内部仍然遵守「分区分配」：该组内消费者数仍不宜长期大于分区数，否则会有空闲。

### 3.2 做法

1. **为新业务单独设一个 groupId**  
   - 例如：  
     - 现有：`raw-data-process-group`（落库、业务处理）  
     - 新增：`raw-data-analytics-group`（只做统计）、`raw-data-search-group`（同步到搜索引擎）  
   - 新业务新建一个 Consumer 类（或新服务），订阅**同一个 topic**（如 `raw-data`），使用**新的 groupId**。

2. **代码组织方式**  
   - **方式 A**：在本项目中新增一个 `@KafkaListener`，只改 `groupId`，其它 topic、序列化、ack 方式与现有一致。  
   - **方式 B**：新业务独立成单独服务/模块，只依赖同一 Kafka 集群和 topic，各自配置自己的 groupId。

3. **配置化 groupId（推荐）**  
   - 将 groupId 放到配置文件（如 `app.kafka.raw-data.group-id`），不同环境或不同部署可以填不同值。  
   - 这样「同一份代码」可以通过配置拆成多套消费者（多实例 + 不同 groupId），便于做多点位、多环境。

4. **分区数**  
   - 多 groupId 不增加 topic 分区数压力，每个组独立分配分区。  
   - 每个 groupId 下仍按「该组内消费者数 ≤ 分区数」来规划即可。

---

## 四、当前工程中的配置与常量

- **Topic**：`raw-data`、`raw-data-dlq`、`test`、`test-dlq`（见 `KafkaConstants`）。
- **分区数**：创建 topic 时建议使用与 `DEFAULT_PARTITION_NUM` 或配置中 `concurrency` 一致或更大。
- **groupId**：  
  - 默认消费者：`spring.kafka.consumer.group-id`（如 `spring-boot-init`）。  
  - raw-data：`KafkaConstants.RAW_DATA_GROUP_ID`（`raw-data-process-group`），可改为从配置读取（如 `app.kafka.raw-data.group-id`）。
- **并发**：`KafkaConfig` 中 `ackContainerFactory` 的 `setConcurrency` 当前与 `DEFAULT_PARTITION_NUM` 一致，可改为读取配置（如 `app.kafka.consumer-concurrency`），便于按环境调整。

---

## 五、设计小结

| 场景 | 做法 |
|------|------|
| 消费者不足、要提速 | 保持 **groupId 不变**；保证 **分区数 ≥ 组内消费者数**；增加实例或提高单实例 **concurrency**。 |
| 新增消费点位（新业务） | 使用**新的 groupId**，订阅同一 topic；新 listener 或新服务；groupId 建议配置化。 |
| 分区数 | 创建 topic 时按「该 topic 上最大一组消费者的数量」设定，后续可按需扩容分区。 |
| 配置化 | groupId、concurrency 建议从配置文件读取，便于扩实例、多点位、多环境。 |

按上述方式，在固定 topic 与分区数的前提下，既能通过加实例/调并发解决「消费者不足」，也能通过多 groupId 增加「新点位」而不影响现有消费者。

---

## 六、如何查看当前分区、消费者、生产者运行情况

### 6.1 本应用提供的 HTTP 接口（推荐）

启动应用后，可直接调用以下接口（需保证能连上配置的 `spring.kafka.bootstrap-servers`）：

| 接口 | 说明 |
|------|------|
| `GET /api/kafka/admin/topics` | 所有 topic 及分区数、各分区 leader 等信息 |
| `GET /api/kafka/admin/consumer-groups` | 所有消费者组及状态、成员数 |
| `GET /api/kafka/admin/consumer-groups/detail?groupId=xxx` | 指定消费者组详情：成员、各分区当前 offset、末尾 offset、lag |

返回示例含义简要说明：

- **topics**：`topic`、`partitionCount`、每个分区的 `partition`、`leaderId`。
- **consumer-groups**：`groupId`、`state`（如 Stable）、`memberCount`、各 `members` 的 `consumerId`、`host`、`assignedPartitions`。
- **consumer-groups/detail**：在以上基础上增加 `partitionOffsets`（每分区 `currentOffset`、`endOffset`、`lag`）及 `totalLag`。

接口文档见 Swagger/Knife4j：`/api/doc.html`（以实际 context-path 为准）。

### 6.2 使用 Kafka 自带命令（需安装 Kafka 或使用镜像内脚本）

在 Kafka 安装目录下（或使用 `kafka-*` 脚本的镜像）：

**查看 topic 与分区：**

```bash
# 列出所有 topic
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# 查看某 topic 的分区与副本
bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic raw-data
```

**查看消费者组：**

```bash
# 列出所有消费者组
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# 查看某组的成员与各分区 offset、lag
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group raw-data-process-group
```

**生产者：**

- Kafka 本身不提供“生产者当前状态”的查询接口；生产者是发完即走的。
- 可观察：各 topic 的**最新 offset**（通过上面 describe 或本应用的 `consumer-groups/detail` 中的 `endOffset` 可间接反映写入进度）。
- 需要更细监控时，可配合 JMX 暴露 Kafka Producer 指标（如发送成功率、延迟），或使用监控系统（如 Prometheus + Grafana、Kafka Manager、AKHQ 等）查看 broker 与 client 指标。
