package cn.ysp.object;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Recorder {
	private double utility;
	private long wait_time_realtime_only;
	private long wait_time;
	private int realtime_r_num;
	private int realtime_r_success_num;
	private int appointment_r_num;
	private int appointment_r_success_num;
	private double income;
	
	public Recorder(){
		utility = 0;
		wait_time = 0;
		wait_time_realtime_only = 0;
		realtime_r_num = 0;
		realtime_r_success_num = 0;
		appointment_r_num = 0;
		appointment_r_success_num = 0;
		income = 0;
	}
	
	public void addUtility( double u){
		utility+=u;
	}
	
	public void addWaitTime(long time){
		wait_time += time;
	}
	
	public void addRealTimeWaitTime(long time){
		wait_time_realtime_only += time;
	}
	
	public void addRealTimeRNum(int num){
		realtime_r_num += num;
	}
	
	public void addRealTimeRSuccessNum(int num){
		realtime_r_success_num += num;
	}
	
	public void addAppointmentRNum(int num){
		appointment_r_num += num;
	}
	
	public void addAppointmentRSuccessNum(int num){
		appointment_r_success_num += num;
	}
	
	public void addRealTimeIncome(double length){
		if(length < 3000){
			income += 13;
		}
		else{
			double extraLength = length - 3000;
			income = income + (extraLength/1000)*2 + 13;
		}
	}
	
	public void addAppointmentIncome(double length){
		income += 20;
		if(length < 3000){
			income += 13;
		}
		else{
			double extraLength = length - 3000;
			income = income + (extraLength/1000)*2 + 13;
		}
	}
	
	public void writeOn() throws IOException{
		FileOutputStream out = new FileOutputStream(new File("C:\\Users\\wydn1\\Desktop\\record.txt"));
		String outStr = "Utility:\r\n";
		outStr = "Utility:\r\n";
		outStr = outStr+utility+"\r\n";
		out.write(outStr.getBytes());
		
		outStr = "TotalWaitTime:\r\n";
		outStr = outStr+wait_time+"\r\n";
		out.write(outStr.getBytes());
		
		outStr = "RealTimeWatiTime:\r\n";
		outStr = outStr+wait_time_realtime_only+"\r\n";
		out.write(outStr.getBytes());
		
		outStr = "RealTimeRequest:\r\n";
		outStr = outStr+realtime_r_num+"\r\n";
		out.write(outStr.getBytes());
		
		outStr = "RealTimeRequestSuccess:\r\n";
		outStr = outStr+realtime_r_success_num+"\r\n";
		out.write(outStr.getBytes());
		
		outStr = "AppointmentRequest:\r\n";
		outStr = outStr+appointment_r_num+"\r\n";
		out.write(outStr.getBytes());
		
		outStr = "AppointmentRequestSuccess:\r\n";
		outStr = outStr+appointment_r_success_num+"\r\n";
		out.write(outStr.getBytes());
		
		outStr = "Income:\r\n";
		outStr = outStr+income+"\r\n";
		out.write(outStr.getBytes());
	}
}
