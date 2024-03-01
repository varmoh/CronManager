package ee.buerokratt.cronmanager.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Service
public class ShellExecutionHelper {

    public static final String DEFAULT_ROOTPATH="/app";

    String[] environment;

    @Value("${application.appRootPath]")
    static String appRootPath;

    @Autowired
    public ShellExecutionHelper(@Value("${application.shellEnvironment}") List<String> environment,
                                @Value("${application.appRootPath}") String rootPath) {
        this.environment = environment.toArray(new String[0]);
        if (appRootPath == null) {
            appRootPath = rootPath;
            if (appRootPath == null) {
                // Default value if undefined: app folder in Docker container
                appRootPath = DEFAULT_ROOTPATH;
            }
        }
    }

    public List<String> execute(String command) throws IOException {
        File dir = new File(appRootPath);

        Process process = Runtime.getRuntime().exec(command, environment, dir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().toList();
    }

    public static List<String> executeWithoutEnvironment(String command) throws IOException {
        File dir = new File(appRootPath);
        Process process = Runtime.getRuntime().exec(command, null, dir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().toList();
    }

}
