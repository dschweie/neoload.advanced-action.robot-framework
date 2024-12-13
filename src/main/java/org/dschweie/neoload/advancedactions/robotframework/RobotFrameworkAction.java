package org.dschweie.neoload.advancedactions.robotframework;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.google.common.base.Optional;
import com.neotys.extensions.action.Action;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;

public final class RobotFrameworkAction implements Action{
	private static final String BUNDLE_NAME = "org.dschweie.neoload.advancedactions.robotframework.callTest";

	final public static String  OPTION_LISTENER = "listener";
	final public static String  OUTPUT_OUTPUTDIR = "outputdir";
	final public static String  OUTPUT_DEBUGFILE = "debugfile";
	final public static String  OUTPUT_LOG = "log";
	final public static String  OUTPUT_OUTPUT = "output";
	final public static String  OUTPUT_REPORT = "report";
	final public static String  OUTPUT_XUNIT = "xunit";
	final public static String  OUTPUT_LOGLEVEL = "loglevel";
	final public static String  OUTPUT_NOSTATUSCODE = "nostatusrc";
	final public static String  OUTPUT_TIMESTAMPS = "timestampoutputs";
	final public static String  EXECUTION_EXTENSION = "extension";
	final public static String  EXECUTION_TEST = "test";
	final public static String  EXECUTION_TASK = "task";
	final public static String  EXECUTION_SUITE = "suite";
	final public static String  EXECUTION_INCLUDE = "include";
	final public static String  EXECUTION_EXCLUDE = "exclude";
	final public static String  EXECUTION_RANDOMIZE = "randomize";
	final public static String	TEST_SOURCE = "test source";

	@Override
	public String getType() {
	    return ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
	}

	@Override
	public List<ActionParameter> getDefaultActionParameters() {
		final List<ActionParameter> parameters = new ArrayList<ActionParameter>();

		parameters.add(new ActionParameter(TEST_SOURCE, "${NL-CustomResources}/"));
		parameters.add(new ActionParameter(OPTION_LISTENER, "${NL-CustomResources}/NeoLoadListener.py"));
		parameters.add(new ActionParameter("output directory", "<path to create output>"));
		parameters.add(new ActionParameter(EXECUTION_TEST, "<name or simple pattern>"));
		parameters.add(new ActionParameter("variable", "name:value"));
		
		return parameters;
	}

	@Override
	public Class<? extends ActionEngine> getEngineClass() {
		return RobotFrameworkActionEngine.class;
	}

	@Override
	public Icon getIcon() {
	    return new ImageIcon( this.getClass().getResource(ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("iconPath")),
	            ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("type")       );
	}

	@Override
	public boolean getDefaultIsHit(){
		return false;
	}

	@Override
	public String getDescription() {
		return ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("description");
	}

	@Override
	public String getDisplayName() {
	    return ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
	}

	@Override
	public String getDisplayPath() {
	    return ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<String> getMinimumNeoLoadVersion() {
	    String version = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("maximumVersion");
	    return (Optional<String>) (version.equals("absent")?Optional.absent():Optional.of(version));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<String> getMaximumNeoLoadVersion() {
	    String version = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("maximumVersion");
	    return (Optional<String>) (version.equals("absent")?Optional.absent():Optional.of(version));
	}
}
