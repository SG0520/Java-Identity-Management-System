#!/bin/bash
dir=$(dirname "$0")
version=$(cat ${dir}/../buildNumber.txt)

aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin 619942913628.dkr.ecr.ap-south-1.amazonaws.com
docker pull 619942913628.dkr.ecr.ap-south-1.amazonaws.com/accelerator:identity-java-$version
docker run -d -p 8083:8080 --name identity-java --link redis:redis 619942913628.dkr.ecr.ap-south-1.amazonaws.com/accelerator:identity-java-$version
sleep 20