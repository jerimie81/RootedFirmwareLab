#!/usr/bin/env bash
set -euo pipefail

# RootedFirmwareLab local bootstrap/build script.
# It sets Android + Gradle env vars, accepts SDK licenses,
# installs required components, and runs the first build.

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Keep Gradle and Android user state in writable user paths.
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
export ANDROID_USER_HOME="${ANDROID_USER_HOME:-$HOME/.android}"

# Use one SDK location consistently to avoid AGP path conflicts.
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

mkdir -p "$GRADLE_USER_HOME" "$ANDROID_USER_HOME" "$ANDROID_HOME"

# Expected packages for this project.
REQUIRED_PACKAGES=(
  "platform-tools"
  "platforms;android-35"
  "build-tools;35.0.0"
  "ndk;26.1.10909125"
  "cmake;3.22.1"
)

SDKMANAGER=""
if [[ -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]]; then
  SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
elif command -v sdkmanager >/dev/null 2>&1; then
  SDKMANAGER="$(command -v sdkmanager)"
else
  cat <<'ERR'
sdkmanager not found.

Install Android command-line tools, then re-run:
  1) Android Studio > SDK Manager > SDK Tools > Android SDK Command-line Tools (latest)
  2) Or install manually and place sdkmanager at:
     $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager
ERR
  exit 1
fi

echo "PROJECT_ROOT=$PROJECT_ROOT"
echo "GRADLE_USER_HOME=$GRADLE_USER_HOME"
echo "ANDROID_USER_HOME=$ANDROID_USER_HOME"
echo "ANDROID_HOME=$ANDROID_HOME"
echo "SDKMANAGER=$SDKMANAGER"

echo
echo "==> Accepting Android SDK licenses"
# Avoid pipefail false-negative when `yes` gets SIGPIPE after sdkmanager exits.
set +o pipefail
yes | "$SDKMANAGER" --sdk_root="$ANDROID_HOME" --licenses >/dev/null || true
set -o pipefail

echo
echo "==> Installing required SDK/NDK/CMake components"
"$SDKMANAGER" --sdk_root="$ANDROID_HOME" "${REQUIRED_PACKAGES[@]}"

echo
echo "==> Writing local.properties"
cat > "$PROJECT_ROOT/local.properties" <<EOF
sdk.dir=$ANDROID_HOME
EOF

echo
echo "==> Ensuring Gradle wrapper is executable"
chmod +x "$PROJECT_ROOT/gradlew"

echo
echo "==> Verifying wrapper"
(
  cd "$PROJECT_ROOT"
  ./gradlew --version
)

echo
echo "==> Running first build (:app:assembleDebug)"
(
  cd "$PROJECT_ROOT"
  ./gradlew :app:assembleDebug --stacktrace
)

echo
echo "Build complete."
