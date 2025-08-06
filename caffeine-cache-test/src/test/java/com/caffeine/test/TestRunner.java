package com.caffeine.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {
    public static void main(String[] args) {
        // 运行缓存配置动态更新测试
        Result result = JUnitCore.runClasses(CacheConfigDynamicUpdateTest.class, CacheConfigIntegrationTest.class);

        // 输出测试结果
        System.out.println("测试总数: " + result.getRunCount());
        System.out.println("失败测试数: " + result.getFailureCount());
        System.out.println("测试是否成功: " + result.wasSuccessful());

        // 输出失败的测试详细信息
        for (Failure failure : result.getFailures()) {
            System.out.println("失败测试: " + failure.getTestHeader());
            System.out.println("失败原因: " + failure.getMessage());
            System.out.println("堆栈跟踪: " + failure.getTrace());
        }
    }
}