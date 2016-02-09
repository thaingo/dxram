package de.hhu.bsinfo.dxgraph.gen;

import de.hhu.bsinfo.dxgraph.GraphTask;

public abstract class GraphGenerator extends GraphTask
{
	private int m_numNodes = 1;
	
	public GraphGenerator()
	{
		
	}
	
	// cluster nodes to be precise
	public void setNumNodes(final int p_numNodes)
	{
		m_numNodes = p_numNodes;
	}
	
	@Override
	public boolean execute() {
		log("Executing graph generation for " + m_numNodes + " nodes.");
		boolean ret = generate(m_numNodes);
		if (ret) {
			log("Executing graph generation successful.");
		} else {
			logError("Executing graph generation failed.");
		}
		
		return ret;
	}
	
	public abstract boolean generate(final int p_numNodes);
}