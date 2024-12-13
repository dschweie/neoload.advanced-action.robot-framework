package org.dschweie.neoload.advancedactions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

/**
 *  \brief  General implementation for executing an Advanced Action.
 *    
 *  The class uses ActionEngine to implement the ActionEngine interface, which
 *  is required by NeoLoad so that NeoLoad can call and stop an Advanced Action.
 */
public abstract class AbstractActionEngine implements ActionEngine 
{
  /**
   *  \brief    The static method creates error entries in NeoLoad and returns a corresponding result.
   *
   *  This method can be used to log an error that occurred during
   *  processing of the Advanced Action in NeoLoad and additionally
   *  generates a SampleResult that can be used as the return value
   *  of the Advanced Action.
   *
   *  @param    context       The parameter must contain an instance
   *                          through which the method can access
   *                          information from NeoLoad at runtime.
   *  @param    result        The parameter expects the current instance
   *                          of SampleResult to be returned to NeoLoad
   *                          by the execute method.
   *  @param    code          The error code that is to be entered in the
   *                          result must be passed in the parameter. This
   *                          error code can be freely selected and must be
   *                          passed as a string.
   *  @param    errorMessage  The parameter must contain an error message
   *                          that should be logged together with the
   *                          error code.
   *  @param    exception     The parameter can optionally contain an
   *                          exception that was caught in connection with
   *                          the processing.
   *
   *  @return   For convenience, the method returns the modified instance \em result.
   */
  protected static SampleResult getErrorResult(final Context context, final SampleResult result, final String code, final String errorMessage, final Exception exception)
  {
    result.setError(true);
    result.setStatusCode(code);
    if(exception != null){
      if(null != context)
        context.getLogger().error(errorMessage, exception);
      else
        System.out.println(errorMessage.concat("\n\n").concat(exception.getLocalizedMessage()));
    } else{
      if(null != context)
        context.getLogger().error(errorMessage);
      else
        System.out.println(errorMessage);
    }
    return result;
  }
	  
  /**
   *  \brief    Instance variable for accessing the started process.
   *
   *  This abstract class starts an application via a command line call.
   *  A reference to the process is kept in this instance variable.
   */
  protected Process       process = null;

  /**
   *  \brief    Instance variable that holds information about the invocation of the process.
   */
  protected StringBuilder requestBuilder = new StringBuilder();

  /**
   *  \brief    Instance variable in which outputs of the process are held.
   */
  protected StringBuilder responseBuilder = new StringBuilder();
  /**
   *  \brief    Instance variable that holds information about the result of the action
   */
  protected SampleResult  sampleResult = new SampleResult();

  /**
   *  \brief    Default constructor for engine classes.
   *
   *  This class is the most general possible implementation of an
   *  engine class that is called to execute an advanced action.
   *
   *  The default constructor instantiates the instance variables that are used to output
   *  \li   request content,
   *  \li   response content and
   *  \li   result of the action.
   */
  public AbstractActionEngine()
  {
    this.sampleResult = new SampleResult();
    this.requestBuilder = new StringBuilder();
    this.responseBuilder = new StringBuilder();
  }

  /**
   *  \brief    Method creates the command from a list of strings.
   *
   *  The method creates the command that is to be called via the command line
   *  from a list of individual strings that represent parameters and
   *  their values.
   *
   *  @return   The method returns the command as a string.
   */
  protected String buildProcessCallToString(List<String> command)
  {
    String str = "";

    for(int i = 0; i < command.size(); ++i)
      if(0 < i)
        str = str.concat(" ".concat(command.get(i)));
      else
        str = str.concat(command.get(i));

    return str;
  }

  /**
   *  \brief    General method for processing a command.
   *
   *  After developing the second Advanced Action for NeoLoad yourself, it
   *  quickly becomes apparent that the processing sequence is almost always
   *  the same. For this reason, a general form of processing was implemented
   *  within this class.
   *
   *  @param    context       An instance must be passed in the parameter
   *                          through which the method has access to information
   *                          from NeoLoad at runtime. The instance is passed
   *                          by NeoLoad via the method
   *                          ActionEngine.execute(Context, List<ActionParameter>).
   *  @param    command       This parameter expects a list containing the
   *                          components of the command.
   *
   *  @return   The method returns the result of the execution as an instance
   *            of the SampleRequest class. This instance is expected
   *            by NeoLoad as feedback.
   *            If a value is passed in the \em forceCode parameter, then this
   *            value is returned.
   */
  protected SampleResult executeProcess(Context context, List<String> command)
  {
    return this.executeProcess(context, command, true, true, null);
  }

  /**
   *  \brief    General method for processing a command.
   *
   *  After developing the second Advanced Action for NeoLoad yourself, it
   *  quickly becomes apparent that the processing sequence is almost always
   *  the same. For this reason, a general form of processing was implemented
   *  within this class.
   *
   *  @param    context       An instance must be passed in the parameter
   *                          through which the method has access to information
   *                          from NeoLoad at runtime. The instance is passed
   *                          by NeoLoad via the method
   *                          ActionEngine.execute(Context, List<ActionParameter>).
   *  @param    command       This parameter expects a list containing the
   *                          components of the command.
   *  @param    waitForProcess  This parameter controls whether the method
   *                          should wait for the end of command processing.
   *  @param    isMainProcess This parameter controls whether the called command
   *                          should be interpreted as the main process
   *                          of the action.
   *  @param    forceCode     A fixed return value can be specified via the
   *                          parameter. This can be of interest if the
   *                          process does not return a value or returns
   *                          an unsuitable value.
   *
   *  @return   The method returns the result of the execution as an instance
   *            of the SampleRequest class. This instance is expected
   *            by NeoLoad as feedback.
   *            If a value is passed in the \em forceCode parameter, then this
   *            value is returned.
   */
  protected SampleResult executeProcess(Context context, List<String> command, boolean waitForProcess, boolean isMainProcess, String forceCode)
  {
    SampleResult result = new SampleResult();
    Process currentProcess = null;

    try {
	        
      // log the concrete call
      this.reportProcessCall(command);
      if(isMainProcess)
        this.reportToResponse("<?xml version=\"1.0\"?>");

      // run action as a external process
      result.sampleStart();
      currentProcess = new ProcessBuilder(command).start();
      if(isMainProcess)
        this.process = currentProcess;
      if(waitForProcess)
        currentProcess.waitFor();
      result.sampleEnd();

      // log the results
      if(waitForProcess)
      {
            if(null == forceCode)
              result.setStatusCode(String.valueOf(currentProcess.exitValue()));
            else
              result.setStatusCode(forceCode);
      }
      else
      { //  in this case the end of process will not be observed, so exit code is set to 0
        result.setStatusCode("0");
      }
      this.reportProcessInput(context, currentProcess);
      this.responseBuilder.append("\n<exitcode>".concat(result.getStatusCode()).concat("</exitcode>"));
    }
    catch (IOException e1)
    {
      result.sampleEnd();
      result.setStatusCode(e1.getClass().getSimpleName());
      // this.reportProcessInput(context, currentProcess);
      this.responseBuilder.append("<exception>".concat(e1.getLocalizedMessage()).concat("</exception>"));
    }
    catch (InterruptedException e)
    {
      result.sampleEnd();
      result.setStatusCode(e.getClass().getSimpleName());
      // this.reportProcessInput(context, currentProcess);
      this.responseBuilder.append("<exception>".concat(e.getLocalizedMessage()).concat("</exception>"));
    }

    // update the result object
    result.setError(!("0".equals(result.getStatusCode())));
    if(result.isError())
      this.reportProcessErrors(context, currentProcess);
    result.setRequestContent(this.requestBuilder.toString());
    result.setResponseContent(this.responseBuilder.toString());

    if(null == context)
      System.out.println(result.getRequestContent().concat(" => ").concat(result.getStatusCode()));

    return result;
  }

  /**
   *  \brief    Method for processing a command as a subprocess.
   *
   *  Complex actions consist of the execution of more than one command.
   *  In this case, one command is understood as the main process and
   *  the other commands as subprocesses.
   *
   *  @param    context       An instance must be passed in the parameter
   *                          through which the method has access to information
   *                          from NeoLoad at runtime. The instance is passed
   *                          by NeoLoad via the method
   *                          ActionEngine.execute(Context, List<ActionParameter>).
   *  @param    command       This parameter expects a list containing the
   *                          components of the command.
   *
   *  @return   The method returns the result of the execution as an instance
   *            of the SampleRequest class. This result is to be combined with
   *            the other results from the main process.
   */
  protected SampleResult executeSubprocess(Context context, List<String> command)
  {
    return this.executeProcess(context, command, true, false, null);
  }

  /**
   *  \brief    Method for processing a command as a subprocess.
   *
   *  Complex actions consist of the execution of more than one command.
   *  In this case, one command is understood as the main process and
   *  the other commands as subprocesses.
   *
   *  @param    context       An instance must be passed in the parameter
   *                          through which the method has access to information
   *                          from NeoLoad at runtime. The instance is passed
   *                          by NeoLoad via the method
   *                          ActionEngine.execute(Context, List<ActionParameter>).
   *  @param    command       This parameter expects a list containing the
   *                          components of the command.
   *  @param    waitForProcess  This parameter controls whether the method
   *                          should wait for the end of command processing.
   *
   *  @return   The method returns the result of the execution as an instance
   *            of the SampleRequest class. This result is to be combined with
   *            the other results from the main process.
   */
  protected SampleResult executeSubprocess(Context context, List<String> command, boolean waitForProcess)
  {
    return this.executeProcess(context, command, waitForProcess, false, null);
  }

  /**
   *  \brief    Reporting method that logs a command in the request output.
   *
   *  An advanced action consists of a request and a response in the NeoLoad
   *  runtime environment.
   *
   *  The call of a command is interpreted as a request and the call is logged
   *  using this method. This allows the user to understand exactly what the
   *  call looks like at runtime.
   *
   *  @param    command       This parameter expects a list containing the
   *                          components of the command.
   */
  protected void reportProcessCall(List<String> command)
  {
    if(0 < this.requestBuilder.length())
      this.requestBuilder.append("\n");
    this.requestBuilder.append(this.buildProcessCallToString(command));
  }

  /**
   *  \brief    Reporting method that logs errors from the process directly in NeoLoad.
   *
   *  The errors that occurred in connection with the processing of a command
   *  are written directly to the log file in the NeoLoad runtime environment
   *  using this method.
   *
   *  The error message is also recorded in the response.
   *
   *  @param    context       An instance must be passed in the parameter
   *                          through which the method has access to
   *                          information from NeoLoad at runtime.
   *                          The instance is passed by NeoLoad via the method
   *                          ActionEngine.execute(Context, List<ActionParameter>).
   *  @param    process       The parameter must contain the instance that
   *                          represents the execution of a command and whose
   *                          output is to be logged via the command line.
   */
  protected void reportProcessErrors(Context context, Process process)
  {
    try
    {
      String message = "";
      InputStream output = process.getErrorStream();

      if(null != output)
      {
        int c = output.read();
        while(-1 != c)
        {
          message = message.concat(String.valueOf((char) c));
          c = output.read();
        }
        output.close();

        //  write message to reponse of the action
        if(0 < this.responseBuilder.length())
          this.responseBuilder.append("\n");
        this.responseBuilder.append("<errormessage>\n".concat(message).concat("</errormessage>"));

        //  write message to logfile of NeoLoad
        if(null!=context)
          context.getLogger().error(message);
        else
          System.out.println("reportProcessErrors: ".concat(message));
      }
    }
    catch (IOException e)
    {
      this.responseBuilder.append("<exception>".concat(e.getLocalizedMessage()).concat("</exception>"));
      context.getLogger().error(e.getLocalizedMessage(), e);
    }
  }

  /**
   *  \brief    Reporting method for logging output via the command line.
   *
   *  Calling QF-Test via the command line sometimes results in output in the
   *  console. This output is interpreted by this action as part of the
   *  response and is therefore logged in the response.
   *
   *  @param    context       An instance must be passed in the parameter
   *                          through which the method has access to
   *                          information from NeoLoad at runtime.
   *                          The instance is passed by NeoLoad via the method
   *                          ActionEngine.execute(Context, List<ActionParameter>).
   *  @param    process       The parameter must contain the instance that
   *                          represents the execution of a command and whose
   *                          output is to be logged via the command line.
   */
  protected void reportProcessInput(Context context, Process process)
  {
    try
    {
      InputStream output = process.getInputStream();

      if(0 < this.responseBuilder.length())
        this.responseBuilder.append("\n");

      this.responseBuilder.append("<console>\n");
      if(null != output)
      {
        int c = output.read();
        while(-1 != c)
        {
          this.responseBuilder.append((char) c);
          c = output.read();
        }
        output.close();
      }

      this.responseBuilder.append("</console>");
    }
    catch (IOException e)
    {
      this.responseBuilder.append("<exception>".concat(e.getLocalizedMessage()).concat("</exception>"));
    }
  }

  /**
   *  \brief    Reporting method for logging a text in the action request.
   *
   *  Using this method, the text passed in the \em message parameter can be
   *  entered into the logging of the action's request.
   *
   *  If the request is not empty, the \em message is preceded by a line break
   *  and appended to the existing content.
   *
   *  @param    message       The parameter must contain the text that is to
   *                          be logged.
   */
  protected void reportToRequest(String message)
  {
    if(0 < this.requestBuilder.length())
      this.requestBuilder.append("\n");
    this.requestBuilder.append(message.toString());
  }

  /**
   *  \brief    Reporting method for logging a text in the action's response.
   *
   *  Using this method, the text passed in the \em message parameter can be
   *  entered into the logging of the action's request.
   *
   *  If the response is not empty, the \em message is preceded by a line break
   *  and appended to the existing content.
   *
   *  @param    message       The parameter must contain the text that is to
   *                          be logged.
   */
  protected void reportToResponse(String message)
  {
    if(0 < this.responseBuilder.length())
      this.responseBuilder.append("\n");
    this.responseBuilder.append(message.toString());
  }

  /**
   *  \brief    Method called by NeoLoad when the test is stopped.
   *
   *  The user can stop tests immediately in the NeoLoad interface. In this
   *  case, Advanced Actions must also be stopped immediately.
   *
   *  In this method, the running main process is terminated.
   */
  @Override
  public void stopExecute()
  {
    this.process.destroy();
  }
}
