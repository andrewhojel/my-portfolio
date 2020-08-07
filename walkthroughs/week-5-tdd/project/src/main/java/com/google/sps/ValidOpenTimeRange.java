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
 * ValidOpenTimeRange class contains a single time range (meant to be an open slot) and
 * a boolean keeping track of whether it meets the meeting request minimum duration
 */
public final class ValidOpenTimeRange {
  private final TimeRange openTime;
  private final boolean longEnough;

  /**
   * Constructs a valid open time range object
   *
   * @param openTime    A single time range (potential open slot)
   * @param longEnough  Whether the duration of the time range meets meeting request requirements
   */
  public ValidOpenTimeRange(TimeRange openTime, boolean longEnough) {
      this.openTime = openTime;
      this.longEnough =longEnough;
  }
  
  /**
   * Getter function for openTimes
   */
  public TimeRange getOpenTime() {
      return openTime;
  }

  /**
   * Getter function for containsOpenTimes
   */
  public boolean checkDuration() {
      return longEnough;
  }
}
