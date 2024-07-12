## 介绍

> `tdengine-orm-spring-boot-starter`是一个基于SpringBootJdbc, 便捷操作TDengine数据的半ORM框架, 参考了MyBatisPlus的设计

### 技术栈

- spring-boot-starter 2.X 主要用到SpringBoot的自动装配功能, SpringBoot2.7以后自动装配方式有修改, 但旧的方式仍旧兼容
- spring-boot-starter-jdbc 2.x 主要用到JdbcTemplate对象
- MyBatisPlusAnnotation 3.5.x 主要就是用到几个注解, 比如@TableName @TableField

## 快速开始

1. 拉取项目源码, 执行`mvn clean install` (或者推送到你自己的私仓)
2. 下载Demo项目源码: [Demo项目仓库地址](https://github.com/klaus-cicd/tdengine-orm-demo)
3. 修改Demo项目内配置文件内的连接配置, 创建对应的数据库(如果没有)
4. 找到测试类, 按顺序依次执行测试方法
