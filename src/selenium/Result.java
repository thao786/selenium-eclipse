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
		if (step > 0)
			this.step_id = step;
		this.runId = runId;
		this.error = error;
	}
	
	public void sync() throws Exception {
		Connection connection = (Connection) DriverManager
				.getConnection(config.url(), config.login(), config.password());
		PreparedStatement insertStm = connection
				.prepareStatement("INSERT INTO results (test_id, runID, error, step_id)" +
				"VALUES (?, ?, ?, ?);");
		insertStm.setInt(1, test_id);
		insertStm.setString(2, runId);
		insertStm.setString(3, error);
		if (step_id > 0)
			insertStm.setInt(4, step_id);
		insertStm.executeUpdate();
	}
}
