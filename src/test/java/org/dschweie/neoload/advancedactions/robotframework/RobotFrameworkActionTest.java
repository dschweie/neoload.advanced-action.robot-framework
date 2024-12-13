package org.dschweie.neoload.advancedactions.robotframework;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.dschweie.neoload.advancedactions.robotframework.RobotFrameworkAction;

public class RobotFrameworkActionTest {
	@Test
	public void shouldReturnType() {
		final RobotFrameworkAction action = new RobotFrameworkAction();
		assertEquals("Robot Framework - Call Test Case", action.getType());
	}

}
