#!/bin/sh
#
# SPDX-License-Identifier: Apache-2.0
#
# Copyright 2020 Agorapulse.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


if [ "$#" -ne 1 ]
then
  echo "Usage: ./execute.sh script_file [host] [mimetype]"
  exit 1
fi

script_file=$1

default_host="http://localhost:8080"
host=${2:-$default_host}

extension="${1##*.}"
default_mimetype="text/$extension"
mimetype=${3:-$default_mimetype}

curl -sS -X POST -H 'Content-Type: application/json' -d @credentials.json -o token.json "$host/login"
curl -X POST -H "Content-Type: $mimetype" -H "Authorization: Bearer $(jq -r .access_token token.json)" --data-binary @"$script_file" "$host/console/execute/result"
rm token.json
echo

