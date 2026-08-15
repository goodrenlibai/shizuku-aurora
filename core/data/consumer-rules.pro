# Shizuku Aurora · data 模块对外 ProGuard 规则
# 保留通过反射加载的 server 入口（AuroraService 经 app_process 反射启动）
-keep class shizuku.aurora.server.AuroraService { *; }
-keep class rikka.shizuku.server.ShizukuService { *; }
