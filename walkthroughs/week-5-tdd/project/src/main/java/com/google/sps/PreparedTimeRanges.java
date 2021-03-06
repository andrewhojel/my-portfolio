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
 * PreparedTimeRanges class is used to store the open time slots after computation as well
 * as a boolean indicator to tell whether there are any available open times
 */
public final class PreparedTimeRanges {
  private final List<TimeRange> openTimes;
  private final Boolean containsOpenTimes;

  /**
   * Constructs a prepared time ranges object
   *
   * @param openTimes   A list of available times givent he current restrains
   */
  public PreparedTimeRanges(List<TimeRange> openTimes) {
      this.openTimes = openTimes;
      this.containsOpenTimes = !openTimes.isEmpty();
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
  public Boolean checkOpenTimes() {
      return containsOpenTimes;
  }
}
