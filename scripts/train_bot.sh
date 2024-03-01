#!/bin/bash
echo 'hi'
#This path should lead to root location for Training-Module
cd ../Training-Module
pwd
docker-compose -f docker-compose-bot.yml up
echo 'Training is completed';
echo 'Making request to populate new data to database';
#This curl should have a proper base url of ruuter in training-module
curl 'http://localhost:8080/rasa/model/add-new-model'
echo 'Process has been completed.';