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

/**
 * OptimizedTimeRanges class is used to store the open time slots after computation as well
 * as a boolean indicator to tell whether there are any available open times and stores a 
 * calculation of the total duration of all the time slots (used for optimization / tie breaking)
 */
public final class OptimizedTimeRanges {
  private final List<TimeRange> openTimes;
  private final boolean containsOpenTimes;
  private final Integer totalDuration;

  /**
   * Constructs an optimized time range object
   *
   * @param openTimes   A list of available times givent he current restrains
   */
  public OptimizedTimeRanges(List<TimeRange> openTimes) {
      this.openTimes = openTimes;
      this.containsOpenTimes = !openTimes.isEmpty();
      this.totalDuration = getDuration(openTimes);
  }
  
  /**
   * Getter function for openTimes
   */
  public List<TimeRange> getOpenTimes() {
      return openTimes;
  }

  /**
   * Getter function for containsOpenTimes
   */
  public boolean checkOpenTimes() {
      return containsOpenTimes;
  }

  /**
   * Getter function for totalDuration
   */
  public Integer getTotalDuration() {
      return totalDuration;
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
}
