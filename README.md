# Bass Analyzer

## Start locally
```bash
docker compose -f docker-compose.local.yml up -d

# create queues
docker exec -it $(docker ps -qf name=localstack) awslocal sqs create-queue --queue-name analysis-request
docker exec -it $(docker ps -qf name=localstack) awslocal sqs create-queue --queue-name analysis-result
```

## Upload and check
```bash
curl -F "file=@./test67bpm.m4a" http://localhost:8080/api/recordings
# -> { jobId: ... }
curl http://localhost:8080/api/jobs/{jobId}
```
