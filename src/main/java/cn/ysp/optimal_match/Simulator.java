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

public class Simulator {

	public static String NEO4JPATH = "D:/Neo4jDB_2_2";	
	//public static String CARNODE_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\carNodeFile_test.txt";
	//public static String CARNODE_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\carNodeFile.txt";
	public static String CARNODE_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\carFile.txt";
	//public static String REQUEST_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\request.txt";
	public static String REQUEST_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\requestFile1.txt";
	//public static String REQUEST_FILE = "C:\\Users\\wydn1\\Desktop\\xiamenosm\\request_test.txt";
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
		//record the results
		Recorder recorder = new Recorder();
		int minute_id = 0;
		int last_minute = -1;
		//cost matrix
		CostMatrixManager cmm = new CostMatrixManager();
		int hour_id = minute_id/6;
		cmm.loadMatrix(hour_id);
		
		
		minute_id = calculateMinuteId(clock);
		hour_id = minute_id/6;
		//cmm.loadMatrix(hour_id);
		//calculateCW and add edge
		StaticMatch.calculateAllCW(bg, n4jMap, minute_id, cmm);
		//printBG(bg);
		//calculate the min-cost-flow
		int requiredFlow = Math.max(bg.getIdleCarList().size(), bg.getRequestNodeList().size());
		StaticMatch.maxFlow(requiredFlow, bg);
		//assign work according to Bipartite Graph
		WorkManager.assignWork(bg, n4jMap, minute_id);
		//printBG(bg);
		//loop 24h
//		while(clock < DATE_14_7_1 + 86400000){
		while(clock < DATE_14_7_1 + 3600000){
			Instant inst1 = Instant.now();
			//update interval:1000ms
			clock+=ut;
			minute_id = calculateMinuteId(clock);
			if(minute_id%6 == 0 && last_minute != minute_id){
				hour_id = minute_id/6;
				cmm.loadMatrix(hour_id);
				last_minute = minute_id;
			}
			//check new request
			//System.out.println("eventCheck");
			eventsGenerator.eventCheck(clock, bg, recorder);
			//update the world
			Instant inst2 = Instant.now();
			System.out.println("moveupdate IdleCar");
			WorkManager.move_update(bg,n4jMap,ut,minute_id,bg.getIdleCarList(),recorder);
			System.out.println("moveupdate OnWorkCar");
			WorkManager.move_update(bg,n4jMap,ut,minute_id,bg.getOnWorkCarList(),recorder);
			System.out.println("checkCarList");
			WorkManager.checkCarList(bg);
			Instant inst3 = Instant.now();

			//calculateCW and add edge
			System.out.println("calculateAllCW");
			StaticMatch.calculateAllCW(bg, n4jMap, minute_id, cmm);
			printBG(bg);
			Instant inst4 = Instant.now();
			System.out.println("idlecar size = "+bg.getIdleCarList().size() +",on work car size = "+bg.getOnWorkCarList().size()+",node list size = "+bg.getNodeList().size()+",request list size = "+bg.getRequestNodeList().size());
			System.out.println("Sink edge count = " + bg.getGbSink().getEdgeList().size());
			//calculate the min-cost-flow
			requiredFlow = Math.max(bg.getIdleCarList().size(), bg.getRequestNodeList().size());
			//printBG(bg);
			System.out.println("maxFlow");
			StaticMatch.maxFlow(requiredFlow, bg);
			//printBG(bg);
			Instant inst5 = Instant.now();
			System.out.println("assignWork");
			WorkManager.assignWork(bg, n4jMap,minute_id);
			Instant inst6 = Instant.now();
			
			//***
			Iterator itdelete = bg.getRequestNodeList().iterator();
			List<GbRequest> deleteRequestList = new ArrayList<GbRequest>();
			while(itdelete.hasNext()){
				GbRequest r = (GbRequest) itdelete.next();
			    deleteRequestList.add(r);
			}
			itdelete = deleteRequestList.iterator();
			while(itdelete.hasNext()){
				GbRequest r = (GbRequest) itdelete.next();
				bg.deleteRequestNode(r);
			}
			//***
			

			
			
			System.out.println("move car =" + (Duration.between(inst2, inst3).toMillis()) );
			System.out.println("calculateAllCW =" + (Duration.between(inst3, inst4).toMillis()) );
			System.out.println("maxFlow =" + (Duration.between(inst4, inst5).toMillis()) );
			System.out.println("assignwork =" + (Duration.between(inst5, inst6).toMillis()) );
			System.out.println("total =" + (Duration.between(inst1, inst6).toMillis()) );
			
			
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println(sdf.format(new Date(Long.parseLong(String.valueOf(clock)))));
//			if(clock > 1404144900000L){
//			Iterator it = bg.getRequestNodeList().iterator();
//			while(it.hasNext()){
//				GbRequest r = (GbRequest) it.next();
//				System.out.println("start lon = "+ r.getOlon() + " start lat = " + r.getOlat());
//				System.out.println("end lon = "+ r.getDlon() + " end lat = " + r.getDlat());
//			}
//			System.exit(0);
//			}
//			if(clock > 1404144902000L){
//				printBG(bg);
//				System.exit(0);
//			}
		}
		recorder.writeOn();
	}
	
	public static void printBG(BipartiteGraph bg){
		System.out.println("print BipartiteGraph");
		GbNode sourceNode = bg.getGbSource();
		List<GbEdge> sourceedgelist = sourceNode.getEdgeList();
		//System.out.println("sourceedgelistsize:"+sourceedgelist.size());
		Iterator itedge = sourceedgelist.iterator();
		while(itedge.hasNext()){
			GbEdge sourceedge = (GbEdge) itedge.next();
			if(sourceedge.getIsForward() && sourceedge.isFromNode(sourceNode)){
			  System.out.println("source to car");
			  System.out.println("fromnode_id:"+sourceedge.getFromNode().getNodeId()+",residualflow:"+sourceedge.getResidualFlow()+",cw:"+sourceedge.getCw()+",tonode_id:"+sourceedge.getToNode().getNodeId());
			  //System.out.println("fromnode_id:"+sourceedge.getReverseEdge().getFromNode().getNodeId()+",residualflow:"+sourceedge.getReverseEdge().getResidualFlow()+",tonode_id:"+sourceedge.getReverseEdge().getToNode().getNodeId());
			  GbNode tempNode2 = sourceedge.getToNode();
			  List<GbEdge> edgelist2 = tempNode2.getEdgeList();
			  Iterator it2 = edgelist2.iterator();
			  while(it2.hasNext()){
				  GbEdge edge2 = (GbEdge) it2.next();
				  if(edge2.isFromNode(tempNode2) && edge2.getIsForward()==true){
					  System.out.println("car to request");
					  System.out.println("fromnode_id:"+edge2.getFromNode().getNodeId()+",residualflow:"+edge2.getResidualFlow()+",cw:"+edge2.getCw()+",tonode_id:"+edge2.getToNode().getNodeId());
					  //System.out.println("fromnode_id:"+edge2.getReverseEdge().getFromNode().getNodeId()+",residualflow:"+edge2.getReverseEdge().getResidualFlow()+",tonode_id:"+edge2.getReverseEdge().getToNode().getNodeId());
					  GbNode tempNode3 = edge2.getToNode();
				      List<GbEdge> edgelist3 = tempNode3.getEdgeList();
				      Iterator it3 = edgelist3.iterator();
				      while(it3.hasNext()){
				    	  GbEdge edge3 = (GbEdge) it3.next();
				    	  if(edge3.isFromNode(tempNode3) && edge3.getIsForward()==true){
				    		System.out.println("request to sink");
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