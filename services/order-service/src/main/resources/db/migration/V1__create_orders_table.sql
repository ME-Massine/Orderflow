CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        customer_id VARCHAR(255) NOT NULL,
                        product_id BIGINT NOT NULL,
                        quantity INTEGER NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL
);