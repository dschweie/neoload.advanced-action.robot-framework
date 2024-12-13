package org.dschweie.neoload.advancedactions.command;

import java.util.List;
import java.util.Vector;

import com.neotys.extensions.action.ActionParameter;

public class CommandFactory 
{
  /**
   *  \brief  The method returns the corresponding value for a parameter.
   *
   *  In NeoLoad, the user can enter parameter and value pairs for an
   *  advanced action. These pairs are managed as instances of the
   *  ActionParameter class in a list. This list is transferred to the
   *  corresponding implementation of the ActionEngine interface.
   *
   *  This method can be used to search the list for a specific parameter. If
   *  this is found, the stored value is returned. Otherwise, the value
   *  \b substitude is returned.
   *
   *  @param  parameters      The list of action parameters in which the
   *                          \b key parameter is to be searched for is
   *                          transferred in this variable.
   *  @param  key             The name of the parameter to be searched for in
   *                          the list that is transferred in \b parameters
   *                          must be transferred in this variable.
   *  @param  substitude      In this variable, the caller should pass a value
   *                          that is to be selected as the return value if the
   *                          parameter was not found in the list.
   *
   *  @return The method returns a string that corresponds to the value found
   *          in the parameter \b key and the value \b substitude if \b key was
   *          not found in \b parameters.
   */
  public static String getParameterValue(List<ActionParameter> parameters, String key, String substitude)
  {
    String value = substitude;

    for(int i = 0; i < parameters.size(); ++i)
    {
      if(parameters.get(i).getName().equals(key))
      {
        value = parameters.get(i).getValue();
      }
    }

    return value;
  }

  public static List<String> buildOptionValueArgument(String option, String value, String noshow)
  {
    final List<String>  elements = new Vector<String>();
    if((null != value) && (!value.equals(noshow)))
    {
      elements.add(option);
      elements.add(value);
    }
    return elements;
  }
  
  public static List<String> buildCommand(String type, List<ActionParameter> parameters)
  {
    List<String> retval = null;
    
    switch(type)
    { 
      case "RobotFramework-Execute-Test" :  retval = org.dschweie.neoload.advancedactions.robotframework.factory.CommandWorker.buildCommand(parameters);
                                            break;
    }
    
    return retval;
  }
}
