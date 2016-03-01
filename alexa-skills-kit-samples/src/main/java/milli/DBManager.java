package milli;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBManager {
  private Connection conn = null;
  private PreparedStatement stmt = null;
  private ResultSet resultSet = null;


  public int applianceStatusRead(String applianceName) throws Exception {
    try {
      // This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      // Setup the connection with the DB
      conn = DriverManager.getConnection("jdbc:mysql://localhost/appliances?"
              + "user=sqluser&password=sqluserpw");

      // Statements allow to issue SQL queries to the database
      stmt = conn.prepareStatement("SELECT status FROM appliances WHERE name=?");
      stmt.setString(1, applianceName);      
      resultSet = stmt.executeQuery();
      if (!resultSet.next()){
        return -1;
      } else {
	return resultSet.getInt(0);
      } 	
      
    } catch (Exception e) {
      throw e;
    } finally {
      conn.close();
    }
  }
  
  public String applianceTypeRead(String appName) throws Exception {
	  
	  return "switch";
  }
  
  
  public void statusToggle(String applianceName) throws Exception {
    try {
      // This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      // Setup the connection with the DB
      conn = DriverManager.getConnection("jdbc:mysql://localhost/appliances?"
              + "user=sqluser&password=sqluserpw");

      int status = applianceStatusRead(applianceName);
      int newStatus = ~status;
      stmt = conn.prepareStatement("UPDATE appliances SET" + newStatus + "WHERE name=?");
      stmt.setString(1, applianceName); 
      stmt.executeQuery();

      
    } catch (Exception e) {
      throw e;
    } finally {
      conn.close();
    }

  }

public String getMacAddr(String aPPLIANCE_NAME) {
	// TODO Auto-generated method stub
	return null;
}

public String getActionFromStatus(int aPPLIANCE_STATUS) {
	// TODO Auto-generated method stub
	return null;
}



}
