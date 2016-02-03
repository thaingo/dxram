package de.hhu.bsinfo.dxram.run.test.nothaas;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.hhu.bsinfo.dxram.DXRAM;
import de.hhu.bsinfo.dxram.chunk.ChunkService;
import de.hhu.bsinfo.dxram.data.Chunk;
import de.hhu.bsinfo.dxram.nameservice.NameserviceService;
import de.hhu.bsinfo.utils.Pair;
import de.hhu.bsinfo.utils.main.Main;
import de.hhu.bsinfo.utils.main.MainArguments;

//before running this as a peer, start one superpeer
public class SimpleNameserviceTest extends Main {
	
	public static final Pair<String, Integer> ARG_CHUNK_SIZE = new Pair<String, Integer>("chunkSize", 76);
	public static final Pair<String, Integer> ARG_CHUNK_COUNT = new Pair<String, Integer>("chunkCount", 10);
	
	private DXRAM m_dxram;
	private ChunkService m_chunkService;
	private NameserviceService m_nameserviceService;

	public static void main(final String[] args) {
		Main main = new SimpleNameserviceTest();
		main.run(args);
	}
	
	public SimpleNameserviceTest()
	{
		m_dxram = new DXRAM();
		m_dxram.initialize("config/dxram.conf");
		m_chunkService = m_dxram.getService(ChunkService.class);
		m_nameserviceService = m_dxram.getService(NameserviceService.class);
	}
	
	@Override
	protected void registerDefaultProgramArguments(MainArguments p_arguments) {
		p_arguments.setArgument(ARG_CHUNK_SIZE);
		p_arguments.setArgument(ARG_CHUNK_COUNT);
	}

	@Override
	protected int main(MainArguments p_arguments) {
		final int size = p_arguments.getArgument(ARG_CHUNK_SIZE);
		final int chunkCount = p_arguments.getArgument(ARG_CHUNK_COUNT);
		
		Chunk[] chunks = new Chunk[chunkCount];
		int[] sizes = new int[chunkCount];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = size;
		}
		
		System.out.println("Creating chunks...");
		long[] chunkIDs = m_chunkService.create(sizes);
		
		for (int i = 0; i < chunks.length; i++) {
			chunks[i] = new Chunk(chunkIDs[i], sizes[i]);
		}
		
		Map<Long, String> verificationMap = new HashMap<Long, String>();
		
		System.out.println("Registering " + chunkCount + " chunks...");
		for (int i = 0; i < chunks.length; i++) {
			String name = new String("I-" + i);
			m_nameserviceService.register(chunks[i], name);
			verificationMap.put(chunks[i].getID(), name);
		}
		
		System.out.println("Getting chunk IDs from name service and verifying...");
		for (Entry<Long, String> entry : verificationMap.entrySet())
		{
			long chunkID = m_nameserviceService.getChunkID(entry.getValue());
			if (chunkID != entry.getKey().longValue())
			{
				System.out.println("ChunkID from name service (" + Long.toHexString(entry.getKey()) + 
						") not matching original chunk ID (" + Long.toHexString(chunkID) + "), for name " + entry.getValue() + "."); 
			}
		}
		
		System.out.println("Removing chunks from name service...");
		for (Chunk chunk : chunks)
		{
			m_nameserviceService.remove(chunk);
		}
		
		System.out.println("Done.");
		return 0;
	}
}