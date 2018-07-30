package cn.ysp.object;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import cn.ysp.events.Event;
import cn.ysp.optimal_match.PathManager;
import cn.ysp.optimal_match.WorkManager;

public class GbCar extends GbNode{
	
	//private double speed;
	private Relationship roadSeg;
	private Node location;
	private Node nextLocation;
	private double residualDistance;
	private double totalDistance;
	private double coverDistance;
	private LinkedList<Relationship> path;
	private GbRequest request;
	private double w;
	private long awake_time;
	//carStatus 0:the car has no request match
	//			1:the car has a request match,but the match can be revoked
	//			2:the car hss a request match,but the match can not be revoked
	//			3:the car has picked up the request
	private int carStatus;
	//changeListType 0:ToIdleList
	//				 1:ToWorkList
	private int changeListType;
	
	public GbCar(){

	}
	
	public GbCar(Node location){
		this.location = location;
		this.nextLocation = location;
		this.residualDistance= 0;
		this.totalDistance = 0;
		this.coverDistance = 0;
		this.node_type = "car_node";
		this.request = null;
		this.carStatus = 0;
		this.changeListType = 0;
		this.roadSeg = null;
		this.awake_time = 0;
	}
	

	public Node getLocation(){
		return location;
	}
	
	public void setLocation(Node location){
		this.location = location;
	}
	
	public Node getNextLocation(){
		return nextLocation;
	}
	
	public void setNextLocation(Node location){
		this.nextLocation = location;
	}
	
	public double getResidualDistance(){
		return residualDistance;
	}
	
	public void setResidualDistance(double distance){
		this.residualDistance = distance;
	}
	
	public LinkedList<Relationship> getPath(){
		return path;
	}
	
	public void setPath(LinkedList<Relationship> path){
		this.path = path;
	}
	
	public int getCarStatus(){
		return carStatus;
	}
	
	public void setCarStatus(int carStatus){
		this.carStatus = carStatus;
	}
	
	public double getTotalDistance(){
		return totalDistance;
	}
	
	public void setTotalDistacne(double distance){
		this.totalDistance = distance;
	}
	
	public double getCoverDistance(){
		return coverDistance;
	}
	
	public void setCoverDistance(double distance){
		this.coverDistance = distance;
	}
	
	public GbRequest getRequest(){
		return request;
	}
	
	public void setRequest(GbRequest request){
		this.request = request;
	}
	
	public int getChangeListType(){
		return changeListType;
	}
	
	public void setChangeListType(int type){
		this.changeListType = type;
	}
	
	public Relationship getRoadSeg(){
		return roadSeg;
	}
	
	public void setRoadSeg(Relationship r){
		this.roadSeg = r;
	}
	
	public void setW(double w){
		if(w<0)
			w = 0;
		this.w = w;
	}
	
	public double getW(){
		return w;
	}
	
	public void setAwakeTime(long time){
		this.awake_time = time;
	}
	
	public long getAwakeTime(){
		return this.awake_time;
	}
	
	//init the path
	public void initPath(WeightedPath wpath){
		if(wpath != null){
			LinkedList<Relationship> path = PathManager.iterable2LinkedListPath(wpath.relationships());
			if(!path.isEmpty()){
				Relationship r = path.poll();
				this.setResidualDistance((double)r.getProperty("length"));
				this.setCoverDistance(0);
				this.setTotalDistacne(PathManager.getWeightedPathDistance(wpath));
				this.setRoadSeg(r);
				this.setLocation(r.getStartNode());
				this.setNextLocation(r.getEndNode());
				this.setPath(path);
			}
			else{
				this.setPath(path);
				this.setRoadSeg(null);
				this.setResidualDistance(0);
				this.setCoverDistance(0);
				this.setTotalDistacne(0);
			}
		}
		else{
			this.setPath(null);
			this.setRoadSeg(null);
			this.setResidualDistance(0);
			this.setCoverDistance(0);
			this.setTotalDistacne(0);
		}
	}
	
	//init the path and update the car's request
	public void initPathWithRequest(WeightedPath wpath, GbRequest request){
		this.request = request;
		LinkedList<Relationship> path = PathManager.iterable2LinkedListPath(wpath.relationships());
		if(!path.isEmpty()){
			Relationship r = path.poll();
			this.setResidualDistance((double)r.getProperty("length"));
			this.setCoverDistance(0);
			this.setTotalDistacne(PathManager.getWeightedPathDistance(wpath));
			this.setRoadSeg(r);
			this.setLocation(r.getStartNode());
			this.setNextLocation(r.getEndNode());
			this.setPath(path);
		}
		else{
			this.setPath(path);
			this.setRoadSeg(null);
			this.setCoverDistance(0);
			this.setTotalDistacne(0);
		}
	}
}
