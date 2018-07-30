package cn.ysp.object;

import java.util.ArrayList;
import java.util.List;

public class GbNode {
	
	private int nodeId;
	private List<GbEdge> gbEdgeList;
	private boolean ifAvailable;
	private static int initNodeId = 0;
	public String node_type;
	
	private static int generateId(){
		return initNodeId ++;
	}
	
	public GbNode(){
		this.nodeId = generateId();
		gbEdgeList = new ArrayList<GbEdge>();
		this.ifAvailable = true;
	}
	
	public boolean addEdge(GbEdge gbEdge){
		return gbEdgeList.add(gbEdge);
	}
	public int getNodeId(){
		return nodeId;
	}
	
	public String getNodeType(){
		return node_type;
	}
	
	public List<GbEdge> getEdgeList()
	{
		return gbEdgeList;
	}
	
	public boolean ifAvailable()
	{
		return this.ifAvailable;
	}
	
	public void setAvailable()
	{
		this.ifAvailable = true;
	}
	
	public void setUnavailable()
	{
		this.ifAvailable = false;
	}
}
