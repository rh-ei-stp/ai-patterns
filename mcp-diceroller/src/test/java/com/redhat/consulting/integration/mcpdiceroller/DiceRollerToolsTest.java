package com.redhat.consulting.integration.mcpdiceroller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DiceRollerToolsTest {
    @Test
    void rollExpression() {
        var diceRoller = new DiceRollerTools();
        String response = diceRoller.rollExpression("2d6+1").firstContent().asText().text();
        int result = Integer.valueOf(response);
        assertTrue(3 <= result && result <= 13);

        response = diceRoller.rollExpression("1d1-1").firstContent().asText().text();
        result = Integer.valueOf(response);
        assertTrue(result == 0);

        assertTrue(diceRoller.rollExpression("asdf").isError());
    }

    @Test
    void rollSimple() {
        var diceRoller = new DiceRollerTools();
        String response = diceRoller.rollSimple(1, 6).firstContent().asText().text();
        int result = Integer.valueOf(response);
        assertTrue(1 <= result && result <= 6);
    }

}
