#!/bin/bash
newman run /postman/Integration_Tests_User_Service.postman_collection_17sep.json -r cli,json,htmlextra \
--reporter-json-export output/result.json --reporter-htmlextra-export output/result.html \
--env-var "baseUrl=http://host.testcontainers.internal:8086"
