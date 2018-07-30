package cn.ysp.map;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.cj.api.jdbc.Statement;
import com.mysql.cj.jdbc.PreparedStatement;

public class MySqlDB {
    private static final String URL="jdbc:mysql://localhost:3306/speed_network?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&rewriteBatchedStatements=true";//数据库连接字符串，这里的deom为数据库名  
    private static final String NAME="root";//登录名  
    private static final String PASSWORD="";//密码  
    private String driver = "com.mysql.jdbc.Driver";
    private Connection conn ;
    private PreparedStatement psql;
    
    
    private MySqlDB()
    {
        //1.加载驱动  
        try {  
            Class.forName(driver);  
        } catch (ClassNotFoundException e) {  
            System.out.println("未能成功加载驱动程序，请检查是否导入驱动程序！");  
                        //添加一个println，如果加载驱动异常，检查是否添加驱动，或者添加驱动字符串是否错误  
            e.printStackTrace();  
        }  
        //conn = null;  
        try {  
            conn = DriverManager.getConnection(URL, NAME, PASSWORD);
                System.out.println("获取Mysql数据库连接成功！");  
        } catch (SQLException e) {  
            System.out.println("获取数据库连接失败！");  
                        //添加一个println，如果连接失败，检查连接字符串或者登录名以及密码是否错误  
            e.printStackTrace();  
        }  
    }
    private static MySqlDB mydb=null;
    //静态工厂方法   
    public static MySqlDB getInstance() throws SQLException{
         if (mydb == null) {    
             mydb = new MySqlDB();
         }
         else{
        	 mydb.mydbClose();
        	 mydb = new MySqlDB();
         }
        return mydb; 
    }
 
    
    
    public ResultSet queryStatement(String sql) {
    	Statement statement = null;
    	ResultSet rs = null;
		try {
			statement = (Statement) conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			 rs = statement.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return rs;
    }
    
    
    public boolean updateStatement(String sql){
    	Statement statement = null;
    	boolean f = false;
    	try {
			statement = (Statement) conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			f = statement.execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return f;
    }
    
    public void mydbClose() throws SQLException
    {
        if(conn!=null)
        {  
            try {  
                conn.close();  
            } catch (SQLException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
                conn=null;  
            }  
        }  
    }
    
    
    public PreparedStatement getPreparedStatement(String sql) throws SQLException{
    	return psql = (PreparedStatement) conn.prepareStatement(sql);
    }
    
}