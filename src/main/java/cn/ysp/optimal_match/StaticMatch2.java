package cn.ysp.optimal_match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.osm.OSMDataset;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;

import cn.ysp.map.Neo4jMap;
import cn.ysp.object.BipartiteGraph;
import cn.ysp.object.GbCar;
import cn.ysp.object.GbEdge;
import cn.ysp.object.GbNode;
import cn.ysp.object.GbRequest;

public class StaticMatch2 {
	static private int MAX_DISTANCE = 100000;
	static private int NO_PARENT = -1;
	
	//calculate a BipartiteGraph's min cost flow
	//requireFlow:the flow that required in the bg
	static public boolean maxFlow(int requireFlow,BipartiteGraph bg, List<GbEdge> candiList){
	
		if(bg.getIdleCarList().isEmpty() || bg.getRequestNodeList().isEmpty()){
			return true;
		}
		
		
		
		
		while(!candiList.isEmpty()){
			GbEdge minEdge = getMinCWEdge(candiList);
			if(minEdge!=null ){
				GbCar  c = (GbCar) minEdge.getFromNode();
				GbRequest r = (GbRequest) minEdge.getToNode();
				if(c.ifAvailable()&& r.ifAvailable()){
					minEdge.setResidualFlow(0);
					minEdge.getReverseEdge().setResidualFlow(1);
					c.setUnavailable();
					r.setUnavailable();
				}
				candiList.remove(minEdge);
			}
			else{
				break;
			}
		}
		
		
		
		
		
//		Iterator rit = bg.getRequestNodeList().iterator();
//		while(rit.hasNext()){
//			GbRequest r = (GbRequest) rit.next();
//			Iterator eit = r.getEdgeList().iterator();
//			GbEdge minEdge = new GbEdge(new GbNode(),new GbNode());
//			double min_u = 1000000;
//			while(eit.hasNext()){
//				GbEdge e = (GbEdge) eit.next();
//				if(e.isToNode(r)){
//					if(e.getAnotherNode(r).ifAvailable() == true){
//						if(min_u > e.getCw()){
//							min_u = e.getCw();
//							minEdge = e;
//						}
//					}
//				}
//			}
//			if(min_u<1000000){
//				minEdge.setResidualFlow(0);
//				minEdge.getReverseEdge().setResidualFlow(1);
//				minEdge.getAnotherNode(r).setUnavailable();
//			}
//		}
		
		
		return true;
	}
	 
	//calculate request-car pair's cw and add edge
	static public void calculateAllCW(BipartiteGraph bg, Neo4jMap n4jMap, int minute_id, List<GbEdge> candiList){
		List<GbCar> carNodeList = bg.getIdleCarList();
		List<GbRequest> requestList = bg.getRequestNodeList();
		Iterator carIt = carNodeList.iterator();
		while(carIt.hasNext()){
			GbCar carNode = (GbCar) carIt.next();
			Iterator requestIt = requestList.iterator();
			while(requestIt.hasNext()){
				GbRequest requestNode = (GbRequest) requestIt.next();
				double serv = 0;
				//calculate the cw between car and request
				//1.calculate pt
				int w = calculateW(carNode, requestNode, n4jMap, minute_id);
				long pt = calculatePt(carNode, requestNode, n4jMap, w);
				//2.calculate serv , trac and u
				//(1)realtime request
				if(requestNode.getT0() <= requestNode.getT1() && requestNode.getT1() < requestNode.getT0()+Simulator.T1_inter){
					if(requestNode.getT1()<=pt && pt<=requestNode.getT2()){
						serv = 1;
					}
					else{
						serv = 0;
					}
					double trac = 1;
					if(w == 0){
						//100000000 is a very big number
						trac = 10;
					}
					else{
						trac = Simulator.U/w;
					}
					
					double u = Simulator.x_factor*serv + (1-Simulator.x_factor)*trac;
					//System.out.println("serv= "+serv +",trac = "+trac +",u=" +u+",w="+w);
					//add edge
					GbEdge e = bg.getEdgeFromCtoQ(carNode, requestNode);
					if(Simulator.clock + w < requestNode.getT2()){
						if(e == null){
							GbEdge ee = bg.addEdgeBetweenNode(carNode,requestNode,1,1, -u);
							candiList.add(ee);
						}
						else{
							e.setCw(-u);
							e.getReverseEdge().setCw(u);
							candiList.add(e);
						}
					}
				}
				//(2)appointment request
				else if(requestNode.getT1() >= requestNode.getT0()+Simulator.T1_inter){
					if(requestNode.getT1()<=pt && pt<=requestNode.getT2()){
						serv = Simulator.K;
					}
					else{
						serv=0;
					}
					double trac = 1;
					if(w == 0){
						//100000000 is a very big number
						trac = 100000000;
					}else{
						trac = Simulator.U/w;
					}
					double u = Simulator.x_factor*serv + (1-Simulator.x_factor)*trac;
					//add edge
					GbEdge e = bg.getEdgeFromCtoQ(carNode, requestNode);
					if(Simulator.clock + w < requestNode.getT2()){
						if(e == null){
							GbEdge ee = bg.addEdgeBetweenNode(carNode,requestNode,1,1, -u);
							candiList.add(ee);
						}
						else{
							e.setCw(-u);
							e.getReverseEdge().setCw(u);
							candiList.add(e);
						}
					}
				}
			}
		}
	}
	
	
	//calculate request-car pair's cw and add edge using Matrix
	public static void calculateAllCW(BipartiteGraph bg, Neo4jMap n4jMap, int minute_id, CostMatrixManager cmm){
		List<GbCar> newCarList = bg.getNewCarList();
		List<GbCar> carNodeList = bg.getIdleCarList();
		List<GbRequest> requestList = bg.getRequestNodeList();
		List<GbRequest> newRequestList = bg.getNewRequestNodeList();
		List<GbRequest> deleteRequestList = new ArrayList<GbRequest>();
		//delete the out of time request
		Iterator rit = requestList.iterator();
		while(rit.hasNext()){
			GbRequest r = (GbRequest) rit.next();
			if(r.getT2() < Simulator.clock && r.getIsMatched() == false){
				deleteRequestList.add(r);
			}
		}
		rit = deleteRequestList.iterator();
		while(rit.hasNext()){
			GbRequest r = (GbRequest) rit.next();
			bg.deleteRequestNode(r);
		}
		
		Iterator oldCarIt = carNodeList.iterator();
		//the old car and the old request
		while(oldCarIt.hasNext()){
			GbCar carNode = (GbCar) oldCarIt.next();
			Iterator requestIt = requestList.iterator();
			while(requestIt.hasNext()){
				GbRequest requestNode = (GbRequest) requestIt.next();
				//add edge
				GbEdge e = bg.getEdgeFromCtoQ(carNode, requestNode);
				if(e == null){
					long approximateW = cmm.getValue((long)carNode.getLocation().getProperty("box_num"), (long)locateOsmNode(requestNode.getOlon(), requestNode.getOlat(), n4jMap).getProperty("box_num"));
					if(requestNode.getT2()-Simulator.clock-approximateW > -600000){
						double u = calculateU(carNode, requestNode, bg, n4jMap, minute_id);
						if(Math.abs(u) > 0.000000001){
							bg.addEdgeBetweenNode(carNode,requestNode,1,1, -u);
						}
					}
				}
				else{
					double u = calculateU(carNode, requestNode, bg, n4jMap, minute_id);
					if(Math.abs(u) > 0.000000001){
						e.setCw(-u);
						e.getReverseEdge().setCw(u);
					}
				}
			}	
		}
		
		//the old car with new request
		oldCarIt = carNodeList.iterator();
		while(oldCarIt.hasNext()){
			GbCar carNode = (GbCar) oldCarIt.next();
			Iterator requestIt = newRequestList.iterator();
			while(requestIt.hasNext()){
				GbRequest requestNode = (GbRequest) requestIt.next();
				long  approximateW = (long)cmm.getValue((long)carNode.getLocation().getProperty("box_num"), (long)locateOsmNode(requestNode.getOlon(), requestNode.getOlat(), n4jMap).getProperty("box_num"));
				//System.out.println("car_id = "+ carNode.getNodeId() +"request_id = "+ requestNode.getNodeId()+"approW = " +approximateW+" t2- clock = " + (requestNode.getT2()-Simulator.clock ) );
				if(requestNode.getT2()-Simulator.clock-approximateW > -600000)
				{
					double u = calculateU(carNode, requestNode, bg, n4jMap, minute_id);
					if(u<0){
						u=0;
					}
					if(Math.abs(u) > 0.000000001){
						//add edge
						GbEdge e = bg.getEdgeFromCtoQ(carNode, requestNode);
						if(e == null){
							bg.addEdgeBetweenNode(carNode,requestNode,1,1, -u);
						}
						else{
							e.setCw(-u);
							e.getReverseEdge().setCw(u);
						}	
					}
				}
			}
		}
		
		//put the new Request into RequestNodeList
		bg.getRequestNodeList().addAll(bg.getNewRequestNodeList());
		bg.getNewRequestNodeList().clear();
		
		
		//the new car with all request
		Iterator newCarIt = newCarList.iterator();
		while(newCarIt.hasNext()){
			GbCar carNode = (GbCar) newCarIt.next();
			Iterator requestIt = requestList.iterator();
			while(requestIt.hasNext()){
				GbRequest requestNode = (GbRequest) requestIt.next();
				long approximateW = cmm.getValue((long)carNode.getLocation().getProperty("box_num"), (long)locateOsmNode(requestNode.getOlon(), requestNode.getOlat(), n4jMap).getProperty("box_num"));
				if(requestNode.getT2()-Simulator.clock-approximateW > -600000)
				{
					double u = calculateU(carNode, requestNode, bg, n4jMap, minute_id);
					if(Math.abs(u) > 0.000000001){
						//add edge
						GbEdge e = bg.getEdgeFromCtoQ(carNode, requestNode);
						if(e == null){
							bg.addEdgeBetweenNode(carNode,requestNode,1,1, -u);
						}
						else{
							e.setCw(-u);
							e.getReverseEdge().setCw(u);
						}	
					}
				}
			}	
		}
		//put the new Car into RequestNodeList
		bg.getIdleCarList().addAll(bg.getNewCarList());
		bg.getNewCarList().clear();
		
		
	}
	
	public static double  calculateU(GbCar carNode, GbRequest requestNode, BipartiteGraph bg, Neo4jMap n4jMap, int minute_id){
		double serv = 0;
		double u = 0;
		//calculate the cw between car and request
		//1.calculate pt
		int w = calculateW(carNode, requestNode, n4jMap, minute_id);
		if(w <0){
			return -1;
		}
		long pt = calculatePt(carNode, requestNode, n4jMap, w);
		//2.calculate serv , trac and u
		//(1)realtime request
		if(requestNode.getT0() <= requestNode.getT1() && requestNode.getT1() < requestNode.getT0()+Simulator.T1_inter){
			if(requestNode.getT1()<=pt && pt<=requestNode.getT2()){
				serv = 1;
			}
			double trac = 1;
			if(w == 0){
				//100000000 is a very big number
				trac = 100000000;
			}
			else{
				trac = Simulator.U/w;
			}
			
			u = Simulator.x_factor*serv + (1-Simulator.x_factor)*trac;
			//add edge
			return u;
			
		}
		//(2)appointment request
		else if(requestNode.getT1() >= requestNode.getT0()+Simulator.T1_inter){
			if(requestNode.getT1()<=pt && pt<=requestNode.getT2()){
				serv = Simulator.K;
			}
			double trac = 1;
			if(w == 0){
				//100000000 is a very big number
				trac = 100000000;
			}else{
				trac = Simulator.U/w;
			}
			u = Simulator.x_factor*serv + (1-Simulator.x_factor)*trac;
			//add edge
			return u;
		}
		return u;
	}
	
	
	//w(c; q; t) is the cost of time for c travelling to pickup q
	//return w(ms)
	static public int calculateW(GbCar carNode, GbRequest requestNode, Neo4jMap n4jMap, int minute_id){
		double rlon = requestNode.getOlon();
		double rlat = requestNode.getOlat();
		//locate to osm node
		//Node cNode = locateOsmNode(clon, clat, n4jMap);
		Node cNode = carNode.getLocation();
		Node rNode = locateOsmNode(rlon, rlat, n4jMap);
		
		
		
		SpatialDatabaseService spatial = new SpatialDatabaseService(n4jMap.getDB());
		//此处并非读取文件，"D:\\毕设\\lz路网资料\\地图处理全步骤\\map_highway.osm"不是一个地址，而是neo4j数据库中spatial_root节点的LAYER关系的下一个节点的layer属性值
    	OSMLayer layer = (OSMLayer) spatial.getLayer("D:\\毕设\\lz路网资料\\地图处理全步骤\\map_highway.osm");
    	Node layerNode=layer.getLayerNode();
    	OSMDataset osmDs=new OSMDataset(spatial,layer,layerNode);
		
    	
		WeightedPath p1=osmDs.getdijkstraLengthShortestPath(cNode, rNode, minute_id);
		//w is the cost time from cNode to rNode 
		if(p1 == null){
			return -1;
		}
		double w = p1.weight();
		//speed:50km/h = 0.01389m/ms
		int cost_time = (int) w;
		return cost_time;
	}
	
	//pt(c; q; t) is the time spent for picking up q by vehicle c
	//return pt(ms)
	public static long calculatePt(GbCar carNode, GbRequest requestNode, Neo4jMap n4jMap, int w){
		long z = requestNode.getT1()-requestNode.getT0()-w;
		long p = 0;
		if(z>=0){
			p = z;
		}
		return (Simulator.clock + w + p);
	}
	
	//return a NEO4J Node
	public static  Node locateOsmNode(double lon, double lat, Neo4jMap n4jMap){
		String query="MATCH (n:ROADNODE)-[r2:NEXT]-() where n.lat>{lat1} and n.lat<{lat2} and n.lon>{lon1} and n.lon<{lon2} RETURN distinct r2";
		Map<String, Object> parameters=new HashMap<String, Object>();
		parameters.put("lat", lat);
		parameters.put("lon", lon);
		parameters.put("lat1", lat-0.0027f);
		parameters.put("lon1", lon-0.0027f);
		parameters.put("lat2", lat+0.0027f);
		parameters.put("lon2", lon+0.0027f);
		Result result1 = n4jMap.execute(query,parameters);

		//just pick one node randomly
		if (result1.hasNext()) {
        	Map<String,Object> m1=result1.next();
            Relationship m=(Relationship) m1.get("r2");
            return m.getStartNode();
		}
		else{
			return null;
		}
	}
	
	public static GbEdge getMinCWEdge(List<GbEdge> candiList){
		GbEdge minEdge = null;
		double min_cw = 10000;
		Iterator it = candiList.iterator();
		while(it.hasNext()){
			GbEdge edge = (GbEdge) it.next();
			if(edge.getCw()<min_cw){
				minEdge = edge;
				min_cw = minEdge.getCw();
			}
		}
		return minEdge;
	}
	
}
