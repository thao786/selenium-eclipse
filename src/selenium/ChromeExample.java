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
    	System.out.println("digested(hex):" + Autotest.md5("secret"));
    }
}