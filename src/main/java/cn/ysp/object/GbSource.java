package cn.ysp.object;

import java.util.ArrayList;
import java.util.List;

public class GbSource {
	
	List<GbCar> gbCarList;
	
	public GbSource(){
		gbCarList = new ArrayList<GbCar>();
	}
	
	public boolean addCar(GbCar gbc){
		return gbCarList.add(gbc);
	}
	
}
