package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import com.sun.org.apache.xpath.internal.operations.Bool;
import model.User;
import utils.Config;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. FIX (SHA)
    // Password hashed with md5
    String hashedPassw = Hashing.sha(user.getPassword());
    user.setPassword(hashedPassw);
    
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + user.getPassword()
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }


  public static boolean updateUser (User user){
    Log.writeLog(UserController.class.getName(), user, "Updating", 0);

    if (dbCon == null){
      dbCon = new DatabaseController();
    }


    String newPassword = Hashing.sha(user.getPassword());
    user.setPassword(newPassword);

    boolean affected = dbCon.update("UPDATE USER SET first_name = " + "\'" + user.getFirstname() + "\',"
    + "  last_name = "+ "\'" + user.getLastname() + "\'," + " password = " + "\'" + user.getPassword() + "\'," +
            " email = " + "\'" + user.getEmail() + "\'" + "WHERE id = " + "\'" + user.getId() + "\'");

    return affected;
  }

  public static User authenticateUser (User user) {

    int id = 0;

    if (dbCon == null){
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where email = " + "\'" + user.getEmail()+ "\'" +
            "AND password = " + "\'" + Hashing.sha(user.getPassword()) + "\'";

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User foundUser = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        foundUser =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // return the create object
        return foundUser;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return foundUser;

  }


  public static Boolean deleteUser (int userId){

    if (dbCon == null){
      dbCon = new DatabaseController();
    }

    boolean affected = dbCon.update("DELETE FROM user WHERE id = " + userId );

    return affected;

  }

}
