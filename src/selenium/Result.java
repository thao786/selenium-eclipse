package selenium;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class Result {
	public int test_id, step_id, assertion_id;
	public String runId, error, webpage = "";
	public String query = "";
	
	public static int STATUS_ASSERTION = 1;
	public static int REPORT_ASSERTION = 3;
	public static int STEP_SUCCESS_ASSERTION = 4;
	public static int URL_MATCH_ASSERTION = 5;
	
	public Result(int test, int step, String runId, String error, int assertion, String webpage) {
		this(test, step, runId, error, assertion);
		this.webpage = webpage;
	}
	
	public Result(int test, int step, String runId, String error, int assertion) {
		this.test_id = test;
		if (step > 0)
			this.step_id = step;
		this.runId = runId;
		this.error = error;
		this.assertion_id = assertion;
	}
	
	public void sync() throws Exception {
		query = "INSERT INTO results (test_id, runID, error, assertion_id, webpage)" +
				"VALUES (?, ?, ?, ?, ?);";
		if (step_id > 0)
			query = "INSERT INTO results (test_id, runID, error, assertion_id, webpage, step_id)" +
					"VALUES (?, ?, ?, ?, ?, ?);";
		
		Connection connection = (Connection) DriverManager
				.getConnection(config.url, config.login, config.password);
		PreparedStatement insertStm = connection.prepareStatement(query);
		insertStm.setInt(1, test_id);
		insertStm.setString(2, runId);
		insertStm.setString(3, error);
		insertStm.setInt(4, assertion_id);
		insertStm.setString(5, webpage);
		if (step_id > 0)
			insertStm.setInt(6, step_id);
		insertStm.executeUpdate();
	}
}
