## 介绍

> `tdengine-orm-spring-boot-starter`是一个基于SpringBoot和MyBatisPlus的便捷操作TDengine数据的半ORM框架
> 需要自行映入SpringBoot MyBatisPlus TDengine连接驱动

### 技术栈

- spring-boot-starter 2.X (主要用到SpringBoot的自动装配功能, SpringBoot2.7以后自动装配方式有修改, 但旧的方式仍旧兼容)
- spring-boot-starter-jdbc 2.x 主要用到JdbcTemplate对象
- MyBatisPlus 3.5.x (主要用到MyBatis的一些注解和部分工具类)
- Taos jdbc driver 3.0 以上 (连接TDengine数据库使用)

## 使用

### 引入依赖

```xml

<dependencies>
    <!-- ... -->

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <version>2.4.2</version>
    </dependency>
    <dependency>
        <groupId>com.taosdata.jdbc</groupId>
        <artifactId>taos-jdbcdriver</artifactId>
        <version>3.2.5</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
        <version>2.4.2</version>
    </dependency>

    <!-- 如果已经引入了myBatis-plus-starter则无需再引入 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-annotation</artifactId>
        <version>3.5.4.1</version>
    </dependency>

</dependencies>
```

### 配置数据源

```yml

```

### 2. API使用

#### 创建超级表

1. 准备超级表映射的实体类
    1. 需要继承`TdBaseEntity.java`对象, 因为TDengine的表必须有Timestamp字段, 且被字段被@PrimaryTs注解标记
    2. 除了Integer类型的字段, 必须使用 @TdField 注解指定字段类型, 对于部分类型需要指定长度
    3. 对于Tag字段, 必须使用 @TdTag 注解进行标注
    4. 表别名别名优先使用MP的@TableName注解value值,其次是类名称的下划线格式
    5. 字段别名优先使用MP的@TableField注解的value值, 其次是字段名称的下划线格式

   ```java
   import com.baomidou.mybatisplus.annotation.TableField;
   import com.baomidou.mybatisplus.annotation.TableName;
   import com.kalus.tdengineorm.annotation.TdField;
   import com.kalus.tdengineorm.annotation.TdTag;
   import com.kalus.tdengineorm.entity.TdBaseEntity;
   import com.kalus.tdengineorm.enums.TdFieldTypeEnum;
   import lombok.Data;
   import lombok.EqualsAndHashCode;
   
   import java.sql.Timestamp;
   
   @Data
   @TableName("td_orm_test")
   @EqualsAndHashCode(callSuper = true)
   public class TestTdEntity extends TdBaseEntity {
       /**
        * 使用 @TdField 注解指定字段类型以及长度, 未指定时默认为INT类型
        */
       @TdField(type = TdFieldTypeEnum.NCHAR, length = 20)
       private String name;
       private Integer age;
       @TdField(type = TdFieldTypeEnum.DOUBLE)
       private Double db1;
       @TdField(type = TdFieldTypeEnum.DOUBLE)
       private Double db2;
       @TdField(type = TdFieldTypeEnum.FLOAT)
       private Float fl1;
       @TdField(type = TdFieldTypeEnum.BIGINT)
       private Long id;
       @TdField(type = TdFieldTypeEnum.TIMESTAMP)
       private Timestamp createTime;
       /**
        * 使用MP的 @TableField 注解来指定字段别名
        */
       @TableField("a_b_cd_efgg_a")
       @TdField(type = TdFieldTypeEnum.NCHAR, length = 20)
       private String aBCdEfggA;
       /**
        * 使用 @TdTag 注解标记字段为Tag字段
        */
       @TdTag
       @TdField(type = TdFieldTypeEnum.NCHAR, length = 20)
       private String tg1;
   
       @TdTag
       @TdField(type = TdFieldTypeEnum.NCHAR, length = 20)
       private Integer tg2;
   }
   ```

2. 执行Api

```java

import com.kalus.tdengineorm.mapper.TDengineMapper;
import com.kalus.tdengineorm.strategy.DateDynamicNameStrategy;

import javax.annotation.Resource;
import java.sql.Timestamp;

@Resource
private TDengineMapper tDengineMapper;

/**
 * 创建超级表
 */
public void createStableTable() {
    int result = tDengineMapper.createStableTable(TestTdEntity.class);
}

/**
 * 插入数据并同时创建子表
 */
public void insertUsing() {
    // 创建超级表对应的实体类, 需要继承TdBaseEntity, 并且每个字段使用相关注解进行标记
    TestTdEntity testTdEntity = new TestTdEntity();
    // 随便赋值一些数据
    testTdEntity.setTs(new Timestamp(System.currentTimeMillis()));
    testTdEntity.setId(99999999L);
    testTdEntity.setTg2(33);

    //  由于TestTdEntity对应的是超级表的实体类, 而子表和超级表的结构仅在于表名的区别, 所以需要自定义一个名称策略用来指定表名, 这里用DateDynamicNameStrategy来作为例子
    int result = tDengineMapper.insertUsing(testTdEntity, new DateDynamicNameStrategy());
}

/**
 * 向指定的子表插入单条数据
 */
public void insert() {
    // 创建超级表对应的实体类, 需要继承TdBaseEntity, 并且每个字段使用相关注解进行标记
    TestTdEntity testTdEntity = new TestTdEntity();
    // 随便赋值一些数据
    testTdEntity.setTs(new Timestamp(System.currentTimeMillis()));
    testTdEntity.setId(777777777L);
    testTdEntity.setTg2(123);

    //  由于TestTdEntity对应的是超级表的实体类, 而子表和超级表的结构仅在于表名的区别, 所以需要自定义一个名称策略用来指定表名, 这里用DateDynamicNameStrategy来作为例子
    int result = tDengineMapper.insert(testTdEntity, new DateDynamicNameStrategy());
}


```
