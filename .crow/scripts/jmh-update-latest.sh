#!/bin/sh
# Records this run's result keys as the latest baselines. Only runs after every benchmark step passed.
set -eu

bucket_url="https://s3.dussim.xyz/jmh-results"

put_latest() {
  curl --fail-with-body -sS --max-time 60 -X PUT \
    --aws-sigv4 "aws:amz:us-east-1:s3" \
    --user "$S3_ACCESS_KEY:$S3_SECRET_KEY" \
    -H "x-amz-content-sha256: UNSIGNED-PAYLOAD" \
    -H "Content-Type: text/plain" \
    --data-binary "$3" \
    "$bucket_url/latest/$1/$2.key"
}

cd api/feature/benchmark/build/reports

for file in $(find . -name results.json | sort); do
  file="${file#./}"
  suite="$(echo "$file" | sed -E 's|^validation/([^/]+)/.*|\1|; s|/results.json$||')"
  key="$CI_PIPELINE_NUMBER-$CI_COMMIT_SHA/$file"

  put_latest any "$suite" "$key"
  if [ "$CI_COMMIT_BRANCH" = "master" ]; then
    put_latest master "$suite" "$key"
  fi
  echo "Recorded $suite as latest"
done
