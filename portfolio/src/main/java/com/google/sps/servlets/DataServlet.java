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

import java.io.PrintWriter;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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

  // Some libraries can only be run when deployed
  private final boolean dev = true;

  /**
   * Get comments from Database (correct number, sorting, & translation using query strings)
   * @param request     contains info on number of comments, sorting, & translation
   * @param response    returns comment entities to the client 
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Check if user is logged in / store email
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
        PrintWriter out = response.getWriter();
        out.println("<h1>Restricted Action!</h1>");
        return;
    }
    
    int numComments = Integer.parseInt(request.getParameter("count"));
    String sortingOrder = request.getParameter("sort");
    String langCode = request.getParameter("lang");
    Query query = prepareQuery(sortingOrder);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(numComments));
    
    // Get Translation instance for future usage
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    // Populate array with data from the DB
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
        String email = (String) entity.getProperty("email");
        long id = entity.getKey().getId();
        String name = (String) entity.getProperty("name");
        String comment_content = (String) entity.getProperty("comment");
        long timestamp = (long) entity.getProperty("timestamp");
        long comment_length = (long) entity.getProperty("length");

        // Translate the comment content
        comment_content = dev ? comment_content : translate(translate, comment_content, langCode);

        Comment comment = new Comment(id, name, comment_content, timestamp, comment_length, email);
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
   * @param request     contains all the information about the comment
   * @param reponnse    sends redirect to #Comments page to render comments
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Check if user is logged in / store email
      UserService userService = UserServiceFactory.getUserService();
      if (!userService.isUserLoggedIn()) {
          PrintWriter out = response.getWriter();
          out.println("<h1>Restricted Action!</h1>");
          return;
      }
      String email = userService.getCurrentUser().getEmail();
      
      // Get data from request
      String name = request.getParameter("comment_name");
      String comment =  request.getParameter("comment_content");

      // Add to Datastore
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("email", email);
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
   * @param arr an array of comments that will be converted to JSON
   */
  private String convertToJson(List<Comment> arr) {
    Gson gson = new Gson();
    String json = gson.toJson(arr);
    return json;
  }

  /**
   * Translates the comments into the desired language
   * @param translate   the translation object (used to call translation API function)
   * @param comment     a comment that needs to be translate
   * @param langCode    the language code of the target language 
   */
  private String translate(Translate translate, String comment, String langCode) {
    Translation translation =
        translate.translate(comment, Translate.TranslateOption.targetLanguage(langCode));
    return translation.getTranslatedText();
  }

  /**
   * Prepares a query with the user-inputed sorting order
   * @param sortingOrder    the order that the comments will be sorted in
   */
  private Query prepareQuery(String sortingOrder) {
      // Set to the default case
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
