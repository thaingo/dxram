
package de.hhu.bsinfo.dxcompute.ms.tasks;

import java.nio.charset.StandardCharsets;

import de.hhu.bsinfo.dxcompute.ms.AbstractTaskPayload;
import de.hhu.bsinfo.dxcompute.ms.Signal;
import de.hhu.bsinfo.dxcompute.ms.TaskContext;
import de.hhu.bsinfo.utils.args.ArgumentList;
import de.hhu.bsinfo.utils.args.ArgumentList.Argument;
import de.hhu.bsinfo.utils.serialization.Exporter;
import de.hhu.bsinfo.utils.serialization.Importer;

/**
 * Print a message to the console.
 *
 * @author Stefan Nothaas <stefan.nothaas@hhu.de> 22.04.16
 */
public class PrintTaskPayload extends AbstractTaskPayload {

	private static final Argument MS_ARG_MSG =
			new Argument("msg", null, false, "Message to print.");

	private String m_msg = "";

	/**
	 * Constructor
	 */
	public PrintTaskPayload() {
		super(MasterSlaveTaskPayloads.TYPE, MasterSlaveTaskPayloads.SUBTYPE_PRINT_TASK);
	}

	/**
	 * Message to print.
	 *
	 * @param p_msg Message.
	 */
	public void setMessage(final String p_msg) {
		m_msg = p_msg;
	}

	@Override
	public int execute(final TaskContext p_ctx) {
		System.out.println(m_msg);
		return 0;
	}

	@Override
	public void handleSignal(final Signal p_signal) {
		// ignore signals
	}

	@Override
	public void terminalCommandRegisterArguments(final ArgumentList p_argumentList) {
		p_argumentList.setArgument(MS_ARG_MSG);
	}

	@Override
	public void terminalCommandCallbackForArguments(final ArgumentList p_argumentList) {
		m_msg = p_argumentList.getArgumentValue(MS_ARG_MSG, String.class);
	}

	@Override
	public void exportObject(final Exporter p_exporter) {
		super.exportObject(p_exporter);

		p_exporter.writeInt(m_msg.length());
		p_exporter.writeBytes(m_msg.getBytes(StandardCharsets.US_ASCII));
	}

	@Override
	public void importObject(final Importer p_importer) {
		super.importObject(p_importer);

		int strLength = p_importer.readInt();
		byte[] tmp = new byte[strLength];
		p_importer.readBytes(tmp);
		m_msg = new String(tmp, StandardCharsets.US_ASCII);
	}

	@Override
	public int sizeofObject() {
		return super.sizeofObject() + Integer.BYTES + m_msg.length();
	}
}
