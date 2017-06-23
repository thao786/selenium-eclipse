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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class Autotest {
	public WebDriver driver;
	public int test_id;
	public String runId;
	Connection connection;
	
	public Autotest(int test, String runId) {
		this.test_id = test;
		this.runId = runId;
	}
	
	public boolean switchTab(String url) {
		ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
        for (int i = 0; i< tabs.size(); i++) {        	
        	driver.switchTo().window((String) tabs.get(i));
        	System.out.println(driver.getCurrentUrl());
        	
			if (url == driver.getCurrentUrl()) // how to check for new tab? not match http or ftp
				return true;
		}
		return false;
	}
	
	public static boolean isBlank(String url) {
		ArrayList<String> emptyUrls = new ArrayList<String>();
		emptyUrls.add("");
		emptyUrls.add("about:blank");
		emptyUrls.add("data:,");
		if (emptyUrls.contains(url))
			return true;
		
		return false;
	}

	public void screenShot(String awsFileName) {
		try {
			String awsPath = config.awsPath();
			
			TakesScreenshot scrShot = ((TakesScreenshot)driver);
	        File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
	        
	        // copy file to S3
	        Runtime.getRuntime().exec("cp " + SrcFile.getAbsolutePath() +
	        		" " + config.picDir() + awsFileName);
	        
	        // resize pic
	        Runtime.getRuntime().exec(config.convertPath() + " " + config.picDir() + awsFileName + 
	        		" -resize 50% " + config.picDir() + awsFileName);
	        
			Runtime.getRuntime().exec(awsPath + " s3 cp --acl public-read " 
					+ " " + config.picDir() + awsFileName
					+ " s3://autotest-test/" + awsFileName);
			
			// tag with test value
			Runtime.getRuntime().exec(awsPath + " s3api put-object-tagging " 
					+ " --bucket " + config.bucket()
					+ " --key " + awsFileName
					+ "--tagging 'TagSet=[{Key=runId,Value=" + runId + "}]'");
			
			SrcFile.delete();
		} catch (Exception e) {
			System.out.println(e.getMessage());
	    }
	}
	
	public void checkAssertion(ResultSet assertions) throws Exception {
		String textInPage, pageSource;
		WebElement body;
		String condition = assertions.getString("condition");
    	String assertion_type = assertions.getString("assertion_type");
    	int assertion_id = Integer.parseInt(assertions.getString("id"));
    	
		switch (assertion_type) {
	        case "text-in-page":
	        	body = driver.findElements(By.className("body")).get(0);
	        	textInPage = body.getText();
	        	if (!textInPage.contains(condition))
	        		(new Result(test_id, 0, runId, "",
	        				assertion_id, driver.getCurrentUrl())).sync();
	        	break;
	        case "text-not-in-page":
	        	body = driver.findElements(By.className("body")).get(0);
	        	textInPage = body.getText();
	        	if (textInPage.contains(condition))
	        		(new Result(test_id, 0, runId, "",
	        				assertion_id, driver.getCurrentUrl())).sync();
	        	break;
	        case "html-in-page":
	        	pageSource = driver.getPageSource();
	        	if (!pageSource.contains(condition))
	        		(new Result(test_id, 0, runId, "",
	        				assertion_id, driver.getCurrentUrl())).sync();
	        	break;
	        case "html-not-in-page":
	        	pageSource = driver.getPageSource();
	        	if (pageSource.contains(condition))
	        		(new Result(test_id, 0, runId, "",
	        				assertion_id, driver.getCurrentUrl())).sync();
	        	break;
	        case "page-title":
	        	String title = driver.getTitle();
	        	if (title.contains(condition))
	        		(new Result(test_id, 0, runId, "",
	        				assertion_id, driver.getCurrentUrl())).sync();
	        	break;
//	        case "status-code": // check console log
//	        	break;
	        case "self-enter":
	        	break;
	        default: break;
		}
	}
	
	public void checkAssertions() throws Exception {
		// check assertions
		Statement selectStm = (Statement) connection.createStatement();
		ResultSet assertions = selectStm.executeQuery("Select * FROM assertions a WHERE a.test_id=" + 
	    		test_id + " AND a.active = true");
	    
		ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
		// check browser console
        for (int i = 0; i< tabs.size(); i++) {
        	driver.switchTo().window((String) tabs.get(i));
        	String webpage = driver.getCurrentUrl();
			
        	LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            for (LogEntry log : logs) {
                String msg = log.getMessage();
                if (msg.contains("Failed to load resource: the server responded with a status")) {
                	(new Result(test_id, 0, runId, msg,
                			Result.STATUS_ASSERTION, webpage)).sync();
                }
            }
		}
        
        // check assertions
        while(assertions.next()) {
        	String webpage = assertions.getString("webpage");
        	for (int i = 0; i< tabs.size(); i++) {
            	driver.switchTo().window((String) tabs.get(i));
            	String currentUrl = driver.getCurrentUrl();
            	
            	if (currentUrl == webpage) {
            		checkAssertion(assertions);
            		break;
            	}
            	
            	// if no webpage specified, try all tabs
            	if (webpage.trim().length() == 0)
            		checkAssertion(assertions);
    		}
        }
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("webdriver.chrome.driver",
        		"/usr/local/Cellar/chromedriver/2.29/bin/chromedriver");
        System.setProperty("webdriver.chrome.logfile", "/Users/thao786/log");
        
        Autotest autoTest = new Autotest(1, "runId");
		Set<String> chromeTabs = new HashSet<>();
		
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		autoTest.connection = (Connection) DriverManager
				.getConnection(config.url(), config.login(), config.password());
		Statement selectStm = (Statement) autoTest.connection.createStatement();
		ResultSet result = selectStm.executeQuery("Select * FROM steps s WHERE s.test_id=" 
				+ autoTest.test_id + " AND s.active = true");
					   
    	DesiredCapabilities cap = DesiredCapabilities.chrome();
		LoggingPreferences pref = new LoggingPreferences();
		pref.enable(LogType.BROWSER, Level.ALL);
		cap.setCapability(CapabilityType.LOGGING_PREFS, pref);
		autoTest.driver = new ChromeDriver(cap);
		JavascriptExecutor jse = (JavascriptExecutor)autoTest.driver;
		// check if page contains JQuery, otherwise insert
		
	    while(result.next()) {
	    	int order = Integer.parseInt(result.getString("order"));
	    	int step_id = Integer.parseInt(result.getString("id"));
	    	String action_type = result.getString("action_type");
	    	String webpage = result.getString("webpage");
	    	int scrollLeft, scrollTop, wait = Integer.parseInt(result.getString("wait"));
	    	String selectorJSON;
	    	
	    	String chromeTabWindow = result.getString("tabId") + "-" + result.getString("windowId");
	    	// check if we need to switch tab
	    	// only compare root urls (non anchor link)
	    	String currentUrl = autoTest.driver.getCurrentUrl();
	    	if (!isBlank(currentUrl)) {
	    		if (!currentUrl.equals(webpage)) {
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
		    				 (new Result(autoTest.test_id, step_id, autoTest.runId, 
	    			        		"mismatch urls: expect " + webpage + ", got " + currentUrl, 
	    			        		Result.URL_MATCH_ASSERTION, currentUrl)).sync();
		    			 }
		    		}
	    		}
        	}
	    	chromeTabs.add(chromeTabWindow);
	    	TimeUnit.MILLISECONDS.sleep(wait);
	    	
	    	try {
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
	    	} catch (SQLException e) {
		        (new Result(autoTest.test_id, step_id, autoTest.runId, 
		        		e.getMessage(), Result.STEP_SUCCESS_ASSERTION, currentUrl)).sync();
		    }
	    	
	    	autoTest.screenShot(autoTest.runId + "-"+ order + ".jpg");
	    }
	    
	    autoTest.checkAssertions();
	    
	    (new Result(autoTest.test_id, 0, autoTest.runId, "", Result.REPORT_ASSERTION)).sync();
	    autoTest.driver.quit();
	    return;
	}
}
