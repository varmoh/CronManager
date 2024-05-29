package ee.buerokratt.cronmanager.controllers;

import ee.buerokratt.cronmanager.services.CronService;
import ee.buerokratt.cronmanager.services.JobReaderService;
import jakarta.servlet.http.HttpServletRequest;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class CronController {

    @Autowired
    JobReaderService jobReader;

    @Autowired
    CronService cron;

    @GetMapping("/")
    public String index() {
        return "BYK Cron started";
    }

    @GetMapping(value = "/jobs/",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String jobs() {
        return jobs("");
    }

    @GetMapping(value = "/jobs/{groupName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String jobs(@PathVariable(required = false) String groupName) {
        try {
            return cron.getJobs(groupName);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/running/",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String runningJobs() {
        return runningJobs("");
    }

    @GetMapping(value = "/running/{groupName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String runningJobs(@PathVariable(required = false) String groupName) {
        try {
            return cron.getRunningJobs(groupName);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping(value = "/execute/{groupName}/{jobName}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public String executeJob(@PathVariable String groupName,
                             @PathVariable String jobName,
                             HttpServletRequest request) {
        try {
            return cron.executeJob(groupName, jobName, request.getParameterMap());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/stop/{groupName}/{jobName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String stopJob(@PathVariable String groupName, @PathVariable String jobName) {
        try {
            return cron.stopJob(groupName, jobName);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping(value = "/reload/{groupName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String reloadJobs(@RequestParam(required = false) String groupName){
        jobReader.readServices();
        try {
            return cron.getJobs(groupName);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

}
