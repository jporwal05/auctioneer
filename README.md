# auctioneer

## run
- Requires jdk 11
- ```mvnw clean install```

## docker (for author's use for now)
- ```mvnw clean install```
- ```docker build -t jporwal05/auctioneer:latest .```
- ```docker push jporwal05/auctioneer:latest```
- ```docker-compose up```

### prometheus
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

## best way to understand is to run JUnits
- Run/Debug tests in ```BidControllerTest.java``` and observe the logs.
- Ignore ```HHH000010: On release of batch it still contained JDBC statements``` as this is expected from the failing transaction.

