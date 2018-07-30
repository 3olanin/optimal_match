package cn.ysp.optimal_match;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import cn.ysp.map.MySqlDB;

/*
 * cost matrix
 */
public class CostMatrixManager {

	private Map<Long, Map<Long, Long>> costMatrix;
	private MySqlDB mySqlDB;
	
	
	public CostMatrixManager() throws SQLException{
		mySqlDB = MySqlDB.getInstance();
		costMatrix = new HashMap<Long, Map<Long, Long>>();
		String sql = "SELECT distinct(from_box) FROM box_distance ";
		ResultSet rs = mySqlDB.queryStatement(sql);
		try {
			while(rs.next()){
				long from_box = rs.getInt(1);
				costMatrix.put(from_box, new HashMap<Long, Long>());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//load the Matrix depend on time
	public void loadMatrix(int hour_id) throws SQLException{
		mySqlDB = MySqlDB.getInstance();
		String sql1 = "SELECT distinct(from_box) FROM box_distance ";
		ResultSet rs1 = mySqlDB.queryStatement(sql1);
		String sql2 = "SELECT distinct(to_box) FROM box_distance ";
		ResultSet rs2 = mySqlDB.queryStatement(sql2);
		ArrayList<Long> fromList = new ArrayList<Long>(); 
		ArrayList<Long> toList = new ArrayList<Long>();
		while(rs1.next()){
			long from_box = rs1.getInt(1);
			fromList.add(from_box);
		}
		while(rs2.next()){
			long to_box = rs2.getInt(1);
			toList.add(to_box);
		}
		Iterator it1 = fromList.iterator();
		while(it1.hasNext()){
			long from_box = (long) it1.next();
			Iterator it2 = toList.iterator();
			while(it2.hasNext()){
				long to_box = (long) it2.next();
				String sql3 = "SELECT pass_time FROM box_distance Where from_box = "+from_box +" and to_box = "+to_box +" and hour_id = "+hour_id;
				ResultSet rs3 = mySqlDB.queryStatement(sql3);
				//default value 1200000
				long pass_time = 1200000;
				if(rs3.next()){
					pass_time = rs3.getInt(1);
				}
				putValue(from_box, to_box, pass_time);
			}
		}
		mySqlDB.mydbClose();
	}
	
	public void putValue(long from_box, long to_box, long pass_time){
		HashMap<Long, Long> subM = (HashMap<Long, Long>) costMatrix.get(from_box);
		subM.put(to_box, pass_time);
		costMatrix.put(from_box, subM);
	}
	
	public long getValue(long from_box, long to_box){
		return costMatrix.get(from_box).get(to_box);
	}
	
}
