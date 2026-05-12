#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
KEYSTORE_DIR="$ROOT_DIR/keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/uav_defense_release.jks"
ALIAS="uav_defense"
STOREPASS="changeit123"
KEYPASS="changeit123"
DNAME="CN=UavDefense, OU=Android, O=UavDefense, L=Shenzhen, ST=Guangdong, C=CN"
VALIDITY=36500

mkdir -p "$KEYSTORE_DIR"

if [ -f "$KEYSTORE_FILE" ]; then
  echo "Keystore already exists: $KEYSTORE_FILE"
  exit 0
fi

keytool -genkeypair \
  -v \
  -storetype JKS \
  -keystore "$KEYSTORE_FILE" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$VALIDITY" \
  -storepass "$STOREPASS" \
  -keypass "$KEYPASS" \
  -dname "$DNAME"

echo "Generated keystore: $KEYSTORE_FILE"
echo "alias=$ALIAS"
echo "storePassword=$STOREPASS"
echo "keyPassword=$KEYPASS"
