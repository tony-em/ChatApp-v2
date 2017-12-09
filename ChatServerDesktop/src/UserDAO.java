import java.sql.SQLException;

public interface UserDAO {
    int addUser(String username, String password, String email) throws SQLException;

    User validateUser(String username, String password, String email) throws SQLException;
}
