package cl.duoc.springappduoc.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingsControllerTest {

    private final GreetingsController controller = new GreetingsController();

    @Test
    void greetings_withNoMessage_returnsHelloWorld() {
        assertEquals("Hello world", controller.greetings(null));
    }

    @Test
    void greetings_withBlankMessage_returnsHelloWorld() {
        assertEquals("Hello world", controller.greetings("   "));
    }

    @Test
    void greetings_withMessage_returnsHelloMessage() {
        assertEquals("Hello Duoc", controller.greetings("Duoc"));
    }

    @Test
    void greetings_withEmptyString_returnsHelloWorld() {
        assertEquals("Hello world", controller.greetings(""));
    }
}
