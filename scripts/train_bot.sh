#!/bin/bash
echo 'Start script...'

pwd
. constants.ini

# POST request to merge training yaml files
train_yaml=$(curl -X POST -H "Content-Type: application/json" -d '{"file_path":"'$TRAINING_FILES_PATH'"}' "$TRAINING_DMAPPER/mergeYaml")

checksum=$(curl -X POST -H "Content-Type: text/plain" -d "$train_yaml" "$TRAINING_DMAPPER/utils/calculate-sha256-checksum")

resql_response=$(curl -X POST -H "Content-Type: application/json" "$TRAINING_RESQL/get-latest-ready-model")
if [ "$resql_response" != [] ]; then
    training_data_checksum=$(echo "$resql_response" | grep -o '"trainingDataChecksum":"[^"]*' | grep -o '[^"]*$')
fi

if [ "$training_data_checksum" == "$checksum" ]; then
    echo "Model already trained with the same data"
    curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-already-trained"
    exit 1
fi

curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-processing"

# POST request to train model in RASA
train_response=$(curl -s -X POST -D - -d "$train_yaml" "$TRAINING_RASA/model/train?force_training=true")
train_status=$(echo "$train_response" | grep -oP "HTTP/\d\.\d \K\d+")
trained_model_filename=$(echo "$train_response" | grep -i "^filename:" | sed 's/^filename: //i')
trained_model_filename=$(echo "$trained_model_filename" | tr -d '\r')

if [ "$train_status" != "200" ]; then
    echo "Model training failed with status code $train_status"
    curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-error"
    exit 1
fi

# PUT request to load currently trained model in RASA
load_status=$(curl -s -w "%{http_code}" -X PUT -H "Content-Type: application/json" -d '{"model_file":"/app/models/'$trained_model_filename'"}' "$TRAINING_RASA/model")
if [ "$load_status" != "204" ]; then
    echo "Model loading failed with status code $load_status"
    curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-error"
    exit 1
fi

curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-testing"

# POST request to merge testing yaml files
test_yaml=$(curl -X POST -H "Content-Type: application/json" -d '{"file_path":"'$TESTING_FILES_PATH'"}' "$TRAINING_DMAPPER/mergeYaml")

# POST request to test model in RASA
test_response=$(curl -s -w "%{http_code}" -X POST -d "$test_yaml" "$TRAINING_RASA/model/test/stories")
test_status="${test_response: -3}"
if [ "$test_status" != "200" ]; then
    echo "Model testing failed with status code $test_status"
    curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-error"
    exit 1
fi
test_body="${test_response:: -3}"

curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-cross-validating"

# POST request to merge cross validating yaml files
cross_validate_yaml=$(curl -X POST -H "Content-Type: application/json" -d '{"file_path":"'$CROSS_VALIDATION_FILES_PATH'"}' "$TRAINING_DMAPPER/mergeYaml")

# POST request to cross validate model in RASA
cross_validate_response=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/x-yaml" -d "$cross_validate_yaml" "$TRAINING_RASA/model/test/intents?cross_validation_folds=2")
cross_validate_status="${cross_validate_response: -3}"
if [ "$cross_validate_status" != "200" ]; then
    echo "Model cross validating failed with status code $cross_validate_status"
    curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-error"
    exit 1
fi
cross_validate_body="${cross_validate_response:: -3}"

copy_file_body_dto='{"destinationFilePath":"'$trained_model_filename'","destinationStorageType":"S3","sourceFilePath":"'$trained_model_filename'","sourceStorageType":"FS"}'
copy_file_response=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "$copy_file_body_dto" "$S3_FERRY/v1/files/copy")
copy_file_status="${copy_file_response: -3}"
if [ "$copy_file_status" != "201" ]; then
    echo "Copying file from local to remote storage failed with status code $copy_file_status"
    curl -H "x-ruuter-skip-authentication: true" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-error"
    exit 1
fi

add_new_model_body_dto='{"fileName":"'$trained_model_filename'","testReport":'$test_body',"crossValidationReport":'$cross_validate_body',"trainingDataChecksum":"'$checksum'"}'
curl -X POST -H "x-ruuter-skip-authentication: true" -H "Content-Type: application/json" -d "$add_new_model_body_dto" "$TRAINING_PUBLIC_RUUTER/rasa/model/add-new-model-ready"

rm /data/$trained_model_filename
