package cn.ysp.events;


public class RequestCreatedEvent extends Event{
	
	private long t1;
	private long t2;
	private double olon;
	private double olat;
	private double dlon;
	private double dlat;
	
	public RequestCreatedEvent(long eventtime, long t1, long t2, double olon, double olat, double dlon, double dlat){
		super(eventtime);
		this.t1 = t1;
		this.t2 = t2;
		this.olon = olon;
		this.olat = olat;
		this.dlon = dlon;
		this.dlat = dlat;
		eventType = "REQUEST_CREATED";
	}
	
	public String getEventType(){
		return eventType;
	}
	
	public long getT1(){
		return t1;
	}
	
	public long getT2(){
		return t2;
	}
	
	public double getOlon(){
		return olon;
	}
	
	public void setOlon(double olon){
		this.olon = olon;
	}
	
	public double getOlat(){
		return olat;
	}
	
	public void setOlat(double olat){
		this.olat = olat;
	}
	
	public double getDlon(){
		return dlon;
	}
	
	public void setDlon(double dlon){
		this.dlon = dlon;
	}
	
	public double getDlat(){
		return dlat;
	}
	
	public void setDlat(double dlat){
		this.dlat = dlat;
	}
	
	
	
	
	
	
	
	
	
	
	
}
