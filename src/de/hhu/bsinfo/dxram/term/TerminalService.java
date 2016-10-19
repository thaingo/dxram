
package de.hhu.bsinfo.dxram.term;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.hhu.bsinfo.dxram.boot.AbstractBootComponent;
import de.hhu.bsinfo.dxram.engine.AbstractDXRAMService;
import de.hhu.bsinfo.dxram.logger.LoggerComponent;
import de.hhu.bsinfo.dxram.util.NodeRole;
import de.hhu.bsinfo.menet.NodeID;
import de.hhu.bsinfo.utils.JNIconsole;

/**
 * Service providing an interactive terminal running on a DXRAM instance.
 * Allows access to implemented services, triggering commands, getting information
 * about current or remote DXRAM instances. The command line interface is basically a java script interpreter with
 * a few built in special commands
 *
 * @author Stefan Nothaas <stefan.nothaas@hhu.de> 11.03.16
 */
public class TerminalService extends AbstractDXRAMService {
	private LoggerComponent m_logger;
	private AbstractBootComponent m_boot;
	private TerminalComponent m_terminal;

	private boolean m_loop = true;
	private BufferedWriter m_historyFile;

	private String m_autostartScript;

	/**
	 * Constructor
	 */
	public TerminalService() {
		super("term");
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

		// register commands for auto completion
		JNIconsole.autocompleteCommands(m_terminal.getRegisteredCommands().keySet().toArray(new String[0]));

		// #if LOGGER >= INFO
		m_logger.info(getClass(), "Running terminal...");
		// #endif /* LOGGER >= INFO */

		System.out.println(">>> DXRAM Terminal <<<");
		System.out.println(
				"Running on node " + NodeID.toHexString(m_boot.getNodeID()) + ", role " + m_boot.getNodeRole());
		System.out.println("Type '?' or 'help' to print the help message");

		// auto start script file
		if (!m_autostartScript.isEmpty()) {
			System.out.println("Running auto start script " + m_autostartScript);
			if (!m_terminal.getScriptContext().load(m_autostartScript)) {
				System.out.println("Running auto start script failed");
			} else {
				System.out.println("Running auto start script complete");
			}
		}

		while (m_loop) {
			arr = JNIconsole
					.readline("$" + NodeID.toHexString(m_boot.getNodeID()) + "> ");
			if (arr != null) {
				String command = new String(arr, 0, arr.length);

				try {
					if (m_historyFile != null) {
						m_historyFile.write(command + "\n");
					}
				} catch (IOException e) {
					// #if LOGGER >= ERROR
					m_logger.error(getClass(), "Writing history file failed", e);
					// #endif /* LOGGER >= ERROR */
				}

				evaluate(command);
			}
		}

		// #if LOGGER >= INFO
		m_logger.info(getClass(), "Exiting terminal...");
		// #endif /* LOGGER >= INFO */
	}

	/**
	 * Evaluate the text entered in the terminal.
	 *
	 * @param p_text Text to evaluate
	 */
	private void evaluate(final String p_text) {

		// skip empty
		if (p_text.isEmpty()) {
			return;
		}

		if (p_text.startsWith("?")) {
			m_terminal.getScriptTerminalContext().help();
		} else if (p_text.equals("exit")) {
			m_loop = false;
		} else if (p_text.equals("clear")) {
			// ANSI escape codes (clear screen, move cursor to first row and first column)
			System.out.print("\033[H\033[2J");
			System.out.flush();
		} else {
			eveluateCommand(p_text);
		}
	}

	/**
	 * Evaluate the terminal command
	 *
	 * @param p_text Text to evaluate as terminal command
	 */
	private void eveluateCommand(final String p_text) {
		// resolve terminal cmd "macros"
		String[] tokensFunc = p_text.split("\\(");
		String[] tokensHelp = p_text.split(" ");

		// print help for cmd
		if (tokensHelp.length > 1 && tokensHelp[0].equals("help")) {
			de.hhu.bsinfo.dxram.script.ScriptContext scriptCtx = m_terminal.getRegisteredCommands().get(tokensHelp[1]);
			if (scriptCtx != null) {
				m_terminal.getScriptContext().eval("dxterm.cmd(\"" + tokensHelp[1] + "\").help()");
			} else {
				System.out.println("Could not find help for terminal command '" + tokensHelp[1] + "'");
			}
		} else if (tokensFunc.length > 1) {

			// resolve cmd call
			de.hhu.bsinfo.dxram.script.ScriptContext scriptCtx = m_terminal.getRegisteredCommands().get(tokensFunc[0]);
			if (scriptCtx != null) {
				// load imports
				m_terminal.getScriptContext().eval("dxterm.cmd(\"" + tokensFunc[0] + "\").imports()");

				// assemble long call
				String call = "dxterm.cmd(\"" + tokensFunc[0] + "\").exec(";

				// prepare parameters
				if (tokensFunc[1].length() > 1) {
					call += tokensFunc[1];
				} else {
					call += ")";
				}

				m_terminal.getScriptContext().eval(call);
			} else {
				m_terminal.getScriptContext().eval(p_text);
			}
		} else {
			// filter some generic "macros"
			if (p_text.equals("help")) {
				m_terminal.getScriptTerminalContext().help();
			} else {
				m_terminal.getScriptContext().eval(p_text);
			}
		}
	}

	@Override
	protected void registerDefaultSettingsService(final Settings p_settings) {
		p_settings.setDefaultValue(TerminalConfigurationValues.Service.AUTOSTART_SCRIPT);
	}

	@Override
	protected boolean startService(final de.hhu.bsinfo.dxram.engine.DXRAMEngine.Settings p_engineSettings,
			final Settings p_settings) {
		m_logger = getComponent(LoggerComponent.class);
		m_boot = getComponent(AbstractBootComponent.class);
		m_terminal = getComponent(TerminalComponent.class);

		if (m_boot.getNodeRole() == NodeRole.TERMINAL) {
			loadHistoryFromFile("dxram_term_history");
		}

		m_autostartScript = p_settings.getValue(TerminalConfigurationValues.Service.AUTOSTART_SCRIPT);

		return true;
	}

	@Override
	protected boolean shutdownService() {
		m_logger = null;
		m_boot = null;
		m_terminal = null;

		if (m_historyFile != null) {
			try {
				m_historyFile.close();
			} catch (final IOException ignored) {
			}
		}

		return true;
	}

	/**
	 * Load terminal command history from a file.
	 *
	 * @param p_file File to load the history from and append new commands to.
	 */
	private void loadHistoryFromFile(final String p_file) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(p_file));

			// import history if found
			String str;
			while (true) {
				try {
					str = reader.readLine();
				} catch (IOException e) {
					break;
				}

				if (str == null) {
					break;
				}

				JNIconsole.addToHistory(str);
			}

			reader.close();
		} catch (final FileNotFoundException e) {
			// #if LOGGER >= DEBUG
			m_logger.debug(getClass(), "No history found: " + p_file);
			// #endif /* LOGGER >= DEBUG */
		} catch (final IOException e) {
			// #if LOGGER >= ERROR
			m_logger.error(getClass(), "Reading history " + p_file + " failed", e);
			// #endif /* LOGGER >= ERROR */
		}

		try {
			m_historyFile = new BufferedWriter(new FileWriter(p_file, true));
		} catch (final IOException e) {
			m_historyFile = null;
			// #if LOGGER >= WARN
			m_logger.warn(getClass(), "Opening history " + p_file + " for writing failed", e);
			// #endif /* LOGGER >= WARN */
		}
	}
}
