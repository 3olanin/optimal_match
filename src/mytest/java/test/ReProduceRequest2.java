package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.neo4j.graphdb.Node;

import cn.ysp.events.Event;
import cn.ysp.events.RequestCreatedEvent;
import cn.ysp.map.Neo4jMap;
import cn.ysp.optimal_match.StaticMatch;

public class ReProduceRequest2 {

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		
		int pickup = 600000;
		
		String infile = "C:\\Users\\wydn1\\Desktop\\requestFile2.txt";
		String outfile = "C:\\Users\\wydn1\\Desktop\\requestFile_only_realtime.txt";
		File requestFile=new File(infile);
		InputStreamReader read = new InputStreamReader(new FileInputStream(requestFile));
		BufferedReader bufferedReader = new BufferedReader(read);
		
	    FileOutputStream out = new FileOutputStream(new File(outfile));


		String lineTxt = "";
		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		while((lineTxt = bufferedReader.readLine()) != null){
			String s[]=lineTxt.split("#");
//			double olon=Float.valueOf(s[0]);
//			double olat=Float.valueOf(s[1]);
//			double dlon=Float.valueOf(s[2]);
//			double dlat=Float.valueOf(s[3]);
			String timeString = s[4];
		    Date date = format.parse(timeString);
		    long timeStamp = (long) (date.getTime());
//		    String timeString2 = s[5];
//		    Date date2 = format.parse(timeString2);
//		    long t1 = (long) (date2.getTime());
//		    String timeString3 = s[6];
//		    Date date3 = format.parse(timeString3);
//		    long t2 = (long) (date3.getTime());
		
		
		    long t1 = timeStamp + 1000;
		    long t2 = t1 + pickup;
		    

			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String outStr = s[0]+"#"+s[1]+"#"+s[2]+"#"+s[3]+"#"+format.format(new Date(timeStamp))+"#"+format.format(new Date(t1))+"#"+format.format(new Date(t2))+"#\r\n";

			out.write(outStr.getBytes());
		
		}
	}

}
