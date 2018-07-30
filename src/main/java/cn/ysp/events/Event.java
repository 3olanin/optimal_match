package cn.ysp.events;

public abstract class Event {
	
	private long eventtime;
	protected String eventType;
	
	public Event(long eventtime){
		this.eventtime = eventtime;
	}
	
	public long getEventTime(){
		return eventtime;
	}
}
