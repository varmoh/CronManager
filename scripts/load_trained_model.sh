#!/bin/bash
echo 'Start script...'

pwd
. constants.ini

if [ -z "$versionNumber" ]; then
    resql_response=$(curl -X POST -H "Content-Type: application/json" "$TRAINING_RESQL/get-latest-ready-model")
else
    resql_response=$(curl -X POST -H "Content-Type: application/json" -d '{"versionNumber":"'$versionNumber'"}' "$TRAINING_RESQL/get-ready-model-by-version-number")
fi

if [ "$resql_response" == [] ]; then
    echo "error: specified model not found in the database"
    exit 1
fi

filename=$(echo "$resql_response" | grep -o '"fileName":"[^"]*' | grep -o '[^"]*$')
model_type=$(echo "$resql_response" | grep -o '"modelType":"[^"]*' | grep -o '[^"]*$')
version_number=$(echo "$resql_response" | grep -o '"versionNumber":"[^"]*' | grep -o '[^"]*$')
model_version=$(echo "$resql_response" | grep -o '"modelVersion":"[^"]*' | grep -o '[^"]*$')

copy_file_body_dto='{"destinationFilePath":"'$filename'","destinationStorageType":"FS","sourceFilePath":"'$filename'","sourceStorageType":"S3"}'
copy_file_response=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "$copy_file_body_dto" "$S3_FERRY_LOAD/v1/files/copy")
copy_file_status="${copy_file_response: -3}"
if [ "$copy_file_status" != "201" ]; then
    echo "error: model copying from remote to local storage failed with status code $copy_file_status"
    exit 1
fi

load_status=$(curl -s -w "%{http_code}" -X PUT -H "Content-Type: application/json" -d '{"model_file":"/app/models/'$filename'"}' "$CHATBOT_BOT/model")
if [ "$load_status" != "204" ]; then
    echo "error: failed to load trained model from RASA with status code $load_status"
    exit 1
fi

add_deployed_model_body_dto='{"fileName":"'$filename'","modelType":"'$model_type'","versionNumber":"'$version_number'","modelVersion":"'$model_version'"}'
curl -X POST -H "x-ruuter-skip-authentication: true" -H "Content-Type: application/json" -d "$add_deployed_model_body_dto" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-deployed"

shopt -s extglob 
rm /data/!($filename)