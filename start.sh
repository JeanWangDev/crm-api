#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

PROFILE=""
EXTRA_ARGS=()

for arg in "$@"; do
  case "$arg" in
    tidb|dev|prod|h2|local|mysql)
      case "$arg" in
        local) PROFILE="h2" ;;
        mysql|dev) PROFILE="tidb" ;;
        *) PROFILE="$arg" ;;
      esac
      ;;
    *)
      EXTRA_ARGS+=("$arg")
      ;;
  esac
done

if [ -z "$PROFILE" ]; then
  PROFILE="${SPRING_PROFILES_ACTIVE:-tidb}"
fi

export SPRING_PROFILES_ACTIVE="$PROFILE"

if [ -f "./crm.local.env" ]; then
  set -a
  # shellcheck disable=SC1091
  source "./crm.local.env"
  set +a
  echo "已加载 ./crm.local.env"
fi

case "$PROFILE" in
  tidb)
    DEFAULT_PORT=8080
    echo "环境: tidb（端口 ${SERVER_PORT:-$DEFAULT_PORT}）"
    if [ -n "${CRM_DB_URL:-}" ]; then
      echo "数据库: ${CRM_DB_URL%%\?*}"
    fi
    missing=()
    for v in CRM_DB_URL CRM_DB_USER CRM_DB_PASSWORD; do
      if [ -z "${!v:-}" ]; then
        missing+=("$v")
      fi
    done
    if [ "${#missing[@]}" -gt 0 ]; then
      echo "错误: tidb 缺少环境变量: ${missing[*]}"
      echo "请复制 crm.local.env.example 为 crm.local.env 并填写连接信息"
      echo "  cp crm.local.env.example crm.local.env"
      exit 1
    fi
    ;;
  h2)
    DEFAULT_PORT=8080
    echo "环境: h2（本地文件库 ./data/crm，端口 ${SERVER_PORT:-$DEFAULT_PORT}）"
    ;;
  prod)
    DEFAULT_PORT=8090
    echo "环境: prod（端口 ${SERVER_PORT:-$DEFAULT_PORT}）"
    missing=()
    for v in CRM_DB_URL CRM_DB_USER CRM_DB_PASSWORD CRM_JWT_SECRET; do
      if [ -z "${!v:-}" ]; then
        missing+=("$v")
      fi
    done
    if [ "${#missing[@]}" -gt 0 ]; then
      echo "错误: prod 缺少环境变量: ${missing[*]}"
      echo "本地开发请改用: ./start.sh tidb"
      echo "生产示例:"
      echo "  export CRM_DB_URL='jdbc:mysql://host:4000/jdys-crm?sslMode=REQUIRED&serverTimezone=Asia/Shanghai'"
      echo "  export CRM_DB_USER=user"
      echo "  export CRM_DB_PASSWORD=password"
      echo "  export CRM_JWT_SECRET='your-32-byte-secret-key!!!!!!'"
      echo "  ./start.sh prod"
      exit 1
    fi
    ;;
  *)
    echo "未知环境: $PROFILE，支持 tidb | h2 | prod"
    exit 1
    ;;
esac

PORT="${SERVER_PORT:-$DEFAULT_PORT}"
if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "错误: 端口 $PORT 已被占用，Spring 无法启动。"
  echo "当前占用进程:"
  lsof -nP -iTCP:"$PORT" -sTCP:LISTEN || true
  echo
  echo "处理方式任选其一:"
  echo "  1) 若旧实例已够用，直接用即可（不必再启动）"
  echo "  2) 先停掉再启动:  lsof -tiTCP:$PORT -sTCP:LISTEN | xargs kill"
  echo "  3) 换端口启动:    SERVER_PORT=8081 ./start.sh $PROFILE"
  exit 1
fi

# macOS / Clash 常把系统代理指到 127.0.0.1:7890，后端数据库必须直连
BOOT_JVM_ARGS="-Djava.net.useSystemProxies=false -DsocksProxyHost= -Dhttp.proxyHost= -Dhttps.proxyHost="

if [ -x "./mvnw" ]; then
  MVN=(./mvnw)
else
  MVN=(mvn)
fi

CMD=(
  "${MVN[@]}"
  spring-boot:run
  -Dspring-boot.run.profiles="$PROFILE"
  -Dspring-boot.run.jvmArguments="$BOOT_JVM_ARGS"
)
if [ "${#EXTRA_ARGS[@]}" -gt 0 ]; then
  CMD+=("${EXTRA_ARGS[@]}")
fi
exec "${CMD[@]}"
