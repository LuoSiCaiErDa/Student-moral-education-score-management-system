#!/usr/bin/env bash
set -euo pipefail

APP_NAME="moral-education-score-management-system"
JAR_NAME="${APP_NAME}-0.0.1-SNAPSHOT.jar"
INSTALL_DIR="/opt/${APP_NAME}"
SERVICE_NAME="${APP_NAME}.service"
CONFIG_NAME="application-ubuntu.yml"

echo "============================================"
echo "一键部署：${APP_NAME} 到 Ubuntu 服务器"
echo "============================================"

if ! command -v java >/dev/null 2>&1; then
  echo "错误：未检测到 Java，请先安装 JDK 17 或更高版本。"
  exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "检测到 Java 版本：${JAVA_VERSION}"

if [[ ! "${JAVA_VERSION}" =~ 17 ]]; then
  echo "警告：建议使用 JDK 17 运行该应用。"
fi

if [[ ! -f "target/${JAR_NAME}" ]]; then
  echo "错误：找不到打包文件 target/${JAR_NAME}。请先执行 mvn clean package -DskipTests。"
  exit 1
fi

sudo mkdir -p "${INSTALL_DIR}"
sudo chown "$(id -u):$(id -g)" "${INSTALL_DIR}"
cp "target/${JAR_NAME}" "${INSTALL_DIR}/"

cat > "${INSTALL_DIR}/${CONFIG_NAME}" <<'EOF'
spring:
  datasource:
    url: jdbc:h2:mem:moral_education
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
server:
  port: 3030
EOF

sudo tee "/etc/systemd/system/${SERVICE_NAME}" > /dev/null <<EOF
[Unit]
Description=学生德育分管理系统
After=network.target

[Service]
User=$(whoami)
WorkingDirectory=${INSTALL_DIR}
ExecStart=/usr/bin/java -jar ${INSTALL_DIR}/${JAR_NAME} --spring.config.location=${INSTALL_DIR}/${CONFIG_NAME}
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable "${SERVICE_NAME}"
sudo systemctl restart "${SERVICE_NAME}"

echo "部署完成。应用正在运行。"
echo "HTTP 访问地址: http://localhost:3030/"

echo "查看服务状态：sudo systemctl status ${SERVICE_NAME} --no-pager"
