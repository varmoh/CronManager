package ee.buerokratt.cronmanager.model;

import ee.buerokratt.cronmanager.services.ShellExecutionHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class ShellExecuteJob extends YamlJob {

    private String command;

    @Autowired
    ShellExecutionHelper shellHelper;

    @Override
    public String getType() {
        return "exec";
    }

    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        super.executeInternal(context);
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        String parentResult = (String) context.getResult();
        if (!parentResult.isEmpty()) {
            throw new JobExecutionException("Stopped execution: current time outside defined limits: %d [ %d -> %d ]".formatted(System.currentTimeMillis(), getStartDate(),  getEndDate()));
        }

        command = context.getJobDetail().getJobDataMap().getString("command");

        try {
            List<String> result = ShellExecutionHelper.executeWithoutEnvironment(command);
            log.info(result.stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new JobExecutionException("Problem running command: ", e);
        }
    }

    @Override
    public JobDataMap getJobData() {
        JobDataMap map = super.getJobData();
        map.put("command", command);
        return map;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) => \"%s\" [%d -> %d]",
                getName(),
                getTrigger(),
                getCommand(),
                getStartDate(),
                getEndDate());
    }
}
