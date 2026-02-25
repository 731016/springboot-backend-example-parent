# SpringBoot 项目初始模板

## 项目结构
+ [online-code-executor-starter](online-code-executor-starter):从零开始，使用 Docker 和 Docker-Java 构建一个属于你自己的代码执行器 starter。本教程自定义实现了容器池，可以有效管理 Docker 容器资源，以保证系统的并发访问。
+ [springboot-backend-common](springboot-backend-common):公共配置，常用工具类,权限校验AOP,多数据源AOP,请求响应日志AOP等。
+ [springboot-backend-elasticsearch](springboot-backend-elasticsearch):elasticsearch搜索。
+ [springboot-backend-example](springboot-backend-example):基于 Java SpringBoot 的项目初始模板，整合了常用框架和主流业务的示例代码。
+ [springboot-backend-fileupload](springboot-backend-fileupload):文件管理，包括cos对象存储和本地文件。
+ [springboot-backend-kafka](springboot-backend-kafka):可通过配置数据采集点位和工作日历对数据进行采集，统计，分析，使用kafka推送采集数据，websocket推送数据给前端。
+ [springboot-backend-quartz](springboot-backend-quartz):定时任务框架，可视化管理定时任务。
+ [springboot-backend-redis](springboot-backend-redis):redis缓存管理。
+ [springboot-backend-websocket](springboot-backend-websocket):websocket推送。

## 打包
mvn clean package -DskipTests -Pprod
