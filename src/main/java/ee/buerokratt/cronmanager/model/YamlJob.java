package ee.buerokratt.cronmanager.model;

import lombok.Data;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

@Data
public abstract class YamlJob implements Job {

    private String name;
    private String trigger;

    private Long startDate;
    private Long endDate;

    public YamlJob() {
    }

    public abstract String getType();

    public String getTriggerName() {
        return getName() + "_trigger";
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        name = context.getJobDetail().getJobDataMap().getString("name");
        trigger = context.getJobDetail().getJobDataMap().getString("trigger");
        startDate = context.getJobDetail().getJobDataMap().containsKey("startDate") ?
                context.getJobDetail().getJobDataMap().getLong("startDate") :
                null;
        endDate = context.getJobDetail().getJobDataMap().containsKey("endDate") ?
                context.getJobDetail().getJobDataMap().getLong("endDate") :
                null;

        if (startDate != null && System.currentTimeMillis() < startDate) {
            context.setResult("<");
        }  else if (endDate != null && System.currentTimeMillis() > endDate){
            context.setResult(">");
        } else{
            context.setResult("");
        }
    }

    public JobDataMap getJobData() {
        JobDataMap map = new JobDataMap();
        map.put("name", name);
        map.put("trigger", trigger);
        if (startDate != null)
            map.put("startDate", startDate);
        if (endDate != null)
            map.put("endDate", endDate);
        return map;
    }
}
