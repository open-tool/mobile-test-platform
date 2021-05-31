CREATE TABLE IF NOT EXISTS nodes (
  id                     VARCHAR(100)  DEFAULT RANDOM_UUID() PRIMARY KEY,
  url                   VARCHAR      NOT NULL
);