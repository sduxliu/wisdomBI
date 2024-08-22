package com.xinxi.wisdomBI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Knife4j 接口文档配置
 * <a href="https://doc.xiaominfo.com/knife4j/documentation/get_start.html">...</a>
 * @author 蒲月理想
 */
@Configuration
@EnableSwagger2
@Profile({"dev", "test"})
public class Knife4jConfig {

//    http://localhost:8080/api/doc.html
    @Bean
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("wisdomBI接口文档")
                        .description("wisdomBI-backend")
                        .version("1.0")
                        .build())
                .select()
                // 指定 Controller 扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.xinxi.wisdomBI.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}

/*
Swagger 是一个API文档生成工具，它提供了多种注解来帮助我们编写更清晰、更易于理解的API文档。以下是一些常用的Swagger注解：

1. @Api：用于描述API接口的基本信息，如接口名称、描述、请求类型等。

```java
@Api(value = "用户管理", tags = "用户相关的操作")
@RestController
@RequestMapping("/users")
public class UserController {
    // ...
}
```

2. @ApiOperation：用于描述API接口的具体操作信息，如操作描述、请求参数、返回值等。

```java
@ApiOperation(value = "获取用户列表", notes = "获取所有用户的列表信息")
@GetMapping("/users")
public ResponseEntity<List<User>> getUsers() {
    // ...
}
```

3. @ApiParam：用于描述API接口中的参数信息。

```java
@ApiOperation(value = "创建用户", notes = "根据用户信息创建新用户")
@PostMapping("/users")
public ResponseEntity<User> createUser(@ApiParam(value = "用户信息", required = true) @RequestBody User user) {
    // ...
}
```

4. @ApiModel：用于描述API接口返回的数据模型。

```java
@ApiModel(description = "用户信息")
public class User {
    @ApiModelProperty(value = "用户ID")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    // ...
}
```

5. @ApiModelProperty：用于描述数据模型中的属性信息。

```java
@ApiModelProperty(value = "用户ID")
private Long id;
```

这些注解可以帮助我们更好地编写API文档，提高开发效率和代码可读性。在实际项目中，可以根据需要使用更多的Swagger注解来丰富API文档内容。

 */