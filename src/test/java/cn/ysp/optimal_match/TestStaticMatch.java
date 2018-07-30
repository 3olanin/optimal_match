package cn.ysp.optimal_match;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.neo4j.graphdb.Node;



import cn.ysp.object.BipartiteGraph;
import cn.ysp.object.GbCar;
import cn.ysp.object.GbEdge;
import cn.ysp.object.GbNode;
import cn.ysp.object.GbRequest;

public class TestStaticMatch {
	public static void main(String[] args) throws NumberFormatException, IOException{
		String fileName = "C:\\Users\\Lushaobin\\Desktop\\bg8.txt";
		BipartiteGraph bg = generateBG(fileName);
		StaticMatch.maxFlow(2, bg);
		Simulator.printBG(bg);
	}
	
	/*
	 * input file:
	 * car_num request_num
	 * node1_1 node2_1 cap_1 cost_1 flow_1
	 * node1_2 node2_2 cap_2 cost_2 flow_2
	 * ...
	 * node1_n node2_n cap_n cost_n flow_n
	 */
	public static BipartiteGraph generateBG(String fileName) throws NumberFormatException, IOException{
		
		BipartiteGraph bg = new BipartiteGraph();
		File file=new File(fileName);
		InputStreamReader read = new InputStreamReader(new FileInputStream(file));
		BufferedReader bufferedReader = new BufferedReader(read);
		String lineTxt = "";
		lineTxt = bufferedReader.readLine();
		String s0[]=lineTxt.split(" ");
		int carNum = Integer.valueOf(s0[0]);
		int requestNum = Integer.valueOf(s0[1]);
		for(int i=0; i<carNum; i++){
			GbCar carNode = new GbCar();
			bg.addNode(carNode);
			bg.addEdgeBetweenNode(bg.getGbSource(), carNode, 1, 1, 0);	
			bg.getIdleCarList().add(carNode);
		}
		for(int i=0; i<requestNum; i++){
			GbRequest requestNode = new GbRequest();
			bg.addNode(requestNode);
			bg.addEdgeBetweenNode(requestNode, bg.getGbSink(), 1, 1, 0);
			bg.getRequestNodeList().add(requestNode);
		}
		
		while((lineTxt = bufferedReader.readLine()) != null){
			String s[]=lineTxt.split(" ");
			int node1 = Integer.valueOf(s[0]);
			int node2 = Integer.valueOf(s[1]);
			int cap = Integer.valueOf(s[2]);
			double cost = Double.valueOf(s[3]);
			int flow = Integer.valueOf(s[4]);
			if(node1==0){
				if(flow!=0){
					GbNode n1 = bg.getGbSource();
					GbNode n2 = bg.getNodeById(node2);
					GbEdge e = bg.getEdgeFromNodetoNode(n1, n2);
					e.setResidualFlow(cap-flow);
					e.getReverseEdge().setResidualFlow(flow);
				}
			}
			else if(node2==1){
				if(flow!=0){
					GbNode n1 = bg.getNodeById(node1);
					GbNode n2 = bg.getGbSink();
					GbEdge e = bg.getEdgeFromNodetoNode(n1, n2);
					e.setResidualFlow(cap-flow);
					e.getReverseEdge().setResidualFlow(flow);
				}
			}
			else{
				bg.addEdgeBetweenNode(bg.getNodeById(node1), bg.getNodeById(node2), cap, cap-flow, cost);
			}
		}
		return bg;
	}
}
