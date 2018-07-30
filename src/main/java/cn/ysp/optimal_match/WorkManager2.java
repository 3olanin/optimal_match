package cn.ysp.optimal_match;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.osm.OSMDataset;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.osm.OSMRelation;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import cn.ysp.map.Neo4jMap;
import cn.ysp.object.BipartiteGraph;
import cn.ysp.object.GbCar;
import cn.ysp.object.GbEdge;
import cn.ysp.object.GbRequest;
import cn.ysp.object.Recorder;

public class WorkManager2 {
	
	/*
	 * update all car's moving and status
	 */
	public static void move_update(BipartiteGraph bg, Neo4jMap n4jMap, int ut, int minute_id, List<GbCar> carList, Recorder reco){
		Iterator it = carList.iterator();
		while(it.hasNext()){
			GbCar car = (GbCar) it.next();
			move_car(bg, n4jMap, car, ut, minute_id, reco);
		}
	}
	
	/*
	 * update one car's moving and status
	 */
	public static void move_car(BipartiteGraph bg, Neo4jMap n4jMap, GbCar car, int ut, int minute_id, Recorder reco){
		int time = 0;
		//speed:50km/h = 0.01389m/ms
		double speed = 0.01389;
		while(time < ut){
			Relationship roadseg = car.getRoadSeg();
			if(roadseg != null){
				speed = ((double)roadseg.getProperty("length"))/((int)roadseg.getProperty("passtime_"+minute_id));
			}
			//System.out.println("speed = " + speed);
			if(car.getResidualDistance() > 0){
				int temptime = (int) (car.getResidualDistance()/speed);
				//can not finish the road segment in ut-time
				if(temptime > ut-time){
					car.setResidualDistance( car.getResidualDistance()-speed*(ut-time) );
					car.setCoverDistance( car.getCoverDistance() + speed*(ut-time));
					carStatusChangeCheck(bg, n4jMap, car, minute_id, reco);
					time = ut;
				}
				//can finish the road segment in ut-time
				else{
					//still have next road segment
					if(!car.getPath().isEmpty()){
						Relationship r = car.getPath().poll();
						car.setRoadSeg(r);
						car.setLocation(r.getStartNode());
						car.setNextLocation(r.getEndNode());
						car.setCoverDistance( car.getCoverDistance() + car.getResidualDistance());
						car.setResidualDistance((double)r.getProperty("length"));
						carStatusChangeCheck(bg, n4jMap, car, minute_id, reco);
						time += temptime;
					}
					//there is no anymore road segment,finish the path
					else{
						car.setRoadSeg(null);
						car.setLocation(car.getNextLocation());
						car.setResidualDistance(0);
						car.setTotalDistacne(0);
						car.setCoverDistance(0);
						carStatusChangeCheck(bg, n4jMap, car, minute_id, reco);
						time = ut;
					}
				}
			}
			else{
				carStatusChangeCheck(bg, n4jMap, car, minute_id, reco);
				time = ut;
			}
		}
	}
	
	public static void assignWork(BipartiteGraph bg, Neo4jMap n4jMap, int minute_id, Recorder reco){
		List<GbRequest> rList = bg.getRequestNodeList();
		Iterator rit = rList.iterator();
		while(rit.hasNext()){
			GbRequest r = (GbRequest) rit.next();
			r.setIsMatched(false);
		}

		List<GbCar> carList = bg.getIdleCarList();
		Iterator it = carList.iterator();
		while(it.hasNext()){
			GbCar carNode = (GbCar) it.next();
			Iterator edge_it = carNode.getEdgeList().iterator();
			while(edge_it.hasNext()){
				GbEdge edge = (GbEdge) edge_it.next();
				if(edge.getIsForward()==true && edge.isFromNode(carNode) && edge.getResidualFlow() == 0){
					GbRequest request = (GbRequest) edge.getAnotherNode(carNode);
					request.setIsMatched(true);
					Node cnode = carNode.getLocation();
					Node qnode = StaticMatch.locateOsmNode(request.getOlon(), request.getOlat(), n4jMap);
					Node dnode = StaticMatch.locateOsmNode(request.getDlon(), request.getDlat(), n4jMap);
					WeightedPath wpath1 = PathManager.findShortestPath(n4jMap.getDB(),cnode,qnode,minute_id);
					WeightedPath wpath2 = PathManager.findShortestPath(n4jMap.getDB(),qnode,dnode,minute_id);
					
					if(wpath1 != null && wpath2 != null){
						long spend_time1 = (long) wpath1.weight();
						long spend_time2 = (long) wpath2.weight();
						
						carNode.setLocation(dnode);
						carNode.setRequest(request);
						carNode.setAwakeTime(Simulator.clock+spend_time1+spend_time2);
						carNode.setCarStatus(3);
						
						//record
						//appointment request
						if(request.getT1() - request.getT0() > Simulator.T1_inter){
							long waittime = Simulator.clock + spend_time1 - request.getT1();
							if(waittime <0){
								waittime = 0;
							}
							reco.addUtility(-edge.getCw());
							reco.addWaitTime(waittime);
							reco.addAppointmentRSuccessNum(1);
							reco.addAppointmentIncome(PathManager.getWeightedPathDistance(wpath2));
						}
						//realtime request
						else{
							long waittime = Simulator.clock + spend_time1 - request.getT1();
							if(waittime <0){
								waittime = 0;
							}
							reco.addUtility(-edge.getCw());
							reco.addWaitTime(waittime);
							reco.addRealTimeWaitTime(waittime);
							reco.addRealTimeRSuccessNum(1);
							reco.addRealTimeIncome(PathManager.getWeightedPathDistance(wpath2));
						}
					}
					else{
						carNode.setRequest(request);
						carNode.setAwakeTime(Simulator.clock+1000);
						carNode.setCarStatus(3);
					}
					
					//LinkedList<Relationship> path = iterable2LinkedListPath(wpath.relationships());
					
					//carNode.initPathWithRequest(wpath,request);
				}
			}
		}
	}
	

	public static void carStatusChangeCheck(BipartiteGraph bg, Neo4jMap n4jMap, GbCar car, int minute_id, Recorder reco){
		double limitedDistance = 2000;
		//if car is idle and car is heading to a request
		if(car.getCarStatus() == 1){
			if(car.getTotalDistance() - car.getCoverDistance() <= limitedDistance){
				//carFromIdleToWork(car);
				car.setChangeListType(1);
				car.setCarStatus(2);
			}
		}
		//if car is on work
		else if(car.getCarStatus() == 2 || car.getCarStatus() == 3){
			if(car.getResidualDistance() == 0 && car.getCarStatus() == 2){
				//give the path from request start to request destination to car
				Node snode = car.getLocation();
				Node dnode = StaticMatch.locateOsmNode(car.getRequest().getDlon(), car.getRequest().getDlat(), n4jMap);

				WeightedPath wpath = PathManager.findShortestPath(n4jMap.getDB(),snode,dnode,minute_id);
				//LinkedList<Relationship> path = iterable2LinkedListPath(wpath.relationships());
				car.initPath(wpath);
				//appointmentRequest
				if(car.getRequest().getT1()-car.getRequest().getT0() > Simulator.T1_inter){
					reco.addAppointmentRSuccessNum(1);
					reco.addWaitTime(Simulator.clock - car.getRequest().getT0());
					reco.addAppointmentIncome(car.getTotalDistance());
				}
				//realtimeRequest
				else{
					reco.addRealTimeRSuccessNum(1);
					reco.addWaitTime(Simulator.clock - car.getRequest().getT0());
					reco.addRealTimeWaitTime(Simulator.clock - car.getRequest().getT0());
					reco.addRealTimeIncome(car.getTotalDistance());
				}
				car.setCarStatus(3);
			}
			else if(car.getResidualDistance() == 0 && car.getCarStatus() == 3){
				car.setCarStatus(0);
				//carFromWorkToIdle(car);
				car.setChangeListType(0);
				//bg.getRequestNodeList().remove(car.getRequest());
				car.setRequest(null);
				//give car a curise path
				Node snode = car.getLocation();
				Node dnode = n4jMap.getRandomNode();
				WeightedPath wpath = PathManager.findShortestPath(n4jMap.getDB(),snode,dnode,minute_id);
				car.initPath(wpath);
			}
		}
		//if car is cruising
		else if( car.getResidualDistance() == 0 && car.getCarStatus() == 0 ){
			Node snode = car.getLocation();
			Node dnode = n4jMap.getRandomNode();
			WeightedPath wpath = PathManager.findShortestPath(n4jMap.getDB(),snode,dnode,minute_id);
			car.initPath(wpath);
		}
	}
	
	
	
	public static void checkCarList(BipartiteGraph bg){
		Iterator it = bg.getIdleCarList().iterator();
		while(it.hasNext()){
			GbCar c = (GbCar) it.next();
			if(c.getCarStatus() == 3){
				bg.deleteCarNodeWithoutIdleList(c);
				c.getEdgeList().clear();
				it.remove();
				bg.getOnWorkCarList().add(c);
//				bg.getNodeList().remove(c);
				bg.deleteRequestNode(c.getRequest());
			}
		}
		it = bg.getOnWorkCarList().iterator();
		while(it.hasNext()){
			GbCar c = (GbCar) it.next();
			if(c.getAwakeTime() <= Simulator.clock){
				c.setAvailable();
				c.setCarStatus(0);
				it.remove();
				//bg.getNewCarList().add(c);
				//bg.getNodeList().add(c);
				bg.addNode(c);
				bg.addEdgeBetweenNode(bg.getGbSource(), c, 1, 1, 0);
			}
		}
	}
	
	public static void timeOutCheck(BipartiteGraph bg){
		List<GbRequest> requestList = bg.getRequestNodeList();
		List<GbRequest> deleteRequestList = new ArrayList<GbRequest>();
		Iterator rit = requestList.iterator();
		while(rit.hasNext()){
			GbRequest r = (GbRequest) rit.next();
			if(r.getT0()+15000 < Simulator.clock ){
			//if(r.getT2() < Simulator.clock){
				deleteRequestList.add(r);
			}
		}
		rit = deleteRequestList.iterator();
		while(rit.hasNext()){
			GbRequest r = (GbRequest) rit.next();
			bg.deleteRequestNode(r);
		}
	}
	

}
