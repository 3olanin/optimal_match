package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.neo4j.graphdb.Node;

import cn.ysp.map.Neo4jMap;
import cn.ysp.optimal_match.StaticMatch;

public class ProduceRequest {
	
	public static String NEO4JPATH = "D:/Neo4jDB_2_2";	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Neo4jMap n4jMap = new Neo4jMap(NEO4JPATH);
		FileOutputStream out = new FileOutputStream(new File("C:\\Users\\wydn1\\Desktop\\requestFile2.txt"));
		int count=0;
		long clock = 1404144001000L;
		double min_lon = 118.0684;
		double max_lon = 118.1933;
		double min_lat = 24.4278;
		double max_lat = 24.5565;
		while(count < 3600){
			double lon = nextDouble(min_lon,max_lon);
			double lat = nextDouble(min_lat,max_lat);
			Node startNode = StaticMatch.locateOsmNode(lon, lat, n4jMap);
			double lon2 = nextDouble(min_lon,max_lon);
			double lat2 = nextDouble(min_lat,max_lat);
			Node endNode = StaticMatch.locateOsmNode(lon2, lat2, n4jMap);
			//时间窗10分钟
			//预约请求预约时间为15分钟
			//每5秒生成一个请求
			//实时请求与预约请求的比例为10：1
			if(startNode != null && endNode!= null){
				count++;
				long t0 = clock;
				long t1;
				long t2;
				if(count %10 ==0){
					t1 = t0 + 900000;
					t2 = t1 + 600000;
				}
				else{
					t1 = t0 + 1000;
					t2 = t1 + 600000;
				}
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String outStr = String.valueOf(lon)+"#"+String.valueOf(lat)+"#";
				outStr = outStr + String.valueOf(lon2)+"#"+String.valueOf(lat2)+"#";
				outStr = outStr + sdf.format(new Date(Long.parseLong(String.valueOf(t0)))) + "#" + sdf.format(new Date(Long.parseLong(String.valueOf(t1)))) +"#" 
						 +sdf.format(new Date(Long.parseLong(String.valueOf(t2))))+"#\r\n";
				out.write(outStr.getBytes());
				clock += 3000;
			}
		}

	}
	
	public static double nextDouble(final double min, final double max) {  
	    return min + ((max - min) * new Random().nextDouble());  
	}  

}
