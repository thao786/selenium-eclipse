package selenium;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
 
public class MultipleTabs  {
	public static void printTabs(List tabs) {
		int i;
		for (i = 0; i< tabs.size(); i++) {
			System.out.println(i + " " + tabs.get(i));
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
        driver.get("http://guides.rubyonrails.org/");
        System.out.println("Page title is: " + driver.getTitle());
        
        WebElement element = (WebElement) ((JavascriptExecutor)driver)
        		.executeScript("window.open('https://www.google.com');");
        
        TimeUnit.MILLISECONDS.sleep(1000);
        element = (WebElement) ((JavascriptExecutor)driver)
        		.executeScript("window.open('https://www.reddit.com');");
        element = (WebElement) ((JavascriptExecutor)driver)
        		.executeScript("window.open('');");
        
        // 0 rails, 1 google, 2 reddit, 3 dummy
        List tabs = new ArrayList(driver.getWindowHandles());
        driver.switchTo().window((String) tabs.get(1));
        for (int i = 0; i< tabs.size(); i++) {
        	driver.switchTo().window((String) tabs.get(i));
			System.out.println(i + " " + driver.getCurrentUrl());
		}
        
        TakesScreenshot scrShot =((TakesScreenshot)driver);
        File SrcFile=scrShot.getScreenshotAs(OutputType.FILE);
        File DestFile=new File("/Users/thao786/a.jpg");
        FileUtils.copyFile(SrcFile, DestFile);
                 
//        driver.quit();
    }
}