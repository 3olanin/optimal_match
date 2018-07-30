package cn.ysp.optimal_match;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;

import cn.ysp.events.Event;
import cn.ysp.events.EventsGenerator;
import cn.ysp.events.RequestCreatedEvent;
import cn.ysp.map.Neo4jMap;
import cn.ysp.object.BipartiteGraph;
import cn.ysp.object.GbCar;
import cn.ysp.object.GbEdge;
import cn.ysp.object.GbNode;
import cn.ysp.object.GbRequest;
import cn.ysp.object.Recorder;

public class Simulator2 {

	public static String NEO4JPATH = "D:/Neo4jDB_2_2";
	//public static String CARNODE_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\carNodeFile.txt";
	public static String CARNODE_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\carFile.txt";
	//public static String REQUEST_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\request.txt";
	public static String REQUEST_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\requestFile2.txt";
	//update interval
	public static int ut = 30000;
	public static long DATE_14_7_1 = 1404144000000L;
	//world clock,ms    2014/7/1 00:00:00
	public static long clock = DATE_14_7_1;
	//the interval for make a appointment request
	public static int T1_inter = 300000;
	//the interval for the ready set of request
	public static int T2_inter = 180000;
	//U is a predefined factor to normalised the cost of time for c travelling to pickup q
	public static double U = 300000;
	//K ≥ 1 is a parameter amplifying the utility of serving an appointment request
	public static double K = 1.5;
	//0<=x_factor<=1 is a predefined balance factor
	public static double x_factor = 0.5;

	
	public static void main(String[] args) throws NumberFormatException, IOException, ParseException, SQLException {
		
		//load noe4j map
		Neo4jMap n4jMap = new Neo4jMap(NEO4JPATH);
		//init BipartiteGraph and source&sink node
		BipartiteGraph bg = new BipartiteGraph();
		//load car node file
		bg.loadAllCarNode(CARNODE_FILE, n4jMap);
		//load request node file and generate request event
		EventsGenerator eventsGenerator = new EventsGenerator();
		eventsGenerator.loadRequestFile(REQUEST_FILE);
		Recorder recorder = new Recorder();
		int minute_id = 0;

		minute_id = calculateMinuteId(clock);
		
		int requiredFlow;
		//StaticMatch.calculateAllCW(bg, n4jMap, minute_id);
		//int requiredFlow = Math.max(bg.getIdleCarList().size(), bg.getRequestNodeList().size());
		//StaticMatch.maxFlow(requiredFlow, bg);
		//WorkManager.assignWork(bg, n4jMap, minute_id, recorder);


//		while(clock < DATE_14_7_1 + 86400000){
		while(clock < DATE_14_7_1 + 5400000){
//		while(clock < DATE_14_7_1 + 4380000){
			clock+=ut;
			minute_id = calculateMinuteId(clock);

			List<GbEdge> candiList = new ArrayList<GbEdge>();
			
			eventsGenerator.eventCheck(clock, bg, recorder);
			
			//calculateCW and add edge
			StaticMatch2.calculateAllCW(bg, n4jMap, minute_id,candiList);

			requiredFlow = Math.max(bg.getIdleCarList().size(), bg.getRequestNodeList().size());
			StaticMatch2.maxFlow(requiredFlow, bg, candiList);
			
			WorkManager2.assignWork(bg, n4jMap,minute_id, recorder);
			
			WorkManager2.checkCarList(bg);

			WorkManager2.timeOutCheck(bg);
			
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println(sdf.format(new Date(Long.parseLong(String.valueOf(clock)))));
			
			System.out.println("idlecar size = "+bg.getIdleCarList().size() +",on work car size = "+bg.getOnWorkCarList().size()+",node list size = "+bg.getNodeList().size()+",request list size = "+bg.getRequestNodeList().size());
			if(bg.getIdleCarList().size()==1){
				GbCar car = bg.getIdleCarList().get(0);
				System.out.println("car id=" + car.getNodeId());
			}

		}
		recorder.writeOn();
	}
	
	public static void printBG(BipartiteGraph bg){
		System.out.println("hhhhh");
		GbNode sourceNode = bg.getGbSource();
		List<GbEdge> sourceedgelist = sourceNode.getEdgeList();
		//System.out.println("sourceedgelistsize:"+sourceedgelist.size());
		Iterator itedge = sourceedgelist.iterator();
		while(itedge.hasNext()){
			GbEdge sourceedge = (GbEdge) itedge.next();
			if(sourceedge.getIsForward()){
			  System.out.println("111");
			  System.out.println("fromnode_id:"+sourceedge.getFromNode().getNodeId()+",residualflow:"+sourceedge.getResidualFlow()+",cw:"+sourceedge.getCw()+",tonode_id:"+sourceedge.getToNode().getNodeId());
			  //System.out.println("fromnode_id:"+sourceedge.getReverseEdge().getFromNode().getNodeId()+",residualflow:"+sourceedge.getReverseEdge().getResidualFlow()+",tonode_id:"+sourceedge.getReverseEdge().getToNode().getNodeId());
			  GbNode tempNode2 = sourceedge.getToNode();
			  List<GbEdge> edgelist2 = tempNode2.getEdgeList();
			  Iterator it2 = edgelist2.iterator();
			  while(it2.hasNext()){
				  GbEdge edge2 = (GbEdge) it2.next();
				  if(edge2.isFromNode(tempNode2) && edge2.getIsForward()==true){
					  System.out.println("222");
					  System.out.println("fromnode_id:"+edge2.getFromNode().getNodeId()+",residualflow:"+edge2.getResidualFlow()+",cw:"+edge2.getCw()+",tonode_id:"+edge2.getToNode().getNodeId());
					  //System.out.println("fromnode_id:"+edge2.getReverseEdge().getFromNode().getNodeId()+",residualflow:"+edge2.getReverseEdge().getResidualFlow()+",tonode_id:"+edge2.getReverseEdge().getToNode().getNodeId());
					  GbNode tempNode3 = edge2.getToNode();
				      List<GbEdge> edgelist3 = tempNode3.getEdgeList();
				      Iterator it3 = edgelist3.iterator();
				      while(it3.hasNext()){
				    	  GbEdge edge3 = (GbEdge) it3.next();
				    	  if(edge3.isFromNode(tempNode3) && edge3.getIsForward()==true){
				    		System.out.println("333");
				    	    System.out.println("fromnode_id:"+edge3.getFromNode().getNodeId()+",residualflow:"+edge3.getResidualFlow()+",cw:"+edge3.getCw()+",tonode_id:"+edge3.getToNode().getNodeId());
				    	    //System.out.println("fromnode_id:"+edge3.getReverseEdge().getFromNode().getNodeId()+",residualflow:"+edge3.getReverseEdge().getResidualFlow()+",tonode_id:"+edge3.getReverseEdge().getToNode().getNodeId());
				    	  }
				      }
				  }
			  }
			} 
		}
	}
	
	public static int calculateMinuteId(long clock){
		long time = clock - DATE_14_7_1;
		int minute_id = (int) (((time/1000)%86400)/600);
		return minute_id;
	}
	
}