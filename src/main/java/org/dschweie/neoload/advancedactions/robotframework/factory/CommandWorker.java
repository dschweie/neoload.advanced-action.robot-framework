package org.dschweie.neoload.advancedactions.robotframework.factory;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.neotys.extensions.action.ActionParameter;
import org.dschweie.neoload.advancedactions.command.CommandFactory;
import org.dschweie.neoload.advancedactions.robotframework.RobotFrameworkAction;

/*
 *  not supported Command Line Options:
 * 
 *  --rpa  
 *        Turn on generic automation mode.
 *        aktuell keine Notwendigkeit
 * 
 *  -N, --name <name>
 *        Sets the name of the top-level test suite.
 *        
 *  -D, --doc <document>
 *        Sets the documentation of the top-level test suite.
 * 
 *  -R, --rerunfailed <file>
 *        Selects failed tests from an earlier output file to be re-executed.
 *        Im Performanztest eher nicht relevant
 *  
 *  -S, --rerunfailedsuites <file>
 *        Selects failed test suites from an earlier output file to be re-executed.
 *        Im Performanztest eher nicht relevant
 *        
 *  -G, --settag <tag>
 *        Sets the tag(s) to all executed test cases.
 *        Im Performanztest eher nicht relevant
 *        
 *  -c, --critical <tag>
 *        Tests that have the given tag are considered critical.
 *        Im Performanztest eher nicht relevant
 *        
 *  -n, --noncritical <tag>
 *        Tests that have the given tag are not critical.
 *        Im Performanztest eher nicht relevant
 *
 *  --xunitskipnoncritical
 *        Mark non-critical tests on xUnit compatible result file as skipped.
 *  -b, --debugfile <file>  
 *        A debug file that is written during execution.
 *        
 *  -T, --timestampoutputs  
 *        Adds a timestamp to all output files.
 *        
 *  --splitlog  
 *        Split log file into smaller pieces that open in browser transparently.
 *        
 *  --logtitle <title>  
 *        Sets a title for the generated test log.
 *        
 *  --reporttitle <title>  
 *        Sets a title for the generated test report.
 *        
 *  --reportbackground <colors>  
 *        Sets background colors of the generated report.
 *        
 *  --maxerrorlines <lines>  
 *        Sets the number of error lines shown in reports when tests fail.
 *  
 *  --suitestatlevel <level>  
 *        Defines how many levels to show in the Statistics by Suite table in outputs.
 *  
 *  --tagstatinclude <tag>  
 *        Includes only these tags in the Statistics by Tag table.
 *  
 *  --tagstatexclude <tag>  
 *        Excludes these tags from the Statistics by Tag table.
 *  
 *  --tagstatcombine <tags:title>  
 *        Creates combined statistics based on tags.
 *  
 *  --tagdoc <pattern:doc>  
 *        Adds documentation to the specified tags.
 *  
 *  --tagstatlink <pattern:link:title>  
 *        Adds external links to the Statistics by Tag table.
 *  
 *  --expandkeywords <name:pattern|tag:pattern>  
 *        Automatically expand keywords in the generated log file.
 *  
 *  --removekeywords <all|passed|name:pattern|tag:pattern|for|wuks>  
 *        Removes keyword data from the generated log file.
 *  
 *  --flattenkeywords <for|foritem|name:pattern|tag:pattern>  
 *        Flattens keywords in the generated log file.
 */


/*
 *  TODO  Metadata for Test Call
 *        Vorstellbar ist hier, dass allgemeine Informationen von NeoLoad übergeben werden.
 *        
 *  -M, --metadata <name:value>
 *        Sets free metadata for the top level test suite.
 */




/*


--runemptysuite
  Executes tests also if the selected test suites are empty.
--dryrun  In the dry run mode tests are run without executing keywords originating from test libraries. Useful for validating test data syntax.
-X, --exitonfailure
  Stops test execution if any critical test fails.
--exitonerror Stops test execution if any error occurs when parsing test data, importing libraries, and so on.
--skipteardownonexit
  Skips teardowns if test execution is prematurely stopped.
--prerunmodifier <name:args>
  Activate programmatic modification of test data.
--prerebotmodifier <name:args>
  Activate programmatic modification of results.
--randomize <all|suites|tests|none>
  Randomizes test execution order.
--console <verbose|dotted|quiet|none>
  Console output type.
--dotted  Shortcut for --console dotted.
--quiet Shortcut for --console quiet.
-W, --consolewidth <width>
  Sets the width of the console output.
-C, --consolecolors <auto|on|ansi|off>
  Specifies are colors used on the console.
-K, --consolemarkers <auto|on|off>
  Show markers on the console when top level keywords in a test case end.
-P, --pythonpath <path>
  Additional locations to add to the module search path.
-A, --argumentfile <path>
  A text file to read more arguments from.
-h, --help  Prints usage instructions.
--version Prints the version information.
 */

public class CommandWorker
{
  public final static String PYTHON_MODE = "PYTHON";
  public final static String JAVA_MODE = "JAVA";
  public final static String UBUNTU = "UBUNTU";

  /**
   *  \brief        The method generates the parts to call Robot Framework
   *
   *  @param        parameters          The list of parameters that the user
   *                                    has configured for the action
   *                                    in NeoLoad is expected here.
   *
   *  @return       The method returns a list of arguments to be added
   *                to the call.
   */
  protected static List<String> getRobotCommand(List<ActionParameter> parameters)
  {
    final List<String>  elements  = new Vector<String>();
    switch(CommandFactory.getParameterValue(parameters, "environment", PYTHON_MODE).toUpperCase().trim())
    {
      case JAVA_MODE:     elements.add("java");
                          elements.add("-jar");
                          elements.add("robotframework.jar");
                          break;
      case UBUNTU:        elements.add("robot");
                          break;
      case PYTHON_MODE:   
      default:            elements.add("python");
                          elements.add("-m");
                          elements.add("robot");
                          break;
    }
    return elements;
  }

  /**
   *  \brief        The method provides the parts that control the output about the test execution.
   *
   *  The configuration of the specific action in NeoLoad must be converted
   *  into a call to Robot Framework via the command line. This method selects
   *  the values from the configuration that affect the output of
   *  Robot Framework and the return value.
   *
   *  This method can be used to generate the following command line parameters in the call:
   *  \li   \c --outputdir <dir><br/>Defines where to create output files.
   *  \li   \c --output <file><br/>Sets the path to the generated output file.
   *  \li   \c --log <file><br/>Sets the path to the generated log file.
   *  \li   \c --report <file><br/>Sets the path to the generated report file.
   *  \li   \c --xunit <file><br/>Sets the path to the generated xUnit compatible result file.
   *  \li   \c --loglevel <level><br/>Sets the threshold level for logging. Optionally the default visible log level can be given separated with a colon (:).
   *  \li   \c --nostatusrc<br/>Sets the return code to zero regardless of failures in test cases. Error codes are returned normally.
   *
   *  @param        parameters          The list of parameters that the user
   *                                    has configured for the action
   *                                    in NeoLoad is expected here.
   *  @return       The method returns a list of arguments to be added
   *                to the call.
   */
  protected static List<String> getOutputOptions(List<ActionParameter> parameters)
  {
    final List<String>  elements = new Vector<String>();
    String outputdir = CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_OUTPUTDIR, null);
    String xunit = CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_XUNIT, null);
    String debugfile = CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_DEBUGFILE, null);
    String loglevel = CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_LOGLEVEL, null);

    if(Boolean.parseBoolean(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_TIMESTAMPS, "false")))
    {
      elements.add("--timestampoutputs");
    }

    if (null != outputdir)
    {
      elements.add("--outputdir");
      elements.add(outputdir);
      if (null != CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_OUTPUT, null))
      {
        elements.add("--output");
        elements.add(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_OUTPUT, null));
      }
      if (null != CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_REPORT, null))
      {
        elements.add("--report");
        elements.add(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_REPORT, null));
      }
      if (null != CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_LOG, null))
      {
        elements.add("--log");
        elements.add(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_LOG, null));
      }
    }
    else {
      elements.add("--output");
      elements.add(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_OUTPUT, "NONE"));
      elements.add("--report");
      elements.add(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_REPORT, "NONE"));
      elements.add("--log");
      elements.add(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_LOG, "NONE"));
    }

    elements.addAll(CommandFactory.buildOptionValueArgument("--xunit", CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_XUNIT, null), null));
    elements.addAll(CommandFactory.buildOptionValueArgument("--debugfile", CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_DEBUGFILE, null), null));

    if (null != loglevel) {
      loglevel = loglevel.toUpperCase();
      elements.add("--loglevel");
      switch (loglevel) {
        case "FAIL":
        case "WARN":
        case "DEBUG":
        case "TRACE": elements.add(loglevel); break;
        case "INFO":
        default:      elements.add("INFO");
      }
    }

    if (Boolean.parseBoolean(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OUTPUT_NOSTATUSCODE, "false")))
    {
      elements.add("--nostatusrc");
    }

    return elements;
  }

  /**
   *  \brief        The method generate arguments to add a listener
   *
   *  @param        parameters          The list of parameters that the user
   *                                    has configured for the action
   *                                    in NeoLoad is expected here.
   *
   *  @return       The method returns a list of arguments to be added
   *                to the call.
   */
  protected static List<String> getListenerOptions(List<ActionParameter> parameters)
  {
    final List<String>  elements = new Vector<String>();
    String optionValue = CommandFactory.getParameterValue(parameters, RobotFrameworkAction.OPTION_LISTENER, null);

    if( null != optionValue ) {
      elements.add("--listener");
      elements.add(optionValue);
    }
    return elements;
  }

  /**
   *  \brief    In this method, arguments are generated that affect the execution of the test cases.
   *
   *  This method evaluates parameters that influence the selection
   *  of test cases and the execution sequence.
   *
   *  This method can be used to generate the following command line parameters in the call:
   *  \li   \c --extension <value><br/>Parse only these files when executing a directory.
   *  \li   \c --test <name><br/>Selects the test cases by name.
   *  \li   \c --task <name><br/>Alias for --test that can be used when executing tasks.
   *  \li   \c --suite <name><br/>Selects the test suites by name.
   *  \li   \c --include <tag><br/>Selects the test cases by tags
   *  \li   \c --exclude <tag><br/>Selects the test cases by tag.
   *  \li   \c --randomize <all|suites|tests><br/>Randomizes test execution order.
   *
   *  @param        parameters          The list of parameters that the user
   *                                    has configured for the action
   *                                    in NeoLoad is expected here.
   *  @return       The method returns a list of arguments to be added
   *                to the call.
   */
  protected static List<String> getTestExecutionOptions(List<ActionParameter> parameters)
  {
    final List<String>  elements = new Vector<String>();
    String randomize = CommandFactory.getParameterValue(parameters, RobotFrameworkAction.EXECUTION_RANDOMIZE, null);

    elements.addAll(CommandFactory.buildOptionValueArgument("--extension", CommandFactory.getParameterValue(parameters, RobotFrameworkAction.EXECUTION_EXTENSION, null), null));
    elements.addAll(CommandFactory.buildOptionValueArgument("--test", CommandFactory.getParameterValue(parameters, RobotFrameworkAction.EXECUTION_TEST, null), null));
    elements.addAll(CommandFactory.buildOptionValueArgument("--task", CommandFactory.getParameterValue(parameters, RobotFrameworkAction.EXECUTION_TASK, null), null));
    elements.addAll(CommandFactory.buildOptionValueArgument("--suite", CommandFactory.getParameterValue(parameters, RobotFrameworkAction.EXECUTION_SUITE, null), null));
    elements.addAll(CommandFactory.buildOptionValueArgument("--include", CommandFactory.getParameterValue(parameters, RobotFrameworkAction.EXECUTION_INCLUDE, null), null));
    elements.addAll(CommandFactory.buildOptionValueArgument("--exclude", CommandFactory.getParameterValue(parameters, RobotFrameworkAction.EXECUTION_EXCLUDE, null), null));

    if(null != randomize)
    {
      switch (randomize.toLowerCase())
      {
        case "tests":
        case "suites":
        case "all":     elements.add("--randomize");
                        elements.add(randomize.toLowerCase());
                        break;
      }
    }
    return elements;
  }

  /**
   *  \brief  The method returns the portion of the variable transfer to Robot Framework.
   *
   *  In order to be able to use the test scripts variably, variables can be
   *  supplied with values via the command line.
   *
   *  The user can set values in the \b parameters list via corresponding
   *  ActionParameters, which are read out by this method and translated into
   *  the format for transfer to the command line.
   *  
   *  Alternatively, several variables can also be transferred as a file.
   *  In this case, the path to the file is embedded in the command line.
   *
   *  This method can be used to generate the following command line parameters in the call:
   *  \li   \c --variable <name:value><br/>Sets individual variables.
   *  \li   \c --variablefile <path:args><br/>Sets variables using variable files.
   *
   *  \note   It is assumed that the order of the variables is irrelevant for
   *          the execution of the test script. The variables are created with
   *          the corresponding values in the order in which they are stored
   *          in the list.<br/>
   *          It is not guaranteed that the order in the GUI is the same as
   *          the order in the list \b parameters .
   *
   *  @param        parameters          The list of parameters that the user
   *                                    has configured for the action
   *                                    in NeoLoad is expected here.
   *  @return       The method returns a list of arguments to be added
   *                to the call.
   */
  protected static List<String> getVariables(List<ActionParameter> parameters)
  {
    final List<String>  elements = new Vector<String>();
    Iterator<ActionParameter> it = parameters.iterator();

    while(it.hasNext())
    {
      ActionParameter current = it.next();
      if("variable".equals(current.getName().toLowerCase().trim()))
      { //  ActionParameter ist Variable und somit muss sie in Kommando eingesetzt werden.
        elements.add("--variable");
        elements.add(current.getValue());
      }
    }
    
    if( null != CommandFactory.getParameterValue(parameters, "variablefile", null) )
    { //  variableFile is defined
      elements.add("--variablefile");
      elements.add(CommandFactory.getParameterValue(parameters, "variablefile", null));
    }
    
    return elements;
  }

  /**
   *  \brief  This method interprets the specification that defines which tests are to be executed.
   *
   *  This method takes the information on the test cases from
   *  the configuration data. This can be a path or a specific file.
   *
   *  @param        parameters          The list of parameters that the user
   *                                    has configured for the action
   *                                    in NeoLoad is expected here.
   *  @return       The method returns a list of arguments to be added
   *                to the call.
   */
  protected static List<String> getTestSource(List<ActionParameter> parameters)
  {
    final List<String>  elements = new Vector<String>();
    if( null != CommandFactory.getParameterValue(parameters, RobotFrameworkAction.TEST_SOURCE, null) )
    {
      elements.add(CommandFactory.getParameterValue(parameters, RobotFrameworkAction.TEST_SOURCE, null));
    }
    return elements;
  }

  /**
   *  \brief  This method turns the configuration into the command that is executed via the command line.
   *
   *  In NeoLoad, the user can configure a specific action via parameters.
   *  The parameters, which consist of a name and a value, must be evaluated
   *  and converted into a call. The method is passed the list of parameters
   *  and returns a list of arguments.
   *
   *  /note   From the list of ActionParameters, only those for which the name
   *          is entered correctly are processed. All other action parameters
   *          are ignored.
   *  @param        parameters          The list of parameters that the user
   *                                    has configured for the action
   *                                    in NeoLoad is expected here.
   *  @return       The method returns a list of arguments from which the call
   *                for the action can then be composed.
   */
  public static List<String> buildCommand(List<ActionParameter> parameters)
  {
    final List<String>  command  = new Vector<String>();
    command.addAll(CommandWorker.getRobotCommand(parameters));
    command.addAll(CommandWorker.getOutputOptions(parameters));
    command.addAll(CommandWorker.getListenerOptions(parameters));
    command.addAll(CommandWorker.getTestExecutionOptions(parameters));
    command.addAll(CommandWorker.getVariables(parameters));
    command.addAll(CommandWorker.getTestSource(parameters));
    return command;
  }

}
