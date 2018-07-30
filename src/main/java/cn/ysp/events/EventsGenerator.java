package cn.ysp.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cn.ysp.object.BipartiteGraph;
import cn.ysp.object.GbRequest;
import cn.ysp.object.Recorder;
import cn.ysp.optimal_match.Simulator;
/*
 * create event
 */
public class EventsGenerator {
	
	private LinkedList<Event> eventQueue = new LinkedList<Event>();
	
	//load request file and create RequestCreateEvent
	public void loadRequestFile(String fileName) throws NumberFormatException, IOException, ParseException{
		File requestFile=new File(fileName);
		InputStreamReader read = new InputStreamReader(new FileInputStream(requestFile));
		BufferedReader bufferedReader = new BufferedReader(read);
		Queue<Event> eventQueue = new LinkedList<Event>();

		String lineTxt = "";
		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		while((lineTxt = bufferedReader.readLine()) != null){
			String s[]=lineTxt.split("#");
			double olon=Float.valueOf(s[0]);
			double olat=Float.valueOf(s[1]);
			double dlon=Float.valueOf(s[2]);
			double dlat=Float.valueOf(s[3]);
			String timeString = s[4];
		    Date date = format.parse(timeString);
		    long timeStamp = (long) (date.getTime());
		    String timeString2 = s[5];
		    Date date2 = format.parse(timeString2);
		    long t1 = (long) (date2.getTime());
		    String timeString3 = s[6];
		    Date date3 = format.parse(timeString3);
		    long t2 = (long) (date3.getTime());

		    Event event = new RequestCreatedEvent(timeStamp,t1,t2,olon,olat,dlon,dlat);
		    addEvent(event);
		    
		    //RequestCreatedEvent revent = (RequestCreatedEvent) event;
		    //System.out.println(revent.getEventTime()+","+revent.getEventType()+","+revent.getT1()+","+revent.getT2());
		}
	}
	
	public LinkedList<Event> getEventQueue(){
		return eventQueue;
	}
	
	public void addEvent(Event event){
		List<Event> eq = getEventQueue();
		if(!eq.isEmpty()){
			for(int i = 0; i < eq.size(); i++){
				Event e = eq.get(i);
				if(event.getEventTime() < e.getEventTime()){
					eq.add(i,event);
					break;
				}
				else{
					if(i == eq.size()-1){
						eq.add(event);
						break;
					}
				}
			}
        }
		else{
			eq.add(event);
		}
	}
	/*
	 * check if the event time is up
	 */
	public void eventCheck(long clock, BipartiteGraph bg, Recorder reco){
		long time;
		if(!eventQueue.isEmpty())
			time = eventQueue.getFirst().getEventTime();
		else
			return;
		
		while(time <= clock){
			RequestCreatedEvent e = (RequestCreatedEvent) eventQueue.pollFirst();
			GbRequest r = bg.generateRequestNodeByEvent(e);
			//appointmentRequest
			if(r.getT1()-r.getT0() > Simulator.T1_inter){
				reco.addAppointmentRNum(1);
			}
			//realTimeRequest
			else{
				reco.addRealTimeRNum(1);
			}
			bg.addNode(r);
			bg.addEdgeBetweenNode(r, bg.getGbSink(), 1, 1, 0);
			if(!eventQueue.isEmpty())
				time = eventQueue.getFirst().getEventTime();
			else
				break;
		}
	}
}
