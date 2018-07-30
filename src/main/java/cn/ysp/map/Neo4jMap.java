package cn.ysp.map;

import java.io.File;
import java.util.Map;
import java.util.Random;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Neo4jMap {
	
	private File dbPath;
	private GraphDatabaseService db;
	
	public Neo4jMap(String filename){
		init(filename);
	}
	
	public void init(String filename){
		dbPath = new File(filename);
		db =  new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath).newGraphDatabase();
	}
	
	public GraphDatabaseService getDB(){
		return db;
	}
	
	public Result execute(String query, Map<String, Object> parameters){
		return db.execute(query, parameters);
	}
	
	public Result execute(String query){
		return db.execute(query);
	}
	
	public Node getRandomNode(){
		Random r = new Random();
		//the ROADNODE id range [20990,54684]，but there are still some other kind of nodes in that range
		long nodeid = r.nextInt(54684 - 20990 + 1) + 20990;
		Node node = db.getNodeById(nodeid);
		while(!node.hasProperty("lat")){
			nodeid = r.nextInt(54684 - 20990 + 1) + 20990;
			node = db.getNodeById(nodeid);
		}
		
		return node;
	}
}
