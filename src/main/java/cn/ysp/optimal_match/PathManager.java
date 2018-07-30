package cn.ysp.optimal_match;

import java.util.Iterator;
import java.util.LinkedList;

import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.osm.OSMDataset;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.osm.OSMRelation;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

//Methods about Path
public class PathManager {
	
	//find the shortest path between two node
	public static WeightedPath findShortestPath(GraphDatabaseService db, Node node1, Node node2, int minute_id){
		SpatialDatabaseService spatial = new SpatialDatabaseService(db);
		//此处并非读取文件，"D:\\毕设\\lz路网资料\\地图处理全步骤\\map_highway.osm"不是一个地址，而是neo4j数据库中spatial_root节点的LAYER关系的下一个节点的layer属性值
    	//OSMLayer layer = (OSMLayer) spatial.getLayer("C:\\Users\\wydn1\\Desktop\\xiamenosm\\map.osm");
		OSMLayer layer = (OSMLayer) spatial.getLayer("D:\\毕设\\lz路网资料\\地图处理全步骤\\map_highway.osm");
    	Node layerNode=layer.getLayerNode();

    	OSMDataset osmDs=new OSMDataset(spatial,layer,layerNode);
    	WeightedPath path = osmDs.getdijkstraLengthShortestPath(node1, node2, minute_id);
    	return path;
	}

	//get the length of a weightedPath
	public static double getWeightedPathDistance(WeightedPath p){
		double distance = 0;
		Iterable<Relationship> rr = p.relationships();
		Iterator<Relationship> itr=rr.iterator();
		//System.out.println("weight = " + p.weight());
		while(itr.hasNext())
		{
			Relationship r = itr.next();
			if(r.isType(OSMRelation.NEXT))
			distance += (double)r.getProperty("length");
		}
		return distance;
	}

/*
 * convert Iterable<Relationship> into LinkedList<Relationship> and delete OSMRelation.NODE
 */
	public static LinkedList<Relationship> iterable2LinkedListPath(Iterable<Relationship> rr){
		LinkedList<Relationship> list = new LinkedList<Relationship>();
		Iterator<Relationship> itr=rr.iterator();
		while(itr.hasNext())
		{
			Relationship r = itr.next();
			if(r.isType(OSMRelation.NEXT))
				list.add(r);
		}
		return list;
	}
}
