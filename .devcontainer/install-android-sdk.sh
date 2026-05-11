#!/usr/bin/env bash
set -euo pipefail

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
CMDLINE_TOOLS_DIR="$ANDROID_SDK_ROOT/cmdline-tools/latest"

mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"

if [ ! -x "$CMDLINE_TOOLS_DIR/bin/sdkmanager" ]; then
  if ! command -v wget >/dev/null || ! command -v unzip >/dev/null; then
    if command -v sudo >/dev/null; then
      sudo apt-get update
      sudo apt-get install -y --no-install-recommends wget unzip ca-certificates
    else
      apt-get update
      apt-get install -y --no-install-recommends wget unzip ca-certificates
    fi
  fi
  wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/commandlinetools.zip
  unzip -q /tmp/commandlinetools.zip -d /tmp
  rm -rf "$CMDLINE_TOOLS_DIR"
  mv /tmp/cmdline-tools "$CMDLINE_TOOLS_DIR"
  rm -f /tmp/commandlinetools.zip
fi

export PATH="$CMDLINE_TOOLS_DIR/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

yes | sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses >/dev/null
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
  "platform-tools" \
  "platforms;android-31" \
  "build-tools;31.0.0"
