# SpringBoot 项目初始模板

## 项目结构
1.[springboot-backend-example](springboot-backend-example):基于 Java SpringBoot 的项目初始模板，整合了常用框架和主流业务的示例代码
2.[springboot-backend-common](springboot-backend-common):公共配置，常用工具类等
3.[springboot-backend-fileupload](springboot-backend-fileupload):文件管理，包括cos对象存储和本地文件
4.[online-code-executor-starter](online-code-executor-starter):从零开始，使用 Docker 和 Docker-Java 构建一个属于你自己的代码执行器 starter。本教程自定义实现了容器池，可以有效管理 Docker 容器资源，以保证系统的并发访问。


## 打包
mvn clean package -DskipTests -Pprod
