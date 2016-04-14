
package de.hhu.bsinfo.dxram.chunk.tcmds;

import de.hhu.bsinfo.dxram.chunk.ChunkService;
import de.hhu.bsinfo.dxram.data.ChunkID;
import de.hhu.bsinfo.dxram.term.AbstractTerminalCommand;
import de.hhu.bsinfo.utils.args.ArgumentList;
import de.hhu.bsinfo.utils.args.ArgumentList.Argument;

public class TcmdChunkRemove extends AbstractTerminalCommand {

	private static final Argument MS_ARG_CID = new Argument("cid", null, true, "Full chunk id of the chunk to remove");
	private static final Argument MS_ARG_LID = new Argument("lid", null, true,
			"Local id of the chunk to remove. If missing node id, current node is assumed");
	private static final Argument MS_ARG_NID =
			new Argument("nid", null, true, "Node id to remove the chunk with specified local id");

	@Override
	public String getName() {
		return "chunkremove";
	}

	@Override
	public String getDescription() {
		return "Remove an existing chunk. Usable with either full chunk id or split into nid and lid.";
	}

	@Override
	public void registerArguments(final ArgumentList p_arguments) {
		p_arguments.setArgument(MS_ARG_CID);
		p_arguments.setArgument(MS_ARG_LID);
		p_arguments.setArgument(MS_ARG_NID);
	}

	@Override
	public boolean execute(ArgumentList p_arguments) {
		Long cid = p_arguments.getArgumentValue(MS_ARG_CID, Long.class);
		Long lid = p_arguments.getArgumentValue(MS_ARG_LID, Long.class);
		Short nid = p_arguments.getArgumentValue(MS_ARG_NID, Short.class);

		ChunkService chunkService = getTerminalDelegate().getDXRAMService(ChunkService.class);

		// we favor full cid
		if (cid != null) {
			// don't allow removal of index chunk
			if (ChunkID.getLocalID(cid) == 0) {
				System.out.println("Removal of index chunk is not allowed.");
				return true;
			}

			if (chunkService.remove(cid) != 1) {
				System.out.println("Removing chunk with ID " + ChunkID.toHexString(cid) + " failed.");
				return true;
			}
		} else {
			if (lid != null) {
				// don't allow removal of index chunk
				if (lid == 0) {
					System.out.println("Removal of index chunk is not allowed.");
					return true;
				}

				// check for remote id, otherwise we assume local
				if (nid == null) {
					System.out.println("error: missing nid for lid");
					return false;
				}

				// create cid
				cid = ChunkID.getChunkID(nid, lid);
				if (chunkService.remove(cid) != 1) {
					System.out.println("Removing chunk with ID " + ChunkID.toHexString(cid) + " failed.");
					return true;
				}
			} else {
				System.out.println("No cid or nid/lid specified.");
				return false;
			}
		}

		System.out.println("Chunk " + ChunkID.toHexString(cid) + " removed.");
		return true;
	}
}
