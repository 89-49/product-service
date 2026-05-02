CREATE TABLE IF NOT EXISTS p_products (
                                          id UUID PRIMARY KEY,
                                          name VARCHAR NOT NULL,
                                          price INTEGER NOT NULL,
                                          description VARCHAR,
                                          status VARCHAR NOT NULL,
                                          start_time TIMESTAMP,
                                          end_time TIMESTAMP,
                                          created_by UUID,
                                          created_at TIMESTAMP,
                                          modified_by UUID,
                                          modified_at TIMESTAMP,
                                          deleted_by UUID,
                                          deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS p_outbox (
                                        message_id UUID PRIMARY KEY,
                                        correlation_id UUID NOT NULL,
                                        domain_type VARCHAR NOT NULL,
                                        domain_id UUID NOT NULL,
                                        event_type VARCHAR NOT NULL,
                                        payload JSONB,
                                        status VARCHAR NOT NULL,
                                        retry_count INTEGER DEFAULT 0,
                                        created_by UUID,
                                        created_at TIMESTAMP,
                                        modified_by UUID,
                                        modified_at TIMESTAMP,
                                        deleted_by UUID,
                                        deleted_at TIMESTAMP,
                                        CONSTRAINT uk_outbox_correlation_id_type UNIQUE (correlation_id, event_type)
    );

CREATE INDEX IF NOT EXISTS idx_outbox_status ON p_outbox (status);

CREATE TABLE IF NOT EXISTS p_inbox (
                                       message_id UUID PRIMARY KEY,
                                       message_group VARCHAR,
                                       status VARCHAR DEFAULT 'RECEIVED',
                                       received_at TIMESTAMP,
                                       processed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inbox_message_group ON p_inbox (message_group);
CREATE INDEX IF NOT EXISTS idx_inbox_processed_at ON p_inbox (received_at);