/*
 * Copyright (C) 2019 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spotify.zoltar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Default {@link Predictor} {@link ScheduledExecutorService} supplier implementation for timeout
 * scheduling.
 */
final class DefaultPredictorTimeoutScheduler implements PredictorTimeoutScheduler {

  private static final ScheduledExecutorService SCHEDULER =
      Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

  private DefaultPredictorTimeoutScheduler() {}

  public static DefaultPredictorTimeoutScheduler create() {
    return new DefaultPredictorTimeoutScheduler();
  }

  @Override
  public ScheduledExecutorService scheduler() {
    return SCHEDULER;
  }
}
