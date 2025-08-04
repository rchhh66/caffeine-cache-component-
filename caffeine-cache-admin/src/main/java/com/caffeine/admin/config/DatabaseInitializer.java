package com.caffeine.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 数据库初始化器，用于在应用启动时初始化数据库
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 创建数据库表
        executeSqlScript("db/schema.sql");

        // 插入初始数据
        // executeSqlScript("db/data.sql");
        System.out.println("数据库初始化完成");
    }

    /**
     * 执行SQL脚本文件
     */
    private void executeSqlScript(String scriptPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        try (InputStream inputStream = resource.getInputStream()) {
            String sql = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
            // 分割SQL语句（简单处理，假设SQL语句以;结尾）
            String[] sqlStatements = sql.split(";");
            for (String sqlStatement : sqlStatements) {
                String trimmedSql = sqlStatement.trim();
                if (!trimmedSql.isEmpty()) {
                    jdbcTemplate.execute(trimmedSql);
                }
            }
        }
    }
}