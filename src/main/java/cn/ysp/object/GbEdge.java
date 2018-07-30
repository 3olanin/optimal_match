package cn.ysp.object;

public class GbEdge {
	//the max flow the edge can have
	private int cap;
	//the residual the edge can have
	private int residualFlow;
	//cost weight
	private double cw;
	private GbNode fromNode;
	private GbNode toNode;
	private GbEdge reverseEdge;
	//if the direction is "from Source to Sink"
	private boolean isForward;
	
	public GbEdge(GbNode fromNode, GbNode toNode){
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public GbEdge(GbNode fromNode, GbNode toNode, int cap, int residualFlow, double cw){
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.cap = cap;
		this.residualFlow = residualFlow;
		this.cw = cw;
		this.isForward = true;
		fromNode.addEdge(this);
		toNode.addEdge(this);
		
		GbEdge reverseEdge = new GbEdge(toNode,fromNode);
		reverseEdge.setCap(cap);
		reverseEdge.setResidualFlow(cap-residualFlow);
		reverseEdge.setCw(-cw);
		reverseEdge.setIsForward(false);
		fromNode.addEdge(reverseEdge);
		toNode.addEdge(reverseEdge);
		
		this.setReverseEdge(reverseEdge);
		reverseEdge.setReverseEdge(this);
	}
	
	//if node is the from node, return true
	public boolean isFromNode(GbNode node){
		
		if(fromNode == node)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//if node is the to node, return true
	public boolean isToNode(GbNode node){
		
		if(toNode == node)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getCap(){
		return cap;
	}
	
	public double getCw(){
		return cw;
	}
	
	public int getResidualFlow(){
		return residualFlow;
	}
	
	public GbNode getFromNode(){
		return fromNode;
	}
	
	public GbNode getToNode(){
		return toNode;
	}
	
	public GbEdge getReverseEdge(){
		return reverseEdge;
	}
	
	public boolean getIsForward(){
		return isForward;
	}
	
	public void setIsForward(boolean isForward){
		this.isForward = isForward;
	}
	
	public void setReverseEdge(GbEdge reverseEdge){
		this.reverseEdge = reverseEdge;
	}
	
	public void setResidualFlow(int residualFlow){
		this.residualFlow = residualFlow;
	}
	
	public void setCap(int cap){
		this.cap = cap;
	}
	
	public void setCw(double cw){
		this.cw = cw;
	}
	
	public GbNode getAnotherNode(GbNode oneNode){
		if(oneNode == fromNode){
			return toNode;
		}
		else{
			return fromNode;
		}
	}
	
	public void resetFlow(){
		this.residualFlow = this.cap;
		this.reverseEdge.setResidualFlow(this.cap-this.residualFlow);
	}
}
