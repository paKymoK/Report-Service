CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS priority
(
    id              bigserial         NOT NULL,
    name            character varying NOT NULL,
    response_time   integer           NOT NULL,
    resolution_time integer           NOT NULL,
    created_at      timestamp with time zone,
    created_by      character varying,
    modified_at     timestamp with time zone,
    modified_by     character varying,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS project
(
    id          bigserial         NOT NULL,
    name        character varying NOT NULL,
    code        character varying NOT NULL,
    workflow_id bigint            NOT NULL,
    created_at  timestamp with time zone,
    created_by  character varying,
    modified_at timestamp with time zone,
    modified_by character varying,
    PRIMARY KEY (id),
    UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS issue_type
(
    id          bigserial         NOT NULL,
    name        character varying NOT NULL,
    code        character varying NOT NULL,
    project_id  integer           NOT NULL,
    created_at  timestamp with time zone,
    created_by  character varying,
    modified_at timestamp with time zone,
    modified_by character varying,
    PRIMARY KEY (id),
    UNIQUE (name, project_id),
    UNIQUE (code, project_id)
);

CREATE TABLE IF NOT EXISTS sla
(
    id          bigserial NOT NULL,
    ticket_id   bigint    NOT NULL,
    status      jsonb     NOT NULL,
    is_paused   bool      NOT NULL,
    paused_time jsonb     NOT NULL,
    priority    jsonb     NOT NULL,
    setting     jsonb     NOT NULL,
    created_at  timestamp with time zone,
    created_by  character varying,
    modified_at timestamp with time zone,
    modified_by character varying,
    PRIMARY KEY (id),
    UNIQUE (ticket_id)
);

CREATE TABLE IF NOT EXISTS ticket
(
    id                   bigserial NOT NULL,
    project              jsonb     NOT NULL,
    issue_type           jsonb     NOT NULL,
    status               jsonb     NOT NULL,
    summary              character varying,
    reporter             jsonb     NOT NULL,
    assignee             jsonb,
    detail               jsonb     NOT NULL,
    priority             jsonb     NOT NULL,
    time_to_in_progress  timestamptz,
    time_to_closed       timestamptz,
    created_at           timestamp with time zone,
    created_by           character varying,
    modified_at          timestamp with time zone,
    modified_by          character varying,
    PRIMARY KEY (id)
);

ALTER TABLE sla
    REPLICA IDENTITY FULL;

CREATE INDEX IF NOT EXISTS idx_ticket_status_group ON ticket ((status->>'group'));
CREATE INDEX IF NOT EXISTS idx_ticket_issue_type ON ticket ((issue_type->>'name'));
CREATE INDEX idx_tickets_detail_gin ON ticket USING GIN (detail);
CREATE INDEX IF NOT EXISTS idx_ticket_status_id
ON ticket (((status ->> 'id')::bigint));

CREATE INDEX IF NOT EXISTS idx_ticket_priority_id
ON ticket (((priority ->> 'id')::bigint));

CREATE INDEX IF NOT EXISTS idx_ticket_assignee_sub
ON ticket ((assignee ->> 'sub'));

CREATE INDEX IF NOT EXISTS idx_sla_status_response ON sla ((status ->> 'response'));
CREATE INDEX IF NOT EXISTS idx_sla_status_resolution ON sla ((status ->> 'resolution'));
CREATE INDEX IF NOT EXISTS idx_sla_status_response_overdue ON sla ((status ->> 'isResponseOverdue'));
CREATE INDEX IF NOT EXISTS idx_sla_status_resolution_overdue ON sla ((status ->> 'isResolutionOverdue'));
CREATE INDEX IF NOT EXISTS idx_sla_status_resolution_percent ON sla ((status ->> 'resolutionPercent'));

CREATE OR REPLACE FUNCTION validate_paused_time()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    data  jsonb;
    count INT = 0;
BEGIN
    FOR data IN SELECT * FROM jsonb_array_elements(NEW.paused_time)
        LOOP
            IF data ->> 'resumeTime' IS NULL THEN
                count = count + 1;
            END IF;
            RAISE NOTICE 'Data: %', data -> 'resumeTime';
        END LOOP;

    IF count > 1 THEN
        RAISE EXCEPTION 'Can not have two active pause Object';
    END IF;

    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER validate_paused_time
    BEFORE INSERT OR UPDATE OF paused_time
    ON sla
    FOR EACH ROW
EXECUTE FUNCTION validate_paused_time();

CREATE EXTENSION IF NOT EXISTS pg_cron;




