package com.cbsexam;

import cache.OrderCache;
import com.google.gson.Gson;
import controllers.LineItemController;
import controllers.OrderController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.LineItem;
import model.Order;
import utils.Encryption;

@Path("order")
public class OrderEndpoints {

  private static OrderCache orderCache = new OrderCache();
  private static boolean forceupdate = false;

  /**
   * @param idOrder
   * @return Responses
   */
  @GET
  @Path("/{idOrder}")
  public Response getOrder(@PathParam("idOrder") int idOrder) {

    // Call our controller-layer in order to get the order from the DB
    Order order = OrderController.getOrder(idOrder);

    ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(idOrder);
    order.setLineItems(lineItems);

    // TODO: Add Encryption to JSON FIX
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(order);

    // Encrypted the json file with XOR method from utils
    json = Encryption.encryptDecryptXOR(json);

    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getOrders() {

    // Call our controller-layer in order to get the order from the DB
    ArrayList<Order> orders = orderCache.getOrders(forceupdate);
    forceupdate = false;

    // TODO: Add Encryption to JSON FIX
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(orders);

    // Encrypted the json file with XOR method from utils
    json = Encryption.encryptDecryptXOR(json);

    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createOrder(String body) {

    // Read the json from body and transfer it to a order class
    Order newOrder = new Gson().fromJson(body, Order.class);

    // Use the controller to add the user
    Order createdOrder = OrderController.createOrder(newOrder);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createdOrder);

    // Encrypts our json file with the encryptDecryptXOR method from utils
    json = Encryption.encryptDecryptXOR(json);

    forceupdate = true;

    // Return the data to the user
    if (createdOrder != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {

      // Return a response with status 400 and a message in text
      return Response.status(400).entity("Could not create user").build();
    }
  }
}