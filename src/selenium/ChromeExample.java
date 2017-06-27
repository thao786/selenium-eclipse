package selenium;

import java.io.IOException;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
 
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
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
 

public class ChromeExample  {
    public static void getConsole(WebDriver driver) {
        LogEntries jserrors = driver.manage().logs().get(LogType.BROWSER);
        for (LogEntry error : jserrors) {
            System.out.println(error.getMessage());
        }
    }
     
    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.chrome.driver", 
        		"/usr/local/Cellar/chromedriver/2.29/bin/chromedriver");
        System.setProperty("webdriver.chrome.logfile", "/Users/thao786/log");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
         
		// Set logging preference In Google Chrome browser capability to log
		// browser errors.
		LoggingPreferences pref = new LoggingPreferences();
		pref.enable(LogType.BROWSER, Level.ALL);
		cap.setCapability(CapabilityType.LOGGING_PREFS, pref);
       
        WebDriver driver = new ChromeDriver(cap);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        driver.manage().window().setSize(new Dimension(1000, 700));
               
	        driver.get("http://test-content.rumie.org/search");	         
	        System.out.println("Page title is: " + driver.getTitle());
	        
	        Boolean return_value = (Boolean)jse.executeScript("return $('.colblock').length >= 30");
	        System.out.println(return_value);
	        

        Thread.sleep(4000);
        ChromeExample.getConsole(driver);
        
        
//        driver.quit();
    }
}