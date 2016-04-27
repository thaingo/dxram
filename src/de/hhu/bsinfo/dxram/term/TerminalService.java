
package de.hhu.bsinfo.dxram.term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hhu.bsinfo.dxram.boot.AbstractBootComponent;
import de.hhu.bsinfo.dxram.engine.AbstractDXRAMService;
import de.hhu.bsinfo.dxram.logger.LoggerComponent;
import de.hhu.bsinfo.dxram.util.NodeRole;
import de.hhu.bsinfo.menet.NodeID;
import de.hhu.bsinfo.utils.JNIconsole;
import de.hhu.bsinfo.utils.args.ArgumentList;
import de.hhu.bsinfo.utils.args.ArgumentList.Argument;
import de.hhu.bsinfo.utils.args.ArgumentListParser;
import de.hhu.bsinfo.utils.args.DefaultArgumentListParser;

/**
 * Service providing an interactive terminal running on a DXRAM instance.
 * Allows access to implemented services, triggering commands, getting information
 * about current or remote DXRAM instances.
 * @author Stefan Nothaas <stefan.nothaas@hhu.de> 11.03.16
 */
public class TerminalService extends AbstractDXRAMService implements TerminalDelegate {
	private LoggerComponent m_logger;
	private AbstractBootComponent m_boot;
	private TerminalComponent m_terminal;

	private boolean m_loop = true;

	/**
	 * Register a new terminal command for the terminal.
	 * @param p_command
	 *            Command to register.
	 * @return True if registering was successful, false if a command with the same name already exists.
	 */
	public boolean registerCommand(final AbstractTerminalCommand p_command) {
		return m_terminal.registerCommand(p_command);
	}

	/**
	 * Run the terminal loop.
	 * Only returns if terminal was exited.
	 */
	public void loop() {
		byte[] arr;

		if (!m_boot.getNodeRole().equals(NodeRole.TERMINAL)) {
			System.out.println("A Terminal node must have the NodeRole \"terminal\". Aborting");
			return;
		}

		// register commands for autocompletion
		{
			Map<String, AbstractTerminalCommand> commands = m_terminal.getRegisteredCommands();
			String[] commandNames = commands.keySet().toArray(new String[0]);
			JNIconsole.autocompleteCommands(commandNames);
		}

		m_logger.info(getClass(), "Running terminal...");

		System.out.println("DXRAM terminal v. 0.1");
		System.out.println(
				"Running on node " + NodeID.toHexString(m_boot.getNodeID()) + ", role " + m_boot.getNodeRole());
		System.out.println("Enter '?' to list all available commands.");
		System.out.println("Use '? <command>' to get information about a command.");
		System.out.println("Use '!' or '! <command>' for interactive mode.");

		while (m_loop) {
			arr = JNIconsole
					.readline("$" + NodeID.toHexString(m_boot.getNodeID()) + "> ");
			if (arr != null) {
				String command = new String(arr, 0, arr.length);
				executeTerminalCommand(command);
			}
		}

		m_logger.info(getClass(), "Exiting terminal...");
	}

	@Override
	protected void registerDefaultSettingsService(final Settings p_settings) {

	}

	@Override
	protected boolean startService(final de.hhu.bsinfo.dxram.engine.DXRAMEngine.Settings p_engineSettings,
			final Settings p_settings) {
		m_logger = getComponent(LoggerComponent.class);
		m_boot = getComponent(AbstractBootComponent.class);
		m_terminal = getComponent(TerminalComponent.class);

		return true;
	}

	@Override
	protected boolean shutdownService() {
		m_logger = null;
		m_boot = null;
		m_terminal = null;

		return true;
	}

	@Override
	protected boolean isServiceAccessor() {
		return true;
	}

	@Override
	public void exitTerminal() {
		m_loop = false;
	}

	@Override
	public boolean areYouSure() {
		boolean ret;
		byte[] arr;

		while (true) {
			System.out.print("Are you sure (y/n)?");

			arr = JNIconsole.readline("");
			if (arr != null && arr.length > 0) {
				if (arr[0] == 'y' || arr[0] == 'Y') {
					ret = true;
					break;
				} else if (arr[0] == 'n' || arr[0] == 'N') {
					ret = false;
					break;
				}
			} else {
				ret = false;
				break;
			}
		}

		return ret;
	}

	@Override
	public String promptForUserInput(final String p_header) {
		byte[] arr = JNIconsole.readline(p_header + "> ");
		if (arr != null) {
			if (arr.length == 0) {
				return null;
			} else {
				return new String(arr, 0, arr.length);
			}
		} else {
			return null;
		}
	}

	@Override
	public <T extends AbstractDXRAMService> T getDXRAMService(final Class<T> p_class) {
		return getServiceAccessor().getService(p_class);
	}

	@Override
	public boolean executeTerminalCommand(final String p_cmdString) {
		String[] arguments;
		ArgumentListParser argsParser = new DefaultArgumentListParser();
		ArgumentList argsList = new ArgumentList();

		arguments = p_cmdString.split(" ");

		if (arguments[0].equals("?")) {
			if (arguments.length > 1) {
				final AbstractTerminalCommand c = m_terminal.getRegisteredCommands().get(arguments[1]);
				if (c == null) {
					System.out.println("error: unknown command");
					return false;
				} else {
					printUsage(c);
				}
			} else {
				System.out.println("Available commands:");
				System.out.println(getAvailableCommands());
			}
		} else if (arguments[0].equals("!") || arguments[0].equals("!")) {
			String cmdStr = null;
			if (arguments.length < 2) {
				System.out.println("Specify command for interactive mode:");
				cmdStr = promptForUserInput("command");
			} else {
				cmdStr = arguments[1];
			}
			final AbstractTerminalCommand c = m_terminal.getRegisteredCommands().get(cmdStr);
			if (c == null) {
				System.out.println("error: unknown command");
				return false;
			} else {
				argsList.clear();
				c.registerArguments(argsList);

				// trigger interactive mode
				System.out.println("Interactive argument input for '" + c.getName() + "':");
				if (!interactiveArgumentMode(argsList)) {
					System.out.println("error entering arguments");
				}

				if (!argsList.checkArguments()) {
					printUsage(c);
					return false;
				} else {
					c.setTerminalDelegate(this);
					if (!c.execute(argsList)) {
						printUsage(c);
						return false;
					}
				}
			}
		} else {
			if (arguments[0].isEmpty()) {
				return true;
			}

			final AbstractTerminalCommand c = m_terminal.getRegisteredCommands().get(arguments[0]);
			if (c == null) {
				System.out.println("error: unknown command");
				return false;
			} else {
				argsList.clear();
				c.registerArguments(argsList);
				try {
					argsParser.parseArguments(arguments, argsList);
				} catch (final Exception e) {
					System.out.println("error: parsing arguments. most likely invalid syntax");
					return false;
				}

				if (!argsList.checkArguments()) {
					printUsage(c);
					return false;
				} else {
					c.setTerminalDelegate(this);
					if (!c.execute(argsList)) {
						printUsage(c);
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Get a list of available/registered commands.
	 * @return List of registered commands.
	 */
	private String getAvailableCommands() {
		String str = new String();
		Collection<String> commands = m_terminal.getRegisteredCommands().keySet();
		List<String> sortedList = new ArrayList<String>(commands);
		Collections.sort(sortedList);
		boolean first = true;
		for (String cmd : sortedList) {
			if (first) {
				first = false;
			} else {
				str += ", ";
			}
			str += cmd;
		}

		return str;
	}

	/**
	 * Print a usage message for the specified terminal command.
	 * @param p_command
	 *            Terminal command to print usage message of.
	 */
	private void printUsage(final AbstractTerminalCommand p_command) {
		ArgumentList argList = new ArgumentList();
		// create default argument list
		p_command.registerArguments(argList);

		System.out.println("Command '" + p_command.getName() + "':");
		System.out.println(p_command.getDescription());
		System.out.println(argList.createUsageDescription(p_command.getName()));
	}

	/**
	 * Execute interactive argument mode to allow the user entering arguments for a command one by one.
	 * @param p_arguments
	 *            List of arguments with arguments that need values to be entered.
	 * @return If user entered arguments properly, false otherwise.
	 */
	private boolean interactiveArgumentMode(final ArgumentList p_arguments) {
		// ask for non optional entries first
		for (Entry<String, Argument> entry : p_arguments.getArgumentMap().entrySet()) {
			Argument arg = entry.getValue();
			if (!arg.isOptional()) {
				String input = promptForUserInput("<" + arg.getKey() + "> ");
				if (input == null) {
					return false;
				}
				p_arguments.setArgument(arg.getKey(), input, "");
			}
		}

		// now go for optional entries
		for (Entry<String, Argument> entry : p_arguments.getArgumentMap().entrySet()) {
			Argument arg = entry.getValue();
			if (arg.isOptional()) {
				String input = promptForUserInput("[" + arg.getKey() + "] ");
				if (input != null) {
					p_arguments.setArgument(arg.getKey(), input, "");
				}
			}
		}

		return true;
	}
}
