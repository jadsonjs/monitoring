package br.com.example.jadson.monitoringdemo.controllers;

import ch.qos.logback.classic.Level;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Enumeration;

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
    public String index(HttpServletRequest request) {

        Logger logger2 =  LoggerFactory.getLogger("RequestLogger");


        // logger.trace("A TRACE Message");
        // logger.debug("A DEBUG Message");
        logger.info("An INFO Message");
        logger.warn("A WARN Message");
        logger.error("An ERROR Message");

        log.info("An INFO Message Using Lombok  !!! ");
        log.error("An ERROR Message Using Lombok !!! ");

        System.out.println("This is not a log !!!");

        /**********************************************
         * Request LOG
         **********************************************/

        logger2.info(getUser().getLogin()+"(ADMIN,COORDENADOR)"
                +" ["+request.getMethod()+"] "+request.getRequestURI()
                +" "+(request.getQueryString() != null ? request.getQueryString() : "")
                +" "+request.getRemoteAddr()+":"+request.getRemotePort()
                +" "+request.getHeader("user-agent"));

        return "Howdy! Check out the Logs to see the output...";
    }



    @GetMapping("/tracing")
    public String tracing() {

        Logger logger2 =  LoggerFactory.getLogger("StackTraceLogger");
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
            logger.error("ERROR: "+ex.getLocalizedMessage());
            logger2.error(ex.getLocalizedMessage(), ex);
        }

        return "Howdy! Check out the Logs to see the tracing error...";
    }


    private User getUser() {
        return new User("jadson");
    }

    private class User {
        String login;
        public User(String login) {
            this.login = login;
        }
        public String getLogin(){ return login;}
    }
}
