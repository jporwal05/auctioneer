# auctioneer

## docker
- ```mvn clean install```
- ```docker build -t jporwal05/auctioneer:latest .```
- ```docker push jporwal05/auctioneer:latest```
- ```docker-compose up```

## prometheus
- ```http://localhost:9090/```

## bid
```curl
curl --location 'http://localhost:8080/auctioneer/v1/bids' \
--header 'Content-Type: application/json' \
--data '{
    "auctionId": 1,
    "bidderId": 1,
    "amount": 160000
}'
```
