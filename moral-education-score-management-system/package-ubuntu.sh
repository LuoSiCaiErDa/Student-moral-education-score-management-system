#!/usr/bin/env bash
set -euo pipefail

APP_NAME="moral-education-score-management-system"
JAR_NAME="${APP_NAME}-0.0.1-SNAPSHOT.jar"
TAR_NAME="${APP_NAME}-ubuntu-deploy.tar.gz"

mvn clean package -DskipTests

echo "打包文件生成成功: target/${JAR_NAME}"

tar -czvf "${TAR_NAME}" \
  "target/${JAR_NAME}" \
  "deploy-ubuntu.sh" \
  "ubuntu-service.template" \
  "README.md"

echo "生成部署包：${TAR_NAME}"