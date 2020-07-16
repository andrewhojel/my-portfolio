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

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import com.google.common.collect.Sets; 
import java.io.*;
import org.javatuples.Pair;
import org.javatuples.Triplet;

public final class FindMeetingQuery {

  // Define start of day and end of day Events
  private final Collection<String> attendeesHolder = new HashSet<>();
  private final Event startOfDay = new Event("START", TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0), attendeesHolder); 
  private final Event endOfDay = new Event ("END", TimeRange.fromStartDuration(TimeRange.END_OF_DAY + 1, 0), attendeesHolder);

  /**
   * Helper function to run a standard query (time slots for all req and optional if possible)
   *
   * @param events A collection of all the Event objects
   * @param request The MeetingRequest for the meeting to be schedules
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      return internalQuery(events, request, false);
  }

  /**
   * Helper function to run an optimized query (time slots for all req and as many opt as possible)
   *
   * @param events A collection of all the Event objects
   * @param request The MeetingRequest for the meeting to be schedules
   */
  public Collection<TimeRange> optimalQuery(Collection<Event> events, MeetingRequest request) {
      return internalQuery(events, request, true);
  }

  /**
   * Function that finds time slots for Meeting Request (can run both standard and optimized)
   *
   * @param events          A collection of all the Event objects
   * @param request         The MeetingRequest for the meeting to be schedules
   * @param optimalQuery    Determines whether it is a standard or optimal query 
   */
  private Collection<TimeRange> internalQuery(Collection<Event> events, MeetingRequest request, boolean optimalQuery) {
    // Create set of required attendees
    Collection<String> requiredAttendees = request.getAttendees();
    Set<String> requiredAttendeesSet = new HashSet<String>(requiredAttendees);

    // Create set of optional attendees
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    Set<String> optionalAttendeesSet = new HashSet<String>(optionalAttendees);

    // Create a set of required + optional attendees (union of both sets)
    Set<String> comboAttendees = Sets.union(requiredAttendeesSet, optionalAttendeesSet);

    // Find open times for all optional + requiered attendes 
    PreparedTimeRange comboOpen = checkCompatibility(events, request, comboAttendees); 
    // if required is empty and it is an optimal query want to maximize optional attendees
    Boolean ignoreRequired = requiredAttendees.isEmpty() && !optimalQuery;
    // if optional or required is empty -> we have the open times for the other (both can be empty)
    if (ignoreRequired || optionalAttendees.isEmpty() || comboOpen.checkOpenTimes()) { 
        return comboOpen.getOpenTimes(); 
    }    

    // Find open times for all required attendees
    List<TimeRange> reqOpenTimes = new ArrayList<TimeRange>();
    if (!requiredAttendees.isEmpty()) {
        reqOpenTimes = checkCompatibility(events, request, requiredAttendeesSet).getOpenTimes();
    }

    // Either return current results or run the optimized version to maximize optional attendees
    return optimalQuery ? optimalQueryHelper(events, request, reqOpenTimes) : reqOpenTimes; 
  }

  /**
   * Function that runs optimal query to maximize optional attendees in addition to required attendees
   *
   * @param events          A collection of all the Event objects
   * @param request         The MeetingRequest for the meeting to be schedules
   * @param reqOpenTimes    The open times for just required attendees
   */
  private List<TimeRange> optimalQueryHelper(Collection<Event> events, MeetingRequest request, List<TimeRange> reqOpenTimes) {
    // Prepare multiple variables for recursive function
    List<TimeRange> optimalSchedule = new ArrayList<TimeRange>();
    Set<String> ignoreAttendees = new HashSet<String>();

    // StaticInfo is used to pass info that doesn't change through the recursive function
    StaticRecursiveInfo staticInfo = new StaticRecursiveInfo(events, request); 

    // Generate subsets of optional Attendees of increasing size (one below all b/c that was already tested)
    for (int i = 1 ; i < staticInfo.getoptionalAttendees().size(); i++) {
        Triplet<Boolean, Integer, List<TimeRange>> optimalCandidate = callRecursiveOptimalQuery(ignoreAttendees, staticInfo, i);
        if (optimalCandidate.getValue0()) {
            optimalSchedule = optimalCandidate.getValue2();
        } else if (i != 1) { // if i == 1 then no open times with optional attendees was found
            return optimalSchedule;
        } else {
            return reqOpenTimes;
        }
    }
    return optimalSchedule;
  }

  /**
   * Helper function to call recursiveOptimalQuery (fills in fixed inputs)
   *
   * @param ignoreAttendees If no open times are found for an attendee or group of attendee we ignore them
   * @param staticInfo      Contains all the information that doesn't change throughout recursive calls 
   * @param numAttendees    The size of the subset of optional attendees
   */
  private Triplet<Boolean, Integer, List<TimeRange>> callRecursiveOptimalQuery(Set<String> ignoreAttendees, StaticRecursiveInfo staticInfo, Integer numAttendees) {
      List<String> chosenAttendees = new ArrayList<String>();
      return recursiveOptimalQuery(ignoreAttendees, staticInfo, numAttendees, chosenAttendees, 0, 0);
  }

  /**
   * Uses recursive backtracking to find the longest list of open times that maximizes attendance
   * of optional attendees and includes all required attendees
   *
   * @param ignoreAttendees If no open times are found for an attendee or group of attendee we ignore them
   * @param staticInfo      Contains all the information that doesn't change throughout recursive calls 
   * @param numAttendees    The size of the subset of optional attendees
   * @param chosenAttendees Current attendees in the subset 
   * @param attendeeIndex   Index in array of optional attendees (found within the StaticInfo object)
   * @param chosenIndex     Index in array of chosen attendees
   */
  private Triplet<Boolean, Integer, List<TimeRange>> recursiveOptimalQuery(Set<String> ignoreAttendees, StaticRecursiveInfo staticInfo, Integer numAttendees, List<String> chosenAttendees, Integer attendeeIndex, Integer chosenIndex) {
      // Subset is of size numAttendees (Base Case)
      if (chosenIndex == numAttendees) {
        // Prepare and union sets to create set of all required attendees and optional attendees in current subset
        Set<String> requiredAttendeesSet = new HashSet<String>(staticInfo.getrequiredAttendees());
        Set<String> optionalAttendeesSet = new HashSet<String>(chosenAttendees);
        Set<String> comboAttendees = Sets.union(requiredAttendeesSet, optionalAttendeesSet);

        // Skip call to checkCompatibility() if one of the attendees is in the ignoreAttendees set
        PreparedTimeRange openTimes;
        if (!Sets.intersection(ignoreAttendees, optionalAttendeesSet).isEmpty()) {
            openTimes = new PreparedTimeRange(new ArrayList<TimeRange>());
        } else {
            openTimes = checkCompatibility(staticInfo.getEvents(), staticInfo.getRequest(), comboAttendees);
        }
        
        if(openTimes.checkOpenTimes()) {
            return Triplet.with(true, getDuration(openTimes.getOpenTimes()) , openTimes.getOpenTimes());
        } else {
            ignoreAttendees.addAll(optionalAttendeesSet); // no open times -> ignore this set of optional attendees
            return Triplet.with(false, 0, openTimes.getOpenTimes());
        }
      }

      // Have no more optional attendees (Base Case)
      if (attendeeIndex == staticInfo.getoptionalAttendees().size()) {
          return Triplet.with(false, 0, new ArrayList<TimeRange>());
      }

      // Update chosenAttendees -> either add element or overwrite previous element
      String curAttendee = staticInfo.getoptionalAttendees().get(attendeeIndex);
      if (chosenIndex == chosenAttendees.size()) {
          chosenAttendees.add(curAttendee);
      } else {
          chosenAttendees.set(chosenIndex, curAttendee);
      } 

      // Resulting open times if we include curAttendee in the subset 
      Triplet<Boolean, Integer, List<TimeRange>> keep = recursiveOptimalQuery(ignoreAttendees, staticInfo, numAttendees, chosenAttendees, attendeeIndex + 1, chosenIndex + 1);

      // Resulting open times if we ignore cureAttendee in the subset
      Triplet<Boolean, Integer, List<TimeRange>> skip = recursiveOptimalQuery(ignoreAttendees, staticInfo, numAttendees, chosenAttendees, attendeeIndex + 1, chosenIndex);

      // If both keep and skip have open times -> choose the one with larger duration of open time
      if (keep.getValue0() && skip.getValue0()) {
          return keep.getValue1() > skip.getValue1() ? keep : skip;
      } else if (keep.getValue0()) {
          return keep;
      } else {
          return skip;
      }
  }
  
  /**
   * Calculates the duration of all the open time slots combines
   *
   * @param openTimes   List of TimeRange objects that represent open times
   */
  private Integer getDuration(List<TimeRange> openTimes) {
      Integer result = 0;
      for (TimeRange slot : openTimes) {
          result += slot.duration();
      }
      return result;
  }

  /**
   * Given a set of attendees returns the set of open times 
   *
   * @param events          A collection of all the Event objects
   * @param request         The MeetingRequest for the meeting to be schedules
   * @param attendees       Set of attendees to be considered when searching for open times
   */
  private PreparedTimeRange checkCompatibility(Collection<Event> events, MeetingRequest request, Set<String> attendees) {
        // Given set of attendees -> filter to include only relevant events
        List<Event> filteredEvents = new ArrayList<Event>();
        filteredEvents.add(startOfDay);
        filteredEvents.addAll(filterEvents(events, attendees));
        filteredEvents.add(endOfDay);

        long neededDif = request.getDuration();

        // Find open times for current list of filtered events
        List<TimeRange> openTimes = findOpenTimes(filteredEvents, neededDif);

        // Return pair of open times and whether or not there are any
        return new PreparedTimeRange(openTimes);
  }

  /**
   * Find open times given a set of relevant events (find gaps between events!)
   *
   * @param filteredEvents  A list of relevant events
   * @param neededDir       The necessary duration of an open slot 
   */
  private List<TimeRange> findOpenTimes(List<Event> filteredEvents, long neededDif) {
    // Sort the filtered events by TimeRange (start time)
    Collections.sort(filteredEvents, (event1, event2) -> TimeRange.ORDER_BY_START.compare(event1.getWhen(), event2.getWhen()));

    List<TimeRange> openTimes = new ArrayList<TimeRange>();
    int numEvents = filteredEvents.size();

    // Move through sorted events looking for large enough gap between consecutive events
    for (int i = 0; i < numEvents - 1; i++) {
        // Generate a new event for the second event taking into account overlap compensation (look at function for more info)
        String title = filteredEvents.get(i + 1).getTitle();
        TimeRange compensatedTimeRange = compensateOverlap(filteredEvents.get(i).getWhen(), filteredEvents.get(i+1).getWhen());
        Collection attendees = filteredEvents.get(i + 1).getAttendees();
        Event newTiming2 = new Event(title, compensatedTimeRange, attendees);

        // Update second event, calculate difference, then include if open slot is long enough
        filteredEvents.set(i + 1, newTiming2); 
        Pair<TimeRange, Boolean> dif = calculateEventDif(filteredEvents.get(i).getWhen(), filteredEvents.get(i+1).getWhen(), neededDif);
        if (dif.getValue1()) { openTimes.add(dif.getValue0()); }
    }

    return openTimes;
  }

  /**
   * Create new list of events (only those attendees by the provided attendees)
   *
   * @param events          A collection of all the Event objects
   * @param attendees       Set of relevant attendees (only care about their events)
   */
  private List<Event> filterEvents(Collection<Event> events, Collection<String> attendees) {
      List<Event> filteredEvents = new ArrayList<Event>();
      Set<String> requiredAttendees = new HashSet<String>(attendees);

      // Check if any attendee of interested attended given event -> if so add to filtered events
      for (Event event : events) {
        Set<String> eventAttendees = event.getAttendees();
        Set<String> intersection = Sets.intersection(eventAttendees, requiredAttendees);

        if (containsRequiredAttendees(intersection)) { filteredEvents.add(event); }
      }

      return filteredEvents;
  }

  /**
   * Event contains required attendees if the size of intersection > 0
   *
   * @param intersection    Intersection of attendees of interest and Event's attendees
   */
  private boolean containsRequiredAttendees(Set<String> intersection) {
      return intersection.size() > 0;
  }

  /**
   * Calculates the different between consecutive events and whether it is sufficiently long
   * to satisfy the meeting request.
   *
   * @param timing1     The TimeRange of the first event
   * @param timing2     The TimeRange of the second event
   * @param neededDif   The time gap necessary to schedule the request
   * 
   */
  private Pair<TimeRange, Boolean> calculateEventDif(TimeRange timing1, TimeRange timing2, long neededDif) {
      // Check for overlap btw ranges -> no time in between
      if (timing1.overlaps(timing2)) {
          return Pair.with(TimeRange.fromStartDuration(0,0), false);
      }

      // Return TimeRange of difference between events 
      int eventDif = timing2.start() - timing1.end();
      TimeRange betweenEvents = TimeRange.fromStartDuration(timing1.end(), eventDif);
      return Pair.with(betweenEvents, eventDif >= neededDif);
  }

  /**
   * Given the algorithm used to find time slots, if the second event 
   * ends before the first event, we need toe extend the second event
   * to the end of the first event to ensure we don't improperly assume
   * we have a timeslot when it would conflict with event 1
   *   
   *   |--------1-------|           |--------1-------|
   *                        ---> 
   *        |--2--|                      |-----2-----|
   *
   * @param timing1     The TimeRange of the first event
   * @param timing2     The TimeRange of the second event
   * 
   */
  private TimeRange compensateOverlap(TimeRange timing1, TimeRange timing2) {
      return timing1.contains(timing2) ? TimeRange.fromStartEnd(timing2.start(), timing1.end(), false) : timing2;
  }
}

