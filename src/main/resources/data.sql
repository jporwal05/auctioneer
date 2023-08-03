-- COMPANY
INSERT INTO company (name, created_time, updated_time) VALUES ('Apple', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO company (name, created_time, updated_time) VALUES ('Google', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO company (name, created_time, updated_time) VALUES ('Microsoft', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- AUCTION

-- Auction with id 1
INSERT INTO auction(company_id, item_name, starting_price, step_price, start_time, end_time, status, created_time, updated_time)
VALUES (1, 'IC-255', 150000, 15000, CURRENT_TIMESTAMP, DATEADD(HOUR, 1, CURRENT_TIMESTAMP), 'LIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Auction with id 2
INSERT INTO auction(company_id, item_name, starting_price, step_price, start_time, end_time, status, created_time, updated_time)
VALUES (2, 'IC-355', 200000, 20000, DATEADD(MINUTE, 10, CURRENT_TIMESTAMP), DATEADD(HOUR, 1, CURRENT_TIMESTAMP), 'UPCOMING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);