package org.dschweie.neoload.advancedactions.robotframework;

import java.util.List;
import org.dschweie.neoload.advancedactions.command.CommandFactory;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import org.dschweie.neoload.advancedactions.AbstractActionEngine;
import org.dschweie.neoload.advancedactions.robotframework.factory.CommandWorker;

public final class RobotFrameworkActionEngine extends AbstractActionEngine {

  @Override
  public SampleResult execute(Context context, List<ActionParameter> parameters) {
    //parameters.add(new ActionParameter("-variable", "NEOLOAD-USERPATH=".concat("context.getCurrentVirtualUser().getId()")));
    // SampleResult retval =
    return this.executeProcess(context, CommandWorker.buildCommand(parameters), true, true, CommandFactory.getParameterValue(parameters, "forceStatusCode", null));
  }

  private void appendLineToStringBuilder(final StringBuilder sb, final String line)
  {
    sb.append(line).append("\n");
  }

  /**
   * This method allows to easily create an error result and log exception.
   */
  private static SampleResult getErrorResult(final Context context, final SampleResult result, final String errorMessage, final Exception exception)
  {
    result.setError(true);
    result.setStatusCode("NL-Robot Framework Action_ERROR");
    result.setResponseContent(errorMessage);
    if(exception != null){
      context.getLogger().error(errorMessage, exception);
    } else{
      context.getLogger().error(errorMessage);
    }
    return result;
  }

  @Override
  public void stopExecute() {
    // TODO add code executed when the test have to stop.
  }

  protected List<String> buildCommand(List<ActionParameter> parameters)
  {
    final List<String>  command  = new java.util.Vector<String>();
    command.add("python");
    command.add("-m");
    command.add("robot.run");
    command.add("--version");
    return command;
  }
}
