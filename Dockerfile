# syntax=docker/dockerfile:1

# ============================================================
 crm-api 运行镜像（jar 版）
# 用法: 本地打包 jar 后，把 jar 重命名为 crm-api.jar 放本目录再构建
# 构建命令: docker compose up -d --build
# ============================================================
FROM eclipse-temurin:21-jre

# curl 用于 Docker HEALTHCHECK 健康检查
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 安全基线：非 root 运行
RUN useradd -r -u 1001 appuser

WORKDIR /app
COPY crm-api.jar /app/app.jar

USER appuser
EXPOSE 8090

# 优雅停机 + 健康检查（start-period 覆盖启动/迁移时间）
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://127.0.0.1:8090/api/auth/health || exit 1

ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/app.jar"]
