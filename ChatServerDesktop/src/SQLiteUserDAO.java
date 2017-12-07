import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteUserDAO implements UserDAO {

    private HelperDB helperDB;

    public SQLiteUserDAO(HelperDB helperDB) {
        this.helperDB = helperDB;
    }

    @Override
    public int addUser(String username, String password, String email) throws SQLException {
        Statement s = open();
        if (s != null) {
            s.execute(String.format("INSERT INTO USERS (NICKNAME, PASSWORD, EMAIL) VALUES ('%s', '%s', '%s');", username, password, email));
            helperDB.closeConnection();
            return 0;
        }

        return -1;
    }

    @Override
    public User validateUser(String username, String password, String email) throws SQLException {
        Statement s = open();
        if (s != null) {
            ResultSet data = s.executeQuery(String.format("SELECT * FROM USERS WHERE (NICKNAME='%s' AND PASSWORD='%s' AND EMAIL='%s');", username, password, email));
            if (data.next()) {
                System.out.println(data.getInt("ID") + data.getString("NICKNAME") + data.getString("PASSWORD"));
                return new User(data.getInt("ID"),
                        data.getString("NICKNAME"),
                        data.getString("PASSWORD"),
                        data.getString("EMAIL"));
            }
        }

        return null;
    }

    private Statement open() {
        try {
            helperDB.openConnection(ChatServer.JDBC_URL, ChatServer.DB_USERNAME, ChatServer.DB_PASSWORD);
            return helperDB.getStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
