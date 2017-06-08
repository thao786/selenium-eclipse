package selenium;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class Result {
	public int test_id, step_id;
	public String runId, error;
	
	public Result(int test, int step, String runId, String error) {
		this.test_id = test;
		this.step_id = step;
		this.runId = runId;
		this.error = error;
	}
	
	public void sync() throws Exception {
		Connection connection = (Connection) DriverManager
				.getConnection(config.url(), config.login(), config.password());
		PreparedStatement insertStm = connection
				.prepareStatement("INSERT INTO results (test_id, runID, step_id, error)" +
				"VALUES (?, ?, ?, ?);");
		insertStm.setInt(1, test_id);
		insertStm.setString(2, runId);
		insertStm.setInt(3, 8);
		insertStm.setString(4, "jhgfrde");
		insertStm.executeUpdate();
	}
}
