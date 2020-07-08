// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

/** Servlet that deals with comments (POST and GET requests) */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  /**
  * Get comments from Database (correct number and sorting using query strings) 
  */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numComments = Integer.parseInt(request.getParameter("count"));
    String sortingOrder = request.getParameter("sort");
    Query query = prepareQuery(sortingOrder);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(numComments));
    

    // populate array with data from the DB
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
        long id = entity.getKey().getId();
        String name = (String) entity.getProperty("name");
        String comment_content = (String) entity.getProperty("comment");
        long timestamp = (long) entity.getProperty("timestamp");
        long comment_length = (long) entity.getProperty("length");

        Comment comment = new Comment(id, name, comment_content, timestamp, comment_length);
        comments.add(comment);
    }

    // Convert comments into readable data type for client
    String json = convertToJson(comments); 
    
    // Send response to client
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /**
  * Put new comment in the Database with data from POST
  */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Get data from request
      String name = request.getParameter("comment_name");
      String comment =  request.getParameter("comment_content");

      // Add to Datastore
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("name", name);
      commentEntity.setProperty("comment", comment);
      commentEntity.setProperty("timestamp", System.currentTimeMillis());
      commentEntity.setProperty("length", comment.length());

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);

      response.sendRedirect("/index.html#Comments");
  }

  /**
   * Converts the List of Comment objects to JSON using Gson Java library
   */
  private String convertToJson(List<Comment> arr) {
    Gson gson = new Gson();
    String json = gson.toJson(arr);
    return json;
  }

/**
 * Prepares a query with the user-inputed sorting order
 */
  private Query prepareQuery(String sortingOrder) {
      // set to the default case
      Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

      // Given user input, format the query
      switch (sortingOrder) {
          case "timestamp_descending":
            break;
          case "timestamp_ascending":
            query = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);
            break;
          case "user_descending":
            query = new Query("Comment").addSort("name", SortDirection.ASCENDING);
            break;
          case "user_ascending":
            query = new Query("Comment").addSort("name", SortDirection.DESCENDING);
            break;
          case "length_descending":
            query = new Query("Comment").addSort("length", SortDirection.DESCENDING);
            break;
          case "length_ascending":
            query = new Query("Comment").addSort("length", SortDirection.ASCENDING);
            break;
      }

      return query;
  }
}
