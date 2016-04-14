
package de.hhu.bsinfo.dxram.chunk.tcmds;

import java.util.ArrayList;

import de.hhu.bsinfo.dxram.chunk.ChunkService;
import de.hhu.bsinfo.dxram.data.ChunkID;
import de.hhu.bsinfo.dxram.term.AbstractTerminalCommand;
import de.hhu.bsinfo.menet.NodeID;
import de.hhu.bsinfo.utils.args.ArgumentList;
import de.hhu.bsinfo.utils.args.ArgumentList.Argument;

public class TcmdChunkList extends AbstractTerminalCommand {

	private static final Argument MS_ARG_NID =
			new Argument("nid", null, false, "Node ID of the remote peer to get the list from");

	@Override
	public String getName() {
		return "chunklist";
	}

	@Override
	public String getDescription() {
		return "Get a list of ranges from a peer holding chunks";
	}

	@Override
	public void registerArguments(final ArgumentList p_arguments) {
		p_arguments.setArgument(MS_ARG_NID);
	}

	@Override
	public boolean execute(final ArgumentList p_arguments) {
		Short nid = p_arguments.getArgumentValue(MS_ARG_NID, Short.class);
		ChunkService chunkService = getTerminalDelegate().getDXRAMService(ChunkService.class);

		ArrayList<Long> chunkRanges = chunkService.getAllLocalChunkIDRanges(nid);

		if (chunkRanges == null) {
			System.out.println("Getting chunk ranges failed.");
			return true;
		}

		System.out.println("Chunk id ranges of " + NodeID.toHexString(nid) + "(" + chunkRanges.size() / 2 + "):");
		for (int i = 0; i < chunkRanges.size(); i++) {
			long currRange = chunkRanges.get(i);
			if (i % 2 == 0) {
				System.out.print("[" + ChunkID.toHexString(currRange));
			} else {
				System.out.println(", " + ChunkID.toHexString(currRange) + "]");
			}
		}

		return true;
	}

}
