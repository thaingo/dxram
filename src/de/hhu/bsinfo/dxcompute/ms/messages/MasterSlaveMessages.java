
package de.hhu.bsinfo.dxcompute.ms.messages;

/**
 * Different message types used for coordination.
 * @author Stefan Nothaas <stefan.nothaas@hhu.de> 18.02.16
 */
public class MasterSlaveMessages {
	public static final byte TYPE = 41;
	public static final byte SUBTYPE_SLAVE_JOIN_REQUEST = 1;
	public static final byte SUBTYPE_SLAVE_JOIN_RESPONSE = 2;
	public static final byte SUBTYPE_EXECUTE_TASK_REQUEST = 3;
	public static final byte SUBTYPE_EXECUTE_TASK_RESPONSE = 4;
	public static final byte SUBTYPE_SUBMIT_TASK_REQUEST = 5;
	public static final byte SUBTYPE_SUBMIT_TASK_RESPONSE = 6;
	public static final byte SUBTYPE_GET_MASTER_STATUS_REQUEST = 7;
	public static final byte SUBTYPE_GET_MASTER_STATUS_RESPONSE = 8;
	public static final byte SUBTYPE_TASK_REMOTE_CALLBACK_MESSAGE = 9;
}