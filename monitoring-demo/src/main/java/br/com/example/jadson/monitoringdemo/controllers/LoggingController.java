package br.com.example.jadson.monitoringdemo.controllers;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller that generate logs.
 * Spring Boot Starter uses ***Logback*** by default.
 */
@RestController
@Slf4j
public class LoggingController {

    /**
     * The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging
     * frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging
     * framework at deployment time.
     */
    Logger logger = LoggerFactory.getLogger(LoggingController.class);

    @GetMapping("/loging")
    public String index() {
        // logger.trace("A TRACE Message");
        // logger.debug("A DEBUG Message");
        logger.info("An INFO Message");
        logger.warn("A WARN Message");
        logger.error("An ERROR Message");

        log.info("An INFO Message Using Lombok  !!! ");
        log.error("An ERROR Message Using Lombok !!! ");

        System.out.println("This is not a log !!!");

        return "Howdy! Check out the Logs to see the output...";
    }

    @GetMapping("/tracing")
    public String tracing() {
        try{
            throw new NullPointerException("NullPointerException");
        }catch (Exception ex) {
            ex.printStackTrace();
            StackTraceElement[] elements = ex.getStackTrace();
            StackTraceElement[] newElements = new StackTraceElement[5];
            int limit = elements.length > 5 ? 5 : elements.length;
            for (int i = 0; i < limit ; i++)
                newElements[i] = elements[i];
            ex.setStackTrace(newElements);
            logger.error("ERROR: "+ex.getLocalizedMessage(), ex);
        }

        return "Howdy! Check out the Logs to see the tracing error...";
    }
}
