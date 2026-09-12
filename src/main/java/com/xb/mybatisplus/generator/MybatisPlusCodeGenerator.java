package com.xb.mybatisplus.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * 代码生成示例（独立 main 方法，不会随 Web 应用自动执行）。
 * 生成物直接写入项目的 src/main/java 和 src/main/resources/mapper，
 * 在 IDEA 中刷新后即可看到。
 *
 * <p>生成器读取的是数据库元数据，例如表名、列名、主键和字段类型；它只负责生成
 * 机械化代码骨架，业务规则、参数校验、事务和权限仍然需要开发者设计。</p>
 *
 * <p>注意：本项目已经手写了 User 相关教学代码，若生成同名的实体/Mapper/Service，
 * 会直接覆盖手写内容，练习时建议用 -Dtable.names 指定新表，或先备份。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
public final class MybatisPlusCodeGenerator {

    // 工具类不需要创建对象，私有构造器可防止被误实例化。
    private MybatisPlusCodeGenerator() {
    }

    public static void main(String[] args) {
        // JVM 参数优先，方便临时执行；其次读取环境变量，最后使用教学默认值。
        String url = setting("db.url", "DB_URL", "jdbc:mysql://xxx:3306/xxx?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true");
        String username = setting("db.username", "DB_USERNAME", "xxx");
        String password = setting("db.password", "DB_PASSWORD", "xxx");

        // 表名优先取配置（-Dtable.names 或环境变量 TABLE_NAMES），没有配置时在控制台询问用户。
        List<String> tables = resolveTables(setting("table.names", "TABLE_NAMES", null));

        // 所有生成物直接写入项目源码目录，IDEA 项目树中即可看到。
        String javaOutput = Paths.get(System.getProperty("user.dir"), "src", "main", "java").toString();
        String xmlOutput = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "mapper").toString();

        // FastAutoGenerator 使用 Builder 风格：每一段配置只负责一种关注点。
        FastAutoGenerator.create(url, username, password)
                // 全局配置：控制作者、日期、Java 文件输出目录等项目级选项。
                .globalConfig(builder -> builder
                        // 生成文件中的作者和日期按教学要求固定，保证每次生成结果一致。
                        .author("xb")
                        // 生成结束后不自动打开文件管理器，适合命令行和 CI 环境。
                        .disableOpenDir()
                        .commentDate(() -> new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()))
                        .outputDir(javaOutput))
                // 包配置：生成类的基础包，以及 XML 这类非 Java 文件的输出位置。
                .packageConfig(builder -> builder
                        .parent("com.xb.mybatisplus")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, xmlOutput)))
                // 策略配置：决定处理哪些表，以及各种代码模板采用什么风格。
                .strategyConfig(builder -> builder
                        // 只生成用户指定的表，避免误把同一数据库中的业务表全部生成出来。
                        .addInclude(tables)
                        // 表名以 demo_ 开头时去掉前缀，例如 demo_user 的实体类名称会变为 User。
                        .addTablePrefix("demo_")
                        // enableFileOverride：同名文件直接覆盖。现在生成到源码目录，
                        // 再次生成可以刷新结果，但也会覆盖已有的手写代码。
                        .entityBuilder().enableFileOverride()
                        .mapperBuilder().enableFileOverride()
                        .serviceBuilder().enableFileOverride()
                        .controllerBuilder().enableRestStyle().enableFileOverride())
                // 生成器负责准备数据模型，Freemarker 负责把模型渲染成源文件。
                .templateEngine(new FreemarkerTemplateEngine())
                // 前面的 Builder 只是在收集配置，execute 才会真正连接数据库并写文件。
                .execute();
    }

    /**
     * 解析要生成的表名：配置优先，否则在控制台提示用户输入。
     * 支持逗号分隔的多个表名，例如“demo_user,orders”。
     */
    private static List<String> resolveTables(String tableNames) {
        if (tableNames != null && !tableNames.isBlank()) {
            return splitTables(tableNames);
        }
        System.out.print("请输入要生成的表名（多个表用英文逗号分隔）：");
        try (Scanner scanner = new Scanner(System.in)) {
            String input = scanner.nextLine().trim();
            while (input.isBlank()) {
                System.out.print("表名不能为空，请重新输入：");
                input = scanner.nextLine().trim();
            }
            return splitTables(input);
        }
    }

    /**
     * 把逗号分隔的字符串拆成表名列表，自动去掉两端的空白和空项。
     */
    private static List<String> splitTables(String tableNames) {
        List<String> tables = new ArrayList<>();
        for (String name : tableNames.split(",")) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                tables.add(trimmed);
            }
        }
        return tables;
    }

    /**
     * 按“JVM 系统属性 -> 环境变量 -> 教学默认值”的优先级读取配置。
     * 这样既方便命令行临时覆盖，也避免在真实项目里把密码硬编码到启动命令。
     */
    private static String setting(String propertyName, String environmentName, String defaultValue) {
        String property = System.getProperty(propertyName);
        if (property != null) {
            return property;
        }
        String value = System.getenv(environmentName);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
