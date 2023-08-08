# auctioneer

## run
- Requires jdk 11
- ```mvnw clean install```

## docker - for running with prometheus
- ```mvnw clean install```
- ```docker build -t auctioneer:latest .```
- ```docker-compose up```
- Run the test in ```LoadTest.java```

### load test
- Make sure that the server is running separately - you can run the ```AuctioneerApplication.java``` from your IDE directly.
- Run the test in ```LoadTest.java```
- The test cleans up after itself.
- Metrics [here](http://localhost:9090/graph?g0.expr=spring_data_repository_invocations_seconds_max&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=5m&g1.expr=successful_bid_total&g1.tab=0&g1.stacked=0&g1.show_exemplars=0&g1.range_input=5m&g2.expr=failed_bid_total&g2.tab=0&g2.stacked=0&g2.show_exemplars=0&g2.range_input=5m&g3.expr=outdated_bid_total&g3.tab=0&g3.stacked=0&g3.show_exemplars=0&g3.range_input=15m&g4.expr=http_server_requests_seconds_max&g4.tab=0&g4.stacked=0&g4.show_exemplars=0&g4.range_input=1m)

Note: The above two endpoints are just a temporary hack.

### prometheus for visualizations - need to be refined
- ```http://localhost:9090/```

## bid
```curl
curl --location 'http://localhost:8080/auctioneer/v1/auctions/1/bid' \
--header 'Content-Type: application/json' \
--data '{
    "bidderId": 1,
    "amount": 160000
}'
```

## best way to understand is to run JUnits
- Run/Debug tests in ```AuctionControllerTest.java``` and observe the logs.
- Ignore ```HHH000010: On release of batch it still contained JDBC statements``` as this is expected from the failing transaction.

## logic
- The implementation uses [Optimistic](https://stackoverflow.com/a/58952004) locking to handle bid races.
- If a database transaction sees that any other thread(transaction) has updated the winning bid, it fails and can be handled appropriately to show a user-friendly message.
- However, before we consider the transaction as failed, we re-try it using ```@Retryable``` annotation.
- The retry is done to handle scenarios where the failing transaction or bid has the higher amount, which makes it a valid bid and can be placed in next attempt.

