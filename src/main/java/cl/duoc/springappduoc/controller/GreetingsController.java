package cl.duoc.springappduoc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Greetings", description = "Greetings API")
public class GreetingsController {

    private static final Logger logger = LoggerFactory.getLogger(GreetingsController.class);

    @GetMapping("/")
    @Operation(summary = "API Info", description = "Shows links to the API documentation")
    public ModelAndView index() {
        logger.info("Request received on / endpoint");
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("message", "Welcome to the Greetings API");
        return mav;
    }

    @GetMapping("/greetings")
    @Operation(summary = "Get a greeting message", description = "Returns a greeting with the provided message or 'Hello world' by default")
    public String greetings(
            @Parameter(description = "Optional message to include in the greeting")
            @RequestParam(value = "message", required = false) String message) {

        logger.info("Request received on /greetings endpoint");

        String response;
        if (message != null && !message.isBlank()) {
            logger.info("Message parameter: {}", message);
            response = "Hello " + message;
        } else {
            logger.info("No message parameter provided, using default");
            response = "Hello world";
        }

        return response;
    }
}
