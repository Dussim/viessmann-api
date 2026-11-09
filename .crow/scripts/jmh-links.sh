#!/bin/sh
# Prints JMH Visualizer links for this run's results, compared with the last run and the last master run.
set -eu

bucket_url="https://s3.dussim.xyz/jmh-results"
# One host letter is percent-encoded so Crow's secret masking does not corrupt printed links.
link_bucket_url="https://s3.%64ussim.xyz/jmh-results"
visualizer_url="https://jmh.morethan.io/"
separator="================================================================"

run_label() {
  sha="${1#*-}"
  printf 'run #%s, %.7s' "${1%%-*}" "$sha"
}

read_latest() {
  curl -fsS --max-time 30 "$bucket_url/latest/$1/$2.key?run=$CI_PIPELINE_NUMBER" 2>/dev/null || true
}

print_comparison() {
  title="$1"
  previous_key="$2"
  if [ -n "$previous_key" ]; then
    echo
    echo "New vs $title ($(run_label "$previous_key")):"
    echo "  $visualizer_url?sources=$link_bucket_url/$previous_key,$link_bucket_url/$key"
  fi
}

cd api/feature/benchmark/build/reports

for file in $(find . -name results.json | sort); do
  file="${file#./}"
  suite="$(echo "$file" | sed -E 's|^validation/([^/]+)/.*|\1|; s|/results.json$||')"
  key="$CI_PIPELINE_NUMBER-$CI_COMMIT_SHA/$file"

  case "$suite" in
    jmh) title="JMH benchmarks" ;;
    *) title="Stable validation: $suite" ;;
  esac

  echo
  echo "$separator"
  echo "  $title"
  echo "$separator"
  echo "New results ($(run_label "$key")):"
  echo "  $visualizer_url?source=$link_bucket_url/$key"

  print_comparison "last run" "$(read_latest any "$suite")"
  print_comparison "last master" "$(read_latest master "$suite")"
done
