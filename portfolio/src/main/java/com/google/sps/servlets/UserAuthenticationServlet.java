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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.User;
import com.google.gson.Gson;


@WebServlet("/auth")
public class UserAuthenticationServlet extends HttpServlet {

  /**
   * Generates a JSON object with user information
   * @param request     not used (do not need any info from client)
   * @param response    sends JSON object to client
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    UserService userService = UserServiceFactory.getUserService();
    String email, loginURL, logoutURL, nickname;
    boolean loggedIn;
    if (userService.isUserLoggedIn()) {
      loggedIn = true;
      email = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/index.html";
      logoutURL = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      loginURL = "";

      // Get user nickname or have them input it
      nickname = getUserNickname(userService.getCurrentUser().getUserId());
      if (nickname == null) { nickname = ""; } 
    } else {
      email = "";
      logoutURL = "";
      loggedIn = false;
      String urlToRedirectToAfterUserLogsIn = "/#Comments";
      loginURL = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      nickname = "";
    }
    User currentUser = new User(loggedIn, loginURL, logoutURL, email, nickname);

    String json = convertToJson(currentUser); 
    response.getWriter().println(json);
  }

  /**
   * Stores the authenticated user's nickname
   * @param request     captures user's id and new nickname
   * @param response    redirects page after POST    
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/auth");
      return;
    }

    String nickname = request.getParameter("nickname");
    String id = userService.getCurrentUser().getUserId();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity("UserInfo", id);
    entity.setProperty("id", id);
    entity.setProperty("nickname", nickname);
    // The put() function automatically inserts new data or updates existing data based on ID
    datastore.put(entity);

    response.sendRedirect("/#Comments");
  }

  /**
   * Converts the List of Comment objects to JSON using Gson Java library
   * @param user    User object that contains all user info
   */
  private String convertToJson(User user) {
    Gson gson = new Gson();
    String json = gson.toJson(user);
    return json;
  }

  /** 
   * Returns the nickname of the user with id, or null if the user has not set a nickname. 
   * @param id  the unique id of the authenticated user
   */
  private String getUserNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }
}