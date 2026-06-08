CREATE OR REPLACE FUNCTION calculate_office_time(
    p_from TIMESTAMPTZ,
    p_to TIMESTAMPTZ,
    p_timezone TEXT DEFAULT 'UTC',
    p_work_start TIME DEFAULT '09:00:00',
    p_work_end TIME DEFAULT '18:00:00',
    p_lunch_start TIME DEFAULT '12:00:00',
    p_lunch_end TIME DEFAULT '13:00:00',
    p_out_of_office TSTZRANGE[] DEFAULT NULL,
    p_weekend_days INTEGER[] DEFAULT ARRAY [0, 6]
)
    RETURNS INTEGER AS
$$
DECLARE
    v_current_date    DATE;
    v_end_date        DATE;
    v_total_seconds   INTEGER := 0;
    v_work_day_start  TIMESTAMPTZ;
    v_work_day_end    TIMESTAMPTZ;
    v_lunch_start_ts  TIMESTAMPTZ;
    v_lunch_end_ts    TIMESTAMPTZ;
    v_work_start      TIMESTAMPTZ;
    v_work_end        TIMESTAMPTZ;
    v_period_seconds  INTEGER;
    v_ooo_range       TSTZRANGE;
    v_overlap_start   TIMESTAMPTZ;
    v_overlap_end     TIMESTAMPTZ;
    v_overlap_seconds INTEGER;
    v_day_of_week     INTEGER;
BEGIN
    -- Handle NULL or invalid input
    IF p_from IS NULL OR p_to IS NULL OR p_from >= p_to THEN
        RETURN 0;
    END IF;

    -- Validate work times
    IF p_work_start IS NULL OR p_work_end IS NULL OR p_work_start >= p_work_end THEN
        RAISE EXCEPTION 'Invalid work time: work_start must be before work_end';
    END IF;

    -- Validate lunch times
    IF p_lunch_start IS NULL OR p_lunch_end IS NULL OR p_lunch_start >= p_lunch_end THEN
        RAISE EXCEPTION 'Invalid lunch time: lunch_start must be before lunch_end';
    END IF;

    -- Validate lunch is within work hours
    IF p_lunch_start < p_work_start OR p_lunch_end > p_work_end THEN
        RAISE EXCEPTION 'Lunch time must be within work hours';
    END IF;

    -- Get date range in the specified timezone
    v_current_date := (p_from AT TIME ZONE p_timezone)::DATE;
    v_end_date := (p_to AT TIME ZONE p_timezone)::DATE;

    -- Loop through each day in the range
    WHILE v_current_date <= v_end_date
        LOOP
            -- Get day of week (0=Sunday, 1=Monday, ..., 6=Saturday)
            v_day_of_week := EXTRACT(DOW FROM v_current_date)::INTEGER;

            -- Check if current day is a weekend (skip if it is)
            IF p_weekend_days IS NULL OR NOT (v_day_of_week = ANY (p_weekend_days)) THEN

                -- Create work day timestamps in the target timezone
                v_work_day_start := make_timestamptz(
                        EXTRACT(YEAR FROM v_current_date)::INT,
                        EXTRACT(MONTH FROM v_current_date)::INT,
                        EXTRACT(DAY FROM v_current_date)::INT,
                        EXTRACT(HOUR FROM p_work_start)::INT,
                        EXTRACT(MINUTE FROM p_work_start)::INT,
                        EXTRACT(SECOND FROM p_work_start)::INT,
                        p_timezone
                                    );

                v_work_day_end := make_timestamptz(
                        EXTRACT(YEAR FROM v_current_date)::INT,
                        EXTRACT(MONTH FROM v_current_date)::INT,
                        EXTRACT(DAY FROM v_current_date)::INT,
                        EXTRACT(HOUR FROM p_work_end)::INT,
                        EXTRACT(MINUTE FROM p_work_end)::INT,
                        EXTRACT(SECOND FROM p_work_end)::INT,
                        p_timezone
                                  );

                v_lunch_start_ts := make_timestamptz(
                        EXTRACT(YEAR FROM v_current_date)::INT,
                        EXTRACT(MONTH FROM v_current_date)::INT,
                        EXTRACT(DAY FROM v_current_date)::INT,
                        EXTRACT(HOUR FROM p_lunch_start)::INT,
                        EXTRACT(MINUTE FROM p_lunch_start)::INT,
                        EXTRACT(SECOND FROM p_lunch_start)::INT,
                        p_timezone
                                    );

                v_lunch_end_ts := make_timestamptz(
                        EXTRACT(YEAR FROM v_current_date)::INT,
                        EXTRACT(MONTH FROM v_current_date)::INT,
                        EXTRACT(DAY FROM v_current_date)::INT,
                        EXTRACT(HOUR FROM p_lunch_end)::INT,
                        EXTRACT(MINUTE FROM p_lunch_end)::INT,
                        EXTRACT(SECOND FROM p_lunch_end)::INT,
                        p_timezone
                                  );

                -- === MORNING SHIFT: Work Start to Lunch Start ===
                v_work_start := GREATEST(p_from, v_work_day_start);
                v_work_end := LEAST(p_to, v_lunch_start_ts);

                IF v_work_start < v_work_end THEN
                    v_period_seconds := EXTRACT(EPOCH FROM (v_work_end - v_work_start))::INTEGER;

                    -- Subtract out-of-office overlaps
                    IF p_out_of_office IS NOT NULL THEN
                        FOREACH v_ooo_range IN ARRAY p_out_of_office
                            LOOP
                                v_overlap_start := GREATEST(v_work_start, LOWER(v_ooo_range));
                                v_overlap_end := LEAST(v_work_end, UPPER(v_ooo_range));

                                IF v_overlap_start < v_overlap_end THEN
                                    v_overlap_seconds := EXTRACT(EPOCH FROM (v_overlap_end - v_overlap_start))::INTEGER;
                                    v_period_seconds := v_period_seconds - v_overlap_seconds;
                                END IF;
                            END LOOP;
                    END IF;

                    v_total_seconds := v_total_seconds + v_period_seconds;
                END IF;

                -- === AFTERNOON SHIFT: Lunch End to Work End ===
                v_work_start := GREATEST(p_from, v_lunch_end_ts);
                v_work_end := LEAST(p_to, v_work_day_end);

                IF v_work_start < v_work_end THEN
                    v_period_seconds := EXTRACT(EPOCH FROM (v_work_end - v_work_start))::INTEGER;

                    -- Subtract out-of-office overlaps
                    IF p_out_of_office IS NOT NULL THEN
                        FOREACH v_ooo_range IN ARRAY p_out_of_office
                            LOOP
                                v_overlap_start := GREATEST(v_work_start, LOWER(v_ooo_range));
                                v_overlap_end := LEAST(v_work_end, UPPER(v_ooo_range));

                                IF v_overlap_start < v_overlap_end THEN
                                    v_overlap_seconds := EXTRACT(EPOCH FROM (v_overlap_end - v_overlap_start))::INTEGER;
                                    v_period_seconds := v_period_seconds - v_overlap_seconds;
                                END IF;
                            END LOOP;
                    END IF;

                    v_total_seconds := v_total_seconds + v_period_seconds;
                END IF;
            END IF;

            v_current_date := v_current_date + 1;
        END LOOP;

    RETURN v_total_seconds;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION json_to_tstzrange_array(
    p_pause_resume_json JSONB
)
    RETURNS TSTZRANGE[] AS
$$
DECLARE
    v_ranges      TSTZRANGE[] := ARRAY []::TSTZRANGE[];
    v_record      JSONB;
    v_paused_time TIMESTAMPTZ;
    v_resume_time TIMESTAMPTZ;
BEGIN
    -- Validate input
    IF p_pause_resume_json IS NULL THEN
        RETURN NULL;
    END IF;

    -- Iterate through each pause/resume record
    FOR v_record IN SELECT * FROM jsonb_array_elements(p_pause_resume_json)
        LOOP
            -- Extract pausedTime
            v_paused_time := (v_record ->> 'pausedTime')::TIMESTAMPTZ;

            -- Extract resumeTime (handle null)
            IF v_record ->> 'resumeTime' IS NOT NULL THEN
                v_resume_time := (v_record ->> 'resumeTime')::TIMESTAMPTZ;
            ELSE
                v_resume_time := CURRENT_TIMESTAMP;
            END IF;

            -- Only add if we have both times and paused < resume
            IF v_paused_time IS NOT NULL AND v_resume_time IS NOT NULL AND v_paused_time < v_resume_time THEN
                v_ranges := array_append(v_ranges, tstzrange(v_paused_time, v_resume_time));
            END IF;
        END LOOP;

    RETURN v_ranges;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE calculate_sla()
    LANGUAGE PLPGSQL
AS
$$
DECLARE
    v_lock_key CONSTANT BIGINT := 987654321;
    v_lock_acquired     BOOLEAN;
BEGIN
    SELECT pg_try_advisory_lock(v_lock_key) INTO v_lock_acquired;

    IF NOT v_lock_acquired THEN
        RAISE NOTICE 'calculate_sla already running, skipping this execution';
        RETURN;
    END IF;

    WITH ticket_data AS (SELECT t.id,
                                t.created_at,
                                s.id                                                            AS sla_id,
                                (s.setting ->> 'workStart')::TIME                               AS work_start,
                                (s.setting ->> 'workEnd')::TIME                                 AS work_end,
                                (s.setting ->> 'lunchStart')::TIME                              AS lunch_start,
                                (s.setting ->> 'lunchEnd')::TIME                                AS lunch_end,
                                (s.priority ->> 'responseTime')::INTEGER                        AS response_time,
                                (s.priority ->> 'resolutionTime')::INTEGER                      AS resolution_time,
                                json_to_tstzrange_array(s.paused_time)                          AS paused_ranges,
                                ARRAY(SELECT jsonb_array_elements(s.setting -> 'weekend')::int) AS weekend_days
                         FROM ticket t
                                  INNER JOIN sla s ON t.id = s.ticket_id
                         WHERE (
                             (
                                 (s.status ->> 'isResponseOverdue') IS DISTINCT FROM 'true'
                                     AND (s.status ->> 'response') IS DISTINCT FROM 'DONE'
                                 ) OR (
                                 (s.status ->> 'isResolutionOverdue') IS DISTINCT FROM 'true'
                                     AND (s.status ->> 'resolution') IS DISTINCT FROM 'DONE'
                                     AND COALESCE((s.status ->> 'resolutionPercent')::int, -1) < 100
                                 )
                             )
                           AND NOW() - t.created_at > LEAST(
                                                              (s.priority ->> 'responseTime')::int,
                                                              (s.priority ->> 'resolutionTime')::int
                                                      ) * interval '1 hour'),
         office_time_data AS (SELECT td.*,
                                     calculate_office_time(
                                             td.created_at, NOW(), 'Asia/Ho_Chi_Minh',
                                             td.work_start, td.work_end,
                                             td.lunch_start, td.lunch_end,
                                             td.paused_ranges, td.weekend_days
                                     ) AS office_seconds
                              FROM ticket_data td),
         office_time_with_percent AS (SELECT *,
                                             LEAST(
                                                     FLOOR(office_seconds::float / NULLIF(resolution_time * 3600, 0) * 100)::int,
                                                     100
                                             ) AS resolution_percent
                                      FROM office_time_data),
         update_response AS (
             UPDATE sla s
                 SET status = status || jsonb_build_object('isResponseOverdue', true)
                 FROM office_time_with_percent td
                 WHERE s.id = td.sla_id
                     AND (s.status ->> 'isResponseOverdue') IS DISTINCT FROM 'true'
                     AND (s.status ->> 'response') IS DISTINCT FROM 'DONE'
                     AND td.office_seconds > td.response_time * 3600),
         update_resolution_percent AS (
             UPDATE sla s
                 SET status = status || jsonb_build_object('resolutionPercent', td.resolution_percent)
                 FROM office_time_with_percent td
                 WHERE s.id = td.sla_id
                     AND (s.status ->> 'isResolutionOverdue') IS DISTINCT FROM 'true'
                     AND (s.status ->> 'resolution') IS DISTINCT FROM 'DONE'
                     AND td.office_seconds <= td.resolution_time * 3600
                     AND td.resolution_percent != COALESCE((s.status ->> 'resolutionPercent')::int, -1))
    UPDATE sla s
    SET status = status || jsonb_build_object(
            'isResolutionOverdue', true,
            'resolutionPercent', 100
                           )
    FROM office_time_with_percent td
    WHERE s.id = td.sla_id
      AND (s.status ->> 'isResolutionOverdue') IS DISTINCT FROM 'true'
      AND (s.status ->> 'resolution') IS DISTINCT FROM 'DONE'
      AND td.office_seconds > td.resolution_time * 3600;

    PERFORM pg_advisory_unlock(v_lock_key);

EXCEPTION
    WHEN OTHERS THEN
        PERFORM pg_advisory_unlock(v_lock_key);
        RAISE;
END;
$$;

