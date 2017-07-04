package selenium;

public class config {
	public static String getEvn() {
		return "dev";
	}
	
	public static String home = "/Users/thao786/";
	public static String bucket = "autotest-test";
	
	public static String convertPath() {
		return "/opt/local/bin/convert";
	}
	
	public static String picDir() {
		return "~/Pictures/selenium/";
	}
	
	public static String url() {
		return "jdbc:mysql://localhost:3306/autotest?user=root&password=root";
	}
	
	public static String login() {
		return "root";
	}
	
	public static String password() {
		return "root";
	}

	public static String awsPath() {
		return "/usr/local/bin/aws";
	}
}