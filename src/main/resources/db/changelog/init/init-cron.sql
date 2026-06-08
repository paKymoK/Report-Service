SELECT cron.schedule('cleanup-cron-logs', '0 * * * *', $$ DELETE FROM cron.job_run_details
    WHERE status = 'succeeded'
$$);

SELECT cron.schedule(
               'process-updates',
               '30 seconds',
               'CALL calculate_sla()'
       );