package ee.buerokratt.cronmanager.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShellExecutionHelper {

    String[] environment;

    @Autowired
    public ShellExecutionHelper(@Value("${application.shellEnvironment}") List<String> environment) {
        this.environment = environment.toArray(new String[0]);
    }

    public List<String> execute(String command) throws IOException {
        File dir = new File("/app/");
        Process process = Runtime.getRuntime().exec(command, environment, dir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().collect(Collectors.toList());
    }

    public static List<String> executeWithoutEnvironment(String command) throws IOException {
        File dir = new File("/app/");
        Process process = Runtime.getRuntime().exec(command, null, dir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().collect(Collectors.toList());
    }

}
