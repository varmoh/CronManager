package ee.buerokratt.cronmanager.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ee.buerokratt.cronmanager.utils.LoggingUtils.mapDeepToString;
import static org.apache.logging.log4j.message.ParameterizedMessage.deepToString;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class ShellExecuteJob extends YamlJob {

    private String command;

    private List<String> allowedEnvs;

    @Override
    public String getType() {
        return "exec";
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        super.execute(context);

        String parentResult = (String) context.getResult();
        if (!parentResult.isEmpty()) {
            throw new JobExecutionException("Stopped execution: current time outside defined limits: %d [ %d -> %d ]".formatted(System.currentTimeMillis(), getStartDate(),  getEndDate()));
        }

        JobDataMap jdm = context.getJobDetail().getJobDataMap();
        command = jdm.getString("command");

        try {
            String params = jdm.containsKey("params") ? (String) jdm.get("params") : "empty";
            List<String> result = execProcess(command, params);
            log.info(result.stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new JobExecutionException("Problem running command: ", e);
        }
    }

    private List<String> execProcess(String command, String params) throws IOException {
        log.debug("Running "+command + "("+params+")");
        File dir = new File("/app/");
        Process process = Runtime.getRuntime().exec(command, params.split(","), dir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().collect(Collectors.toList());
    }

    @Override
    public JobDataMap getJobData() {
        JobDataMap map = super.getJobData();
        map.put("command", command);
        if (allowedEnvs != null)
            map.put("allowedEnvs", String.join(",", allowedEnvs));
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
