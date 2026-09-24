#!/usr/bin/env bash
# Idempotent Cloud Agent setup for the kv4p-ht repository.
# Prepares the two build toolchains used by this project:
#   * JDK 17 + Android SDK  -> android-src/KV4PHT (Gradle app)
#   * Python + PlatformIO   -> microcontroller-src (ESP32 firmware & native tests)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$SCRIPT_DIR")"

ANDROID_HOME="$HOME/android-sdk"
CMDLINE_TOOLS="$ANDROID_HOME/cmdline-tools/latest"
CMDLINE_TOOLS_ZIP_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
JAVA17_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
PIO_VENV="$HOME/.platformio-venv"

echo "==> Installing system packages (JDK 17, build tools)"
if ! dpkg -s openjdk-17-jdk-headless >/dev/null 2>&1; then
  sudo apt-get update -qq
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless build-essential curl unzip python3-venv
fi

export JAVA_HOME="$JAVA17_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

echo "==> Installing Android SDK command-line tools"
if [ ! -x "$CMDLINE_TOOLS/bin/sdkmanager" ]; then
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  tmpzip="$(mktemp --suffix=.zip)"
  curl -fsSL "$CMDLINE_TOOLS_ZIP_URL" -o "$tmpzip"
  rm -rf "$ANDROID_HOME/cmdline-tools/latest" "$ANDROID_HOME/cmdline-tools/cmdline-tools"
  unzip -q "$tmpzip" -d "$ANDROID_HOME/cmdline-tools"
  mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$CMDLINE_TOOLS"
  rm -f "$tmpzip"
fi

export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$CMDLINE_TOOLS/bin:$ANDROID_HOME/platform-tools:$PATH"

echo "==> Accepting licenses and installing Android SDK packages (compileSdk 36)"
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager --install "platform-tools" "platforms;android-36" "build-tools;36.0.0" >/dev/null

echo "==> Pointing Gradle at the Android SDK (local.properties)"
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > "$REPO_ROOT/android-src/KV4PHT/local.properties"

echo "==> Installing PlatformIO 6.1.18 into a virtualenv"
if [ ! -x "$PIO_VENV/bin/pio" ]; then
  python3 -m venv "$PIO_VENV"
  "$PIO_VENV/bin/pip" install --quiet --upgrade pip
  "$PIO_VENV/bin/pip" install --quiet "platformio==6.1.18"
fi
export PATH="$PIO_VENV/bin:$PATH"

echo "==> Warming PlatformIO platforms/libraries (firmware + native-test deps)"
# The native-tests build depends on headers pulled into .pio/libdeps/esp32dev
# (e.g. esp32-afsk), so fetch every environment's packages up front. This is a
# fast no-op once the packages are cached in ~/.platformio.
( cd "$REPO_ROOT/microcontroller-src" && pio pkg install )

echo "==> Persisting toolchain environment for interactive shells"
ENV_START="# >>> kv4p-ht cloud agent env >>>"
ENV_END="# <<< kv4p-ht cloud agent env <<<"
if ! grep -qF "$ENV_START" "$HOME/.bashrc" 2>/dev/null; then
  {
    echo "$ENV_START"
    echo "export JAVA_HOME=\"$JAVA17_HOME\""
    echo "export ANDROID_HOME=\"$ANDROID_HOME\""
    echo "export ANDROID_SDK_ROOT=\"$ANDROID_HOME\""
    echo "export PATH=\"\$JAVA_HOME/bin:$CMDLINE_TOOLS/bin:$ANDROID_HOME/platform-tools:$PIO_VENV/bin:\$PATH\""
    echo "$ENV_END"
  } >> "$HOME/.bashrc"
fi

echo "==> Setup complete"
echo "    java  : $(java -version 2>&1 | head -1)"
echo "    pio   : $("$PIO_VENV/bin/pio" --version)"
echo "    sdk   : $ANDROID_HOME"
