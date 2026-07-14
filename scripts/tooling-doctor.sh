#!/usr/bin/env bash
set -u

status=0

check() {
  if path=$(command -v "$1"); then
    version=$("$1" --version 2>&1 | head -n 1)
    printf '%-10s %s (%s)\n' "$1" "$version" "$path"
  else
    printf '%-10s missing\n' "$1"
    status=1
  fi
}

for command in node python3 uv graphify gsd; do
  check "$command"
done

if command -v node >/dev/null && ! node -e 'if (Number(process.versions.node.split(".")[0]) < 22) process.exit(1)'; then
  echo 'Node.js 22 or newer is required.'
  status=1
fi

if command -v python3 >/dev/null && ! python3 -c 'import sys; raise SystemExit(sys.version_info < (3, 10))'; then
  echo 'Python 3.10 or newer is required.'
  status=1
fi

exit "$status"
