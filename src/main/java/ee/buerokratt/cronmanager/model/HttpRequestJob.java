package ee.buerokratt.cronmanager.model;

import ee.buerokratt.cronmanager.services.HttpHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class HttpRequestJob extends YamlJob implements Job {
    private String method;
    private String url;

    public String getType() {
        return "http";
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        super.execute(context);

        String parentResult = (String) context.getResult();

        if (!parentResult.isEmpty()) {
            throw new JobExecutionException("Stopped execution: current time outside defined limits: %d [ %d -> %d ]".formatted(System.currentTimeMillis(), getStartDate(),  getEndDate()));
        }

        method = context.getJobDetail().getJobDataMap().getString("method");
        url = context.getJobDetail().getJobDataMap().getString("url");
        log.info(HttpHelper.doRequest(method, url).getBody());
    }

    @Override
    public String toString() {
        return String.format("%s (%s) => %s: %s [%d -> %d]",
                getName(),
                getTrigger(),
                getMethod(),
                getUrl(),
                getStartDate(),
                getEndDate());
    }

    @Override
    public JobDataMap getJobData() {
        JobDataMap map = super.getJobData();
        map.put("method", method);
        map.put("url", url);
        return map;
    }
}

