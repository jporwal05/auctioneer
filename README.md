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

## logic
- The implementation uses [Optimistic](https://stackoverflow.com/a/58952004) locking to handle bid races.
- If a database transaction sees that any other thread(transaction) has updated the winning bid, it fails and can be handled appropriately to show a user-friendly message.
- However, before we consider the transaction as failed, we re-try it using ```@Retryable``` annotation.
- The retry is done to handle scenarios where the failing transaction or bid has the higher amount, which makes it a valid bid and can be placed in next attempt.

