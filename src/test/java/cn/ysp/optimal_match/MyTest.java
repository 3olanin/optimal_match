package cn.ysp.optimal_match;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import cn.ysp.map.Neo4jMap;


public class MyTest {
	public static String NEO4JPATH = "D:/Neo4jDB_2_2";
	public static void main(String[] args) throws NumberFormatException, IOException, ParseException, SQLException {
//		CostMatrixManager cmm = new CostMatrixManager();
//		cmm.loadMatrix(1);
//		System.out.println(cmm.getValue(2, 58));
		Neo4jMap n4jMap = new Neo4jMap(NEO4JPATH);
		double slon = 118.1249008178711;
		double slat = 24.496599197387695;
		double elon = 118.11129760742188;
		double elat = 24.475862503051758;
		//Node n = StaticMatch.locateOsmNode(slon, slat, n4jMap);
		Transaction tx = n4jMap.getDB().beginTx();
		Node n = n4jMap.getDB().getNodeById(45941);
		System.out.println("lon = " + n.getProperty("lon")+ " ,lat = "+ n.getProperty("lat"));
		//System.out.println("id = " + n.getId() );
	}
}
