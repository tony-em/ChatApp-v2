import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class HelperDB {

    private HelperDB() {
    }

    private static HelperDB helperDB;

    public static HelperDB getInstance() {
        if (helperDB != null)
            return helperDB;
        else
            return new HelperDB();
    }

    private Connection connection;
    private Statement statement;

    public void initDriver(String driver) throws ClassNotFoundException {
        Class.forName(driver);
    }

    public boolean openConnection(String url, String username, String password) throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
        return (connection != null && !connection.isClosed());
    }

    public void closeConnection() throws SQLException {
        if (statement != null && !statement.isClosed())
            closeStatement();
        connection.close();
        connection = null;
    }

    public Statement getStatement() throws SQLException {
        if (statement != null && !statement.isClosed())
            closeStatement();

        statement = connection.createStatement();
        return statement;
    }

    public void closeStatement() throws SQLException {
        statement.close();
        statement = null;
    }
}