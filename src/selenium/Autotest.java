package selenium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
//import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class Autotest {
	static String url = "jdbc:mysql://localhost:3306/autotest?user=root&password=root";
	static String login = "root";
	static String password = "root";

	public static void main(String[] args) throws Exception {
		System.setProperty("webdriver.chrome.driver", 
        		"/usr/local/Cellar/chromedriver/2.29/bin/chromedriver");
        System.setProperty("webdriver.chrome.logfile", "/Users/thao786/log");
        
		int test_id = 39;
		ResultSet result = null;
		Statement statement = null;
		int lastestOrder = 0;
		
		// chrome_tab => webDriver
		HashMap<String, WebDriver> driverMap = new HashMap<String, WebDriver>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = (Connection) DriverManager.getConnection(url, login, password);
			statement = (Statement) connection.createStatement();
			result = statement.executeQuery("Select * FROM steps s WHERE s.test_id="+test_id
			+ " AND s.active = true");
		}
	    catch ( SQLException e ) {
	        System.out.println(e.getMessage());
	    }
		
	    while(result.next()) {
	    	int order = Integer.parseInt(result.getString("order"));
	    	String action_type = result.getString("action_type");
	    	String chrome_tab = result.getString("chrome_tab");
	    	String webpage = result.getString("webpage");
	    	int scrollLeft, scrollTop, wait = Integer.parseInt(result.getString("wait"));
	    	String selectorJSON;
	    	
	    	WebDriver driver = null;
	    	JavascriptExecutor jse = null;
	    	
	    	if (driverMap.containsKey(chrome_tab)) {
	    		driver = driverMap.get(chrome_tab);
    	    } else {
    	    	DesiredCapabilities cap = DesiredCapabilities.chrome();
    			LoggingPreferences pref = new LoggingPreferences();
    			pref.enable(LogType.BROWSER, Level.ALL);
    			cap.setCapability(CapabilityType.LOGGING_PREFS, pref);
    	       
    	        driver = new ChromeDriver(cap);
    	    	driverMap.put(chrome_tab, driver);
    	    	// first step in a new tab must be a pageload
    	    }
	    	
	    	jse = (JavascriptExecutor)driver;
	    	
	    	switch (action_type) {
	            case "pageload":
	            	driver.get(webpage);
	            	driver.manage().window().setSize(new Dimension(
	    	        		Integer.parseInt(result.getString("screenwidth")), 
	    	        		Integer.parseInt(result.getString("screenheight"))));
	            	break;
	            case "scroll":
	            	scrollLeft = Integer.parseInt(result.getString("scrollLeft"));
	            	scrollTop = Integer.parseInt(result.getString("scrollTop"));
	            	jse.executeScript("scroll("+ scrollLeft +", "+ scrollTop +")");
	            	break;
	            case "keypress":
	            	String typed = result.getString("typed");
	            	Actions action = new Actions(driver);
	                action.sendKeys(typed);
	                action.perform();
	            	break;
	            case "click":
	            	selectorJSON = result.getString("selector"); // in json format
	            	JSONParser parser = new JSONParser();
	        		JSONObject json = (JSONObject) parser.parse(selectorJSON);
	        		String selectorType = (String) json.get("selectorType");
	        		String selector = (String) json.get("selector");
	        		int eq = (int) json.get("eq");
	        		WebElement element = null;
	        		
	        		switch (selectorType) {
		                case "id":
		                	element = driver.findElement(By.id(selector));
		                	break;
		                case "class":  
		                	element = driver.findElements(By.className(selector)).get(eq);
		                	break;
		                case "tag":  
		                	element = driver.findElements(By.tagName(selector)).get(eq);
		                	break;
		                case "name":  
		                	element = driver.findElements(By.name(selector)).get(eq);
		                	break;
		                case "partialLink": 
		                	element = driver.findElements(By.partialLinkText(selector)).get(eq);
		                	break;
		                case "href":  
		                	element = driver.findElements
		                				(By.cssSelector("a[href='" + selector + "']")).get(eq);
		                	break;
		                case "button":  
		                	element = driver.findElements
            							(By.cssSelector("button:contains('" + selector + "')")).get(eq);		                	break;
		                case "css": 
		                	element = driver.findElements(By.cssSelector(selector)).get(eq);
		                	break;
		                case "coordination":  
		                	int x = (int) json.get("x");
		                	int y = (int) json.get("y");
		                	WebElement dummy = driver.findElement(By.id("foo"));
		                	Actions act = new Actions(driver);
		                    act.moveToElement(dummy).moveByOffset(x, y).click().perform();
		                	break;
		                default: break;
	        		}
	        		
	        		if (element != null)
	        			element.click();
	        		
	            	break;
	            case "resize":
	            	break;
	            default:
	            	break;
	        }
	      }
	    
	    // check assertions
//	    result = statement.executeQuery("Select * FROM assertions a WHERE a.test_id="+test_id
//				+ " AND a.active = true");
	    
	}
}
