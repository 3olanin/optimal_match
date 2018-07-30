package cn.ysp.object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import cn.ysp.events.RequestCreatedEvent;
import cn.ysp.map.Neo4jMap;

import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Node;

import cn.ysp.optimal_match.PathManager;
import cn.ysp.optimal_match.StaticMatch;
import cn.ysp.optimal_match.WorkManager;

public class BipartiteGraph2 {
	
	GbNode gbSource;
	GbNode gbSink;
	List<GbNode> nodeList;
	List<GbCar> newCarList;
	List<GbCar> idleCarList;
	List<GbCar> onWorkCarList;
	List<GbRequest> requestNodeList;
	List<GbRequest> newRequestNodeList;

	public BipartiteGraph2(){
		gbSource = new GbNode();
		gbSink = new GbNode();
		nodeList = new ArrayList<GbNode>();
		newCarList = new ArrayList<GbCar>();
		idleCarList = new ArrayList<GbCar>();
		onWorkCarList = new ArrayList<GbCar>();
		requestNodeList = new ArrayList<GbRequest>();
		newRequestNodeList = new ArrayList<GbRequest>();
		this.getNodeList().add(gbSource);
		this.getNodeList().add(gbSink);
	}
	
	public GbNode getGbSource(){
		return gbSource;
	}
	
	public GbNode getGbSink(){
		return gbSink;
	} 
	
	public List<GbNode> getNodeList(){
		return nodeList;
	}
	
	public List<GbCar> getIdleCarList(){
		return idleCarList;
	}
	
	public List<GbCar> getOnWorkCarList(){
		return onWorkCarList;
	}
	
	public List<GbRequest> getRequestNodeList(){
		return requestNodeList;
	}
	
	public List<GbRequest> getNewRequestNodeList(){
		return newRequestNodeList;
	}
	
	public List<GbCar> getNewCarList(){
		return newCarList;
	}
	
	public void addNode(GbNode node){
		this.getNodeList().add(node);
		if(node.getNodeType()=="car_node"){
			this.idleCarList.add((GbCar) node);
		}
		else if(node.getNodeType()=="request_node"){
			this.requestNodeList.add((GbRequest) node);
		}
	}
	
	public GbEdge addEdgeBetweenNode(GbNode fromNode,GbNode toNode){
		GbEdge edge = new GbEdge(fromNode,toNode);
		fromNode.addEdge(edge);
		toNode.addEdge(edge);
		return edge;
	}
	
	public void carFromIdleToWork(GbCar car){
		idleCarList.remove(car);
		onWorkCarList.add(car);
	}
	
	public void carFromWorkToIdle(GbCar car){
		onWorkCarList.remove(car);
		idleCarList.add(car);
	}
	

	
	public GbEdge addEdgeBetweenNode(GbNode fromNode,GbNode toNode,int cap,int residualFlow, double cw){
		GbEdge edge = new GbEdge(fromNode,toNode,cap,residualFlow,cw);
		return edge;
	}
	
	public void loadAllCarNode(String fileName, Neo4jMap n4jMap) throws NumberFormatException, IOException{
		
		//read carnode file
		File carNodeFile=new File(fileName);
		InputStreamReader read = new InputStreamReader(new FileInputStream(carNodeFile));
		BufferedReader bufferedReader = new BufferedReader(read);
		String lineTxt = "";
		while((lineTxt = bufferedReader.readLine()) != null){
			String s[]=lineTxt.split("#");
			double slon=Float.valueOf(s[0]);
			double slat=Float.valueOf(s[1]);

		    //create new car node and add it into the graph
			Node startNode = StaticMatch.locateOsmNode(slon, slat, n4jMap);
			GbCar carNode = new GbCar(startNode);
			this.addNode(carNode);
			this.addEdgeBetweenNode(gbSource, carNode, 1, 1, 0);

		}
	}
	
	public static GbRequest generateRequestNodeByEvent(RequestCreatedEvent e){
		GbRequest r = new GbRequest(e.getOlon(),e.getOlat(),e.getDlon(),e.getDlat(),e.getT1(),e.getT2(),e.getEventTime());
		return r;
	}
	
	public void deleteRequestNode(GbNode node){
		List<GbEdge> edgeList = node.getEdgeList();
		Iterator it = edgeList.iterator();
		while(it.hasNext()){
			GbEdge edge = (GbEdge) it.next();
			GbNode anotherNode = edge.getAnotherNode(node);
			anotherNode.getEdgeList().remove(edge);
		}
		this.getNodeList().remove(node);
		this.getRequestNodeList().remove(node);
	}
	
	public void deleteCarNodeWithoutIdleList(GbNode node){
		List<GbEdge> edgeList = node.getEdgeList();
		Iterator it = edgeList.iterator();
		while(it.hasNext()){
			GbEdge edge = (GbEdge) it.next();
			GbNode anotherNode = edge.getAnotherNode(node);
			anotherNode.getEdgeList().remove(edge);
		}
		this.getNodeList().remove(node);
		//this.getIdleCarList().remove(node);
	}
	
	public GbEdge getEdgeFromCtoQ(GbCar c, GbRequest r){
		List<GbEdge> eList = c.getEdgeList();
		Iterator it = eList.iterator();
		while(it.hasNext()){
			GbEdge e = (GbEdge) it.next();
			if(e.getAnotherNode(c) == r && e.getFromNode() == c){
				return e;
			}
		}
		return null;
	}
}
