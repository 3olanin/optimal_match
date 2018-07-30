package cn.ysp.object;

import java.util.ArrayList;
import java.util.List;

public class GbRequest extends GbNode{
	
	private double olon;
	private double olat;
	private double dlon;
	private double dlat;
	private long t1;
	private long t2;
	private long t0;
	private boolean isMatched;
	
	public GbRequest(){
		
	}
	
	public GbRequest(double olon, double olat, double dlon, double dlat, long t1, long t2,long t0){
		this.olon = olon;
		this.olat = olat;
		this.dlon = dlon;
		this.dlat = dlat;
		this.t1 = t1;
		this.t2 = t2;
		this.t0 = t0;
		this.node_type = "request_node";
		this.isMatched = false;
	}
	
	public double getOlon(){
		return olon;
	}
	
	public double getOlat(){
		return olat;
	}
	
	public void setOlon(double lon){
		this.olon = lon;
	}
	
	public void setOlat(double lat){
		this.olat = lat;
	}
	
	public double getDlon(){
		return dlon;
	}
	
	public double getDlat(){
		return dlat;
	}
	
	public void setDlon(double lon){
		this.dlon = lon;
	}
	
	public void setDlat(double lat){
		this.dlat = lat;
	}
	
	public long getT1(){
		return t1;
	}
	
	public void setT1(long t){
		this.t1 = t;
	}
	
	public long getT2(){
		return t2;
	}
	
	public void setT2(long t){
		this.t2 = t;
	}
	
	public long getT0(){
		return t0;
	}
	
	public void setT0(long t){
		this.t0 = t;
	}
	
	public void setIsMatched(boolean j){
		this.isMatched = j;
	}
	
	public boolean getIsMatched(){
		return isMatched;
	}
}
