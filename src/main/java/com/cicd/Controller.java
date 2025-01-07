package com.cicd;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class Controller {
    Environment environment;

    @GetMapping
    public String systemInfo() {
        var javaVersion = System.getProperty("java.version");
        var osName = System.getProperty("os.name");
        var osVersion = System.getProperty("os.version");
        var userName = System.getProperty("user.name");
        var serverPort = environment.getProperty("server.port");

        var message = "<h1>Hello, Jenkins!</h1> <p>Java Version: %s</p> <p>OS Name: %s</p> <p>OS Version: %s</p> <p>Server Port: %s</p> <p>Logged-in User: %s</p> <p>Hiệp Gà</p>";

        return message.formatted(javaVersion, osName, osVersion, serverPort, userName);

    }

}
