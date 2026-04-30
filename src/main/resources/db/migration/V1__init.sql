CREATE TABLE product (
                         id          BIGSERIAL PRIMARY KEY,
                         name        VARCHAR(200) NOT NULL,
                         brand       VARCHAR(100) NOT NULL,
                         category    VARCHAR(50)  NOT NULL,
                         created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE price_history (
                               id          BIGSERIAL PRIMARY KEY,
                               product_id  BIGINT       NOT NULL REFERENCES product(id),
                               store_name  VARCHAR(100) NOT NULL,
                               price       NUMERIC(10,2) NOT NULL,
                               recorded_at TIMESTAMP    NOT NULL DEFAULT NOW()
);