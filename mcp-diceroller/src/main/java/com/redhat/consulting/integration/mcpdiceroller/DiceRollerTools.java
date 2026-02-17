package com.redhat.consulting.integration.mcpdiceroller;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkus.logging.Log;

public class DiceRollerTools {

    Random dice = new Random();
    Pattern dicePattern = Pattern.compile("(\\d+)d(\\d+)([+-]\\d+)");

    @Tool(description = """
        Roll dice according to the expression XdY+Z, 
        where X is the number of dice, 
        Y is the 'size' of the dice from 1 to Y 
        and Z is a modifier to add or subtract from the total. 
        """)
    @Deprecated(since="Not tested, probably buggy.")
    public String rollExpression(@ToolArg String diceExpression) {
        Log.info("Tool rollExpression. diceExpression="+diceExpression);

        Matcher matcher = dicePattern.matcher(diceExpression);
        int numDice = Integer.valueOf(matcher.group(1));
        int sizeDice = Integer.valueOf(matcher.group(2));
        int modifier = matcher.groupCount() == 3 ? Integer.valueOf(matcher.group(3)) : 0;

        int result = 0;
        for(int i = 0; i < numDice; i++) {
            int roll = dice.nextInt(1, sizeDice+1);
            Log.info("Tool rollExpression. roll="+roll);
            result += roll;
        }
        result += modifier;

        return String.valueOf(result);
    }

    @Tool(description = """
        Roll dice to get a number between the lower bound and the upper bound.
        """)
    public Integer rollSimple(
        @ToolArg(description = "lower bound", defaultValue = "1") Integer lower,
        @ToolArg(description = "upper bound") Integer upper){

        Log.info("rollSimple lower="+lower +",upper="+upper);
        int result = dice.nextInt(lower, upper+1);
        
        Log.info("rollSimple result="+result);
        return result;
    }
    
}
