#!/usr/bin/env bash

set -euo pipefail

APP_PID=""
APP_PGID=""
APP_HAS_OWN_SESSION="false"
LOG_TAIL_PID=""
VERBOSE="false"
PROJECT_DIR=""

usage() {
  echo "Usage: $0 [--verbose] [project_dir]" >&2
  exit 22
}

for arg in "$@"; do
  case "$arg" in
    --verbose)
      VERBOSE="true"
      ;;
    -*)
      usage
      ;;
    *)
      if [ -z "$PROJECT_DIR" ]; then
        PROJECT_DIR="$arg"
      else
        usage
      fi
      ;;
  esac
done

PROJECT_DIR="${PROJECT_DIR:-$(pwd)}"
STARTUP_TIMEOUT_SECONDS="${STARTUP_TIMEOUT_SECONDS:-90}"
POLL_INTERVAL_SECONDS="${POLL_INTERVAL_SECONDS:-1}"
LOG_FILE="${LOG_FILE:-$(mktemp "${TMPDIR:-/tmp}/localdev-startup-smoke.XXXXXX")}"
MAVEN_CLI_OPTS="${MAVEN_CLI_OPTS:---batch-mode --fail-at-end}"
STARTUP_LOG_MARKER="Started Application in"

print_failure_log() {
  if [ -f "$LOG_FILE" ]; then
    cat "$LOG_FILE" >&2
  else
    echo "Log file not found: $LOG_FILE" >&2
  fi
}

stop_app_process() {
  if [ -n "$APP_PGID" ]; then
    kill -TERM -- "-$APP_PGID" >/dev/null 2>&1 || true
    sleep 1
    kill -KILL -- "-$APP_PGID" >/dev/null 2>&1 || true
    return
  fi

  if [ -n "$APP_PID" ]; then
    kill -TERM "$APP_PID" >/dev/null 2>&1 || true
    sleep 1
    kill -KILL "$APP_PID" >/dev/null 2>&1 || true
  fi
}

start_log_stream() {
  if [ "$VERBOSE" != "true" ]; then
    return
  fi

  tail -n +1 -f "$LOG_FILE" &
  LOG_TAIL_PID="$!"
}

stop_log_stream() {
  if [ -n "$LOG_TAIL_PID" ]; then
    kill "$LOG_TAIL_PID" >/dev/null 2>&1 || true
    wait "$LOG_TAIL_PID" 2>/dev/null || true
  fi
}

cleanup() {
  set +e
  stop_app_process
  stop_log_stream
}
trap cleanup EXIT

prepare_log_file() {
  mkdir -p "$(dirname "$LOG_FILE")"
  : > "$LOG_FILE"
}

unset_datasource_overrides() {
  unset SPRING_DATASOURCE_URL
  unset SPRING_DATASOURCE_USERNAME
  unset SPRING_DATASOURCE_PASSWORD
}

compile_application() {
  pushd "$PROJECT_DIR" >/dev/null
  if ! ./mvnw $MAVEN_CLI_OPTS -DskipTests test-compile >>"$LOG_FILE" 2>&1; then
    popd >/dev/null
    echo "Compilation step failed before startup run." >&2
    print_failure_log
    exit 1
  fi
  popd >/dev/null
}

start_application() {
  pushd "$PROJECT_DIR" >/dev/null
  if command -v setsid >/dev/null 2>&1; then
    setsid ./mvnw $MAVEN_CLI_OPTS -DskipTests -Dmaven.compiler.skip=true -Dmaven.test.skip=true spring-boot:run >"$LOG_FILE" 2>&1 &
    APP_HAS_OWN_SESSION="true"
  else
    ./mvnw $MAVEN_CLI_OPTS -DskipTests -Dmaven.compiler.skip=true -Dmaven.test.skip=true spring-boot:run >"$LOG_FILE" 2>&1 &
  fi
  APP_PID=$!
  if [ "$APP_HAS_OWN_SESSION" = "true" ]; then
    APP_PGID="$(ps -o pgid= "$APP_PID" | tr -d ' ' || true)"
  fi
  popd >/dev/null
}

wait_for_startup() {
  local elapsed_seconds=0
  while [ "$elapsed_seconds" -lt "$STARTUP_TIMEOUT_SECONDS" ]; do
    if [ -f "$LOG_FILE" ] && grep -q "$STARTUP_LOG_MARKER" "$LOG_FILE"; then
      echo "Startup smoke passed."
      exit 0
    fi

    if ! kill -0 "$APP_PID" >/dev/null 2>&1; then
      echo "App process exited before startup marker." >&2
      print_failure_log
      exit 1
    fi

    sleep "$POLL_INTERVAL_SECONDS"
    elapsed_seconds=$((elapsed_seconds + POLL_INTERVAL_SECONDS))
  done

  echo "Startup smoke timed out after ${STARTUP_TIMEOUT_SECONDS}s." >&2
  print_failure_log
  exit 1
}

prepare_log_file
start_log_stream
unset_datasource_overrides
compile_application
start_application
wait_for_startup
