package selenium;

import java.io.File;
import java.io.IOException;


public class AwsS3 {
	private static String bucketName     = "autotest-test";
	private static String keyName        = "note";
	private static String uploadFileName = "/Users/thao786/note";
	
	public static void main(String[] args) throws IOException {
		Runtime.getRuntime().exec("/usr/local/bin/aws s3 cp /Users/thao786/log s3://autotest-test/log");
    }
}