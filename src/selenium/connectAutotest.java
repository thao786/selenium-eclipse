package selenium;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class connectAutotest {
	static String url = "jdbc:mysql://localhost:3306/autotest?user=root&password=root";
	static String login = "root";
	static String password = "root";
	
	public ArrayList<Step> getSteps(int test_id) throws Exception {
		ArrayList<Step> steps = new ArrayList<Step>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = (Connection) DriverManager.getConnection(url, 
					login, password);
			Statement statement = (Statement) connection.createStatement();
			ResultSet result = statement.executeQuery("Select * FROM steps s WHERE s.test_id="+test_id
			+ " AND s.active = true");

		    while(result.next()) {
	    	   System.out.println(result.getString("id") + " " + result.getString("action_type"));	
		        Step step = new Step();
		        step.id = Integer.parseInt(result.getString("id"));
		        step.selector = result.getString("selector");
		        step.action_type = result.getString("action_type");
		        step.typed = result.getString("typed");
		        step.webpage = result.getString("webpage");
		        step.device_type = result.getString("device_type");
		        step.chrome_tab = result.getString("chrome_tab");
		        
		        step.scrollTop = Integer.parseInt(result.getString("scrollTop"));
		        step.scrollLeft = Integer.parseInt(result.getString("scrollLeft"));
		        step.wait = Integer.parseInt(result.getString("wait"));
		        step.order = Integer.parseInt(result.getString("order"));
		        
		        steps.add(step);
		     }
	    }
	    catch ( SQLException e ) {
	        System.out.println(e.getMessage());
	    }
		
		return steps;
	}
	
    public static void main(String[] args) throws Exception {
		
    }
}
