package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import controllers.UserController;

import java.util.ArrayList;
import java.util.Date;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import model.User;
import utils.Config;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  private static UserCache userCache = new UserCache();
  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON FIX
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);

    // Encrypted the json file with XOR method from utils
    json = Encryption.encryptDecryptXOR(json);

    // TODO: What should happen if something breaks down? FIX

    if (!json.isEmpty()){
      // Return the user with the status code 200
      return Response.status(404).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    }

    else {
      return Response.status(400).build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON FIX
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    // Encrypted the json file with XOR method from utils
    //json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system. fix
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {

    User user = new Gson().fromJson(x, User.class);

    User loginUser = null;

    loginUser = UserController.authenticateUser(user);

    if (loginUser != null) {

      Algorithm algorithm = Algorithm.HMAC256(Config.getTOKENKEY());
      String token = JWT.create()
              .withIssuer("auth0")
              .withIssuedAt(new Date(System.currentTimeMillis()))
              .withExpiresAt(new Date (System.currentTimeMillis() + 900000))
              .withSubject(Integer.toString(loginUser.getId()))
              .sign(algorithm);

      loginUser.setToken(token);

      String json = new Gson().toJson(loginUser.getToken());

      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("You're logged in. Your token is: " + json).build();
    }
      else {

        return Response.status(401).entity("Unathorized access").build();
      }
    }

  // TODO: Make the system able to delete users fix
  @DELETE
  @Path("/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(String x) {

    User choosenUser = new Gson().fromJson(x, User.class);

    System.out.println(choosenUser.getId());

    if (verifyToken(choosenUser.getToken(), choosenUser)) {

      boolean affected = UserController.deleteUser(choosenUser.getId());

      if (affected){
        userCache.getUsers(true);
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(choosenUser.getId() + " er nu slettet").build();
      }
      else {
        return Response.status(400).entity("Something must have gone wrong").build();
      }
    }
    else{
      return Response.status(401).entity("Unathorized access").build();
    }

    }

  // TODO: Make the system able to update users fix
  @PUT
  @Path("update/{idUser}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("idUser") int idUser, String body) {

    User choosenUser = new Gson().fromJson(body, User.class);


    if(verifyToken(choosenUser.getToken(), choosenUser)) {
      boolean affected = UserController.updateUser(choosenUser);
      if (affected) {
        String json = new Gson().toJson(choosenUser);
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
      } else {
        return Response.status(400).entity("Could not update user").build();
      }
    }
    else {
      return Response.status(401).entity("Unathorized access").build();
    }
  }

  private boolean verifyToken (String token, User user){
    try {
      Algorithm algorithm = Algorithm.HMAC256(Config.getTOKENKEY());
      JWTVerifier verifier = JWT.require(algorithm)
                            .withIssuer("auth0")
                            .withSubject(Integer.toString(user.getId()))
                            .build();
      verifier.verify(token);
      return true;
    }
    catch (JWTVerificationException exception){
      return false;
    }
  }

}

