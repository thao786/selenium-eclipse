package selenium;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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
	public WebDriver driver;
	
	public boolean switchTab(String url) {
		ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
        for (int i = 0; i< tabs.size(); i++) {
        	driver.switchTo().window((String) tabs.get(i));
			if (url == driver.getCurrentUrl()) // how to check for new tab? not match http or ftp
				return true;
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("webdriver.chrome.driver",
        		"/usr/local/Cellar/chromedriver/2.29/bin/chromedriver");
        System.setProperty("webdriver.chrome.logfile", "/Users/thao786/log");
        
        Autotest autoTest = new Autotest();
		int test_id = 42;
		String runId = "runId";
		ResultSet result = null;
		Statement statement = null;
		Set<String> chromeTabs = new HashSet<>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = (Connection) DriverManager.getConnection(url, login, password);
			statement = (Statement) connection.createStatement();
			result = statement.executeQuery("Select * FROM steps s WHERE s.test_id=" + test_id
			+ " AND s.active = true");
		} catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		
    	DesiredCapabilities cap = DesiredCapabilities.chrome();
		LoggingPreferences pref = new LoggingPreferences();
		pref.enable(LogType.BROWSER, Level.ALL);
		cap.setCapability(CapabilityType.LOGGING_PREFS, pref);
		autoTest.driver = new ChromeDriver(cap);
		JavascriptExecutor jse = (JavascriptExecutor)autoTest.driver;
		// check if page contains JQuery, otherwise insert
		
	    while(result.next()) {
	    	int order = Integer.parseInt(result.getString("order"));
	    	String action_type = result.getString("action_type");
	    	String webpage = result.getString("webpage");
	    	int scrollLeft, scrollTop, wait = Integer.parseInt(result.getString("wait"));
	    	String selectorJSON;
	    	
	    	String chromeTabWindow = result.getString("tabId") + "-" + result.getString("windowId");
	    	// check if we need to switch tab
	    	// only compare root urls (non anchor link)
	    	if (autoTest.driver.getCurrentUrl() != webpage) {
	    		// if this chrome tab never appears before, open a new tab and switch to it
	    		if (!chromeTabs.contains(chromeTabWindow)) {
	    			WebElement element = (WebElement) ((JavascriptExecutor)autoTest.driver)
	    	        		.executeScript("window.open('');");
	    			autoTest.switchTab("about:blank"); // Chrome only
	    		} else {
	    			// if we have this tab already, check all tabs, find the first page with this url
		    		// if find none: if this is not a pageload, throw error, not found such url
	    			 if (!autoTest.switchTab(webpage)) {
	    				 System.out.println("Cant find tab with url " + webpage);
	    				 return;
	    			 }
	    		}
        	}
	    	chromeTabs.add(chromeTabWindow);
	    	TimeUnit.MILLISECONDS.sleep(wait);
	    	
	    	switch (action_type) {
	            case "pageload":
	            	// only load new webpage if we dont have it yet
	            	// pageload could result from a link click (no actual reload in this case)
	            	if (autoTest.driver.getCurrentUrl() != webpage) {
	            		autoTest.driver.get(webpage);
	            	}
	            	break;
	            case "scroll":
	            	scrollLeft = Integer.parseInt(result.getString("scrollLeft"));
	            	scrollTop = Integer.parseInt(result.getString("scrollTop"));
	            	jse.executeScript("scroll("+ scrollLeft +", "+ scrollTop +")");
	            	break;
	            case "keypress":
	            	String typed = result.getString("typed");
	            	Actions action = new Actions(autoTest.driver);
	                action.sendKeys(typed);
	                action.perform();
	            	break;
	            case "click":
	            	selectorJSON = result.getString("selector"); // in json format
	            	JSONParser parser = new JSONParser();
	        		JSONObject json = (JSONObject) parser.parse(selectorJSON);
	        		String selectorType = (String) json.get("selectorType");
	        		String selector = (String) json.get("selector");
	        		int eq = Integer.parseInt(json.get("eq") + "");
	        		WebElement element = null;
	        		
	        		switch (selectorType) {
		                case "id":
		                	element = autoTest.driver.findElement(By.id(selector));
		                	break;
		                case "class":  
		                	element = autoTest.driver.findElements(By.className(selector)).get(eq);
		                	break;
		                case "tag":
		                	element = autoTest.driver.findElements(By.tagName(selector)).get(eq);
		                	break;
		                case "name":  
		                	element = autoTest.driver.findElements(By.name(selector)).get(eq);
		                	break;
		                case "partialLink": 
		                	element = autoTest.driver.findElements(By.partialLinkText(selector)).get(eq);
		                	break;
		                case "href":  
		                	element = autoTest.driver.findElements
		                				(By.cssSelector("a[href='" + selector + "']")).get(eq);
		                	break;
		                case "button":
		                	element = (WebElement) ((JavascriptExecutor)autoTest.driver)
		                		.executeScript("return $('button:contains(\"" + selector + "\")')[0]");
		                	break;
		                case "css":
		                	element = autoTest.driver.findElements(By.cssSelector(selector)).get(eq);
		                	break;
		                case "coordination":
		                	int x = (int) json.get("x");
		                	int y = (int) json.get("y");
		                	WebElement dummy = autoTest.driver.findElement(By.id("foo"));
		                	Actions act = new Actions(autoTest.driver);
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
	    	
	    	TakesScreenshot scrShot =((TakesScreenshot)autoTest.driver);
	        File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
	        File DestFile = new File("/Users/thao786/"+ test_id +"-" + runId + "-"+ order + "a.jpg");
	        // copy file to S3
	        FileUtils.copyFile(SrcFile, DestFile);
	        // delete file on server
	      }
	    
	    // check assertions
//	    result = statement.executeQuery("Select * FROM assertions a WHERE a.test_id=" + test_id
//				+ " AND a.active = true");
	    
	}
}
