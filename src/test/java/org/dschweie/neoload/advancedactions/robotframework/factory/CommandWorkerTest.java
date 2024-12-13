package org.dschweie.neoload.advancedactions.robotframework.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dschweie.neoload.advancedactions.command.CommandFactory;
import org.junit.jupiter.api.Test;

import com.neotys.extensions.action.ActionParameter;

import java.util.List;
import java.util.Vector;

public class CommandWorkerTest
{
  public final static String RBTFRMWRK_ACTION = "RobotFramework-Execute-Test";
  @Test
  public void checkParameter() {
    ActionParameter parameter = new ActionParameter("nostatusrc", "true");
    List<ActionParameter> configuration = new Vector<ActionParameter>();

    configuration.add(parameter);

    List<String> result = CommandFactory.buildCommand(RBTFRMWRK_ACTION, configuration);

    int offset = 0;
    switch(result.get(0))
    {
      case "java":
      case "python":  offset = 9; break;
      case "robot":   offset = 7; break;
    }

    assertEquals(1 + offset, result.size());
    assertEquals("--nostatusrc", result.get(0+offset) );
  }
}
