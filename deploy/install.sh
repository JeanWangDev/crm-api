#!/usr/bin/env bash
# ============================================================
# crm-api 服务器端一键安装脚本
# 用法（在服务器上执行）:
#   1. 把 crm-api.jar 和本脚本、crm-api.service 放在同一目录
#   2. sudo bash install.sh
# ============================================================
set -euo pipefail

APP_NAME=crm-api
APP_DIR=/opt/crm
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "==> 1/5 检查 JDK 21"
if ! command -v java >/dev/null 2>&1; then
  echo "错误: 未安装 JDK。请先安装后重试:"
  echo "  Ubuntu:   sudo apt update && sudo apt install -y openjdk-21-jdk-headless"
  echo "  CentOS:   sudo yum install -y java-21-openjdk-headless"
  exit 1
fi
java -version

echo "==> 2/5 部署 jar"
if [ ! -f "$SCRIPT_DIR/crm-api.jar" ]; then
  echo "错误: 未找到 $SCRIPT_DIR/crm-api.jar，请先从本地打包并上传:"
  echo "  ./mvnw clean package -DskipTests"
  echo "  scp target/crm-api-0.1.0-SNAPSHOT.jar root@<服务器IP>:$SCRIPT_DIR/crm-api.jar"
  exit 1
fi
sudo mkdir -p "$APP_DIR"
sudo cp "$SCRIPT_DIR/crm-api.jar" "$APP_DIR/$APP_NAME.jar"

echo "==> 3/5 环境变量"
if [ ! -f "$APP_DIR/crm.env" ]; then
  sudo cp "$SCRIPT_DIR/crm.env.example" "$APP_DIR/crm.env"
  echo "已生成 $APP_DIR/crm.env，请编辑填入真实数据库信息和 JWT 密钥:"
  echo "  sudo nano $APP_DIR/crm.env"
  echo "  JWT 密钥生成命令: openssl rand -hex 32"
  echo "编辑完成后重新执行: sudo bash install.sh"
  exit 0
fi
echo "crm.env 已存在，跳过（如需修改: sudo nano $APP_DIR/crm.env）"

echo "==> 4/5 安装 systemd 服务"
sudo cp "$SCRIPT_DIR/crm-api.service" /etc/systemd/system/$APP_NAME.service
sudo systemctl daemon-reload
sudo systemctl enable $APP_NAME >/dev/null 2>&1

echo "==> 5/5 启动服务"
sudo systemctl restart $APP_NAME
sleep 3
systemctl status $APP_NAME --no-pager || true
echo
echo "================================================"
echo " 健康检查:   curl http://<服务器IP>:8090/api/auth/health"
echo " 实时日志:   journalctl -u crm-api -f"
echo " 重启服务:   sudo systemctl restart crm-api"
echo "================================================"
