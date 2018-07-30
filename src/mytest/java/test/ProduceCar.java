package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.neo4j.graphdb.Node;

import cn.ysp.map.Neo4jMap;
import cn.ysp.optimal_match.StaticMatch;

public class ProduceCar {
	
	public static String NEO4JPATH = "D:/Neo4jDB_2_2";	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Neo4jMap n4jMap = new Neo4jMap(NEO4JPATH);
		FileOutputStream out = new FileOutputStream(new File("C:\\Users\\wydn1\\Desktop\\carFile2.txt"));
		int count=0;
		double min_lon = 118.0684;
		double max_lon = 118.1933;
		double min_lat = 24.4278;
		double max_lat = 24.5565;
		while(count < 500){
			double lon = nextDouble(min_lon,max_lon);
			double lat = nextDouble(min_lat,max_lat);
			Node startNode = StaticMatch.locateOsmNode(lon, lat, n4jMap);
			if(startNode != null){
				count++;
				String outStr = String.valueOf(lon)+"#"+String.valueOf(lat)+"\r\n";
				out.write(outStr.getBytes());
			}
		}

	}
	
	public static double nextDouble(final double min, final double max) {  
	    return min + ((max - min) * new Random().nextDouble());  
	}  

}
