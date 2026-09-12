package com.xb.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 教学项目启动类。
 *
 * <p>{@link SpringBootApplication} 是一个组合注解，主要完成三件事：</p>
 * <ol>
 *     <li>把当前类声明为配置类；</li>
 *     <li>启用 Spring Boot 自动配置，例如自动创建数据源和 Web 容器；</li>
 *     <li>从当前包向下扫描 Controller、Service、Configuration 等组件。</li>
 * </ol>
 *
 * <p>{@link MapperScan} 会扫描 mapper 包，并为每个 Mapper 接口创建动态代理对象。
 * 因此 UserMapper 虽然没有手写实现类，仍然可以被 Spring 注入并调用。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
@MapperScan("com.xb.mybatisplus.mapper")
@SpringBootApplication
public class MybatisPlusDemoApplication {

    public static void main(String[] args) {
        // run 会创建 Spring 容器、完成自动配置，最后启动内嵌 Web 服务器。
        SpringApplication.run(MybatisPlusDemoApplication.class, args);
    }
}
