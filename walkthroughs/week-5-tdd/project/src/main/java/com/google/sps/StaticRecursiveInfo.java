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

package com.google.sps;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * StaticRecursiveInfo is a class used to store static information that is needed in the
 * recursive backtracking function for the optimal query --> allows for a decrease in
 * the number of parameters for clarity
 */
public final class StaticRecursiveInfo {
  private final Collection<Event> events;
  private final MeetingRequest request;
  private final List<String> requiredAttendees;
  private final List<String> optionalAttendees;

  /**
   * Constructs a static info object
   *
   * @param events A collection of all the Event objects
   * @param request The MeetingRequest for the meeting to be schedules
   */
  public StaticRecursiveInfo(Collection<Event> events, MeetingRequest request) {
      this.events = events;
      this.request = request;

      // Can build list of required and optional attendees from request
      this.requiredAttendees = new ArrayList<String>(request.getAttendees());
      this.optionalAttendees = new ArrayList<String>(request.getOptionalAttendees());
  }
  
  /**
   * Getter function for events
   */
  public Collection<Event> getEvents() {
      return events;
  }

  /**
   * Getter function for request
   */
  public MeetingRequest getRequest() {
      return request;
  }

  /**
   * Getter function for requiredAttendees
   */
  public List<String> getrequiredAttendees() {
      return requiredAttendees;
  }
  
  /**
   * Getter function for optionalAttendees
   */
  public List<String> getoptionalAttendees() {
      return optionalAttendees;
  }
}
