package org.farynaa.servermanager;

import java.io.Console;
import java.util.Arrays;

import org.farynaa.servermanager.business.console.command.ConsoleCommandFactory;
import org.farynaa.servermanager.business.console.command.strategy.ConsoleCommandStrategy;
import org.farynaa.servermanager.business.exception.validation.TooManyStartParamsPassedException;
import org.farynaa.servermanager.business.exception.validation.console.AbstractConsoleException;

public class Application {

	private static final String APP_HELP_CONTENT = "This application works in interactive mode. You can pass your config parameters in format of java properties file as shown below. "
			+ "If you do not provide your own properites file default one will be used.\n\n"
			+ "Possible start options:\n"
			+ "server-manager help - prints this help information\n"
			+ "server-manager properties-filename\n\n"
			+ "Example of properties file:\n"
			+ "database.filename: dbhome\n"
			+ "database.name: database\n"
			+ "database.user: dbuser\n" + "database.password: dbpass";
	
	private static final String APP_WELCOME_TEXT = "Welcome to server manager.";
	private static final String CONSOLE_PROMPT = "> ";

	private String[] args;

	public static void main(String[] args) {
		new Application(args).start();
	}

	private Application(String[] args) {
		this.args = args;
	}

	private void start() {
		try {
			validateInputParamNumber();

			if (isJustPrintHelp()) {
				showAppHelp();
				return;
			}

			SpringApplicationContextInitializer.initialize(getExtraStartupParamIfExists());
			showAppWelcomeText();
			startConsoleProcessing();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void startConsoleProcessing() {
		Console console = System.console();
		boolean keepRunning = true;

		while (keepRunning) {
			String commandString = console.readLine(CONSOLE_PROMPT).trim();
			
			try {
				String[] commandParameters = extractCommandParameters(commandString);
				ConsoleCommandStrategy command = ConsoleCommandFactory.getStrategyForCommandName(commandString);
				command.process(commandParameters);
				keepRunning = command.isKeepConsoleRunning();
				
			} catch (AbstractConsoleException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private String[] extractCommandParameters(String command) {
		String[] splitedCommandString = command.split(" ");
		String[] commandParameters = removeCommandNameFromCommandString(splitedCommandString);
		return commandParameters;
	}

	private String[] removeCommandNameFromCommandString(String[] splitedCommandString) {
		String[] commandParameters = Arrays.asList(splitedCommandString).subList(1, splitedCommandString.length).toArray(new String[]{});
		return commandParameters;
	}

	private void validateInputParamNumber() {
		if (args.length > 1) {
			throw new TooManyStartParamsPassedException();
		}
	}

	private boolean isJustPrintHelp() {
		return isExtraParamPassed() && "help".equals(getExtraStartupParamIfExists());
	}

	private boolean isExtraParamPassed() {
		return args.length == 1;
	}

	private String getExtraStartupParamIfExists() {
		return isExtraParamPassed() ? args[0] : null;
	}
	
	private void showAppHelp() {
		System.out.println(APP_HELP_CONTENT);
	}
	
	private void showAppWelcomeText() {
		System.out.println(APP_WELCOME_TEXT);
	}
}
