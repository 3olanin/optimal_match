package cn.ysp.object;

import java.util.ArrayList;
import java.util.List;

public class GbSink {
	List<GbRequest> gbRequestList;
	
	public GbSink(){
		gbRequestList = new ArrayList<GbRequest>();
	}
	
	public boolean addRequest(GbRequest gbc){
		return gbRequestList.add(gbc);
	}
	
}
