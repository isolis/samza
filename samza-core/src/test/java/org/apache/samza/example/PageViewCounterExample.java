/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.samza.example;

import org.apache.samza.application.StreamApplication;
import org.apache.samza.config.Config;
import org.apache.samza.operators.KV;
import org.apache.samza.operators.MessageStream;
import org.apache.samza.operators.OutputStream;
import org.apache.samza.operators.StreamGraph;
import org.apache.samza.operators.functions.FoldLeftFunction;
import org.apache.samza.operators.triggers.Triggers;
import org.apache.samza.operators.windows.AccumulationMode;
import org.apache.samza.operators.windows.WindowPane;
import org.apache.samza.operators.windows.Windows;
import org.apache.samza.runtime.LocalApplicationRunner;
import org.apache.samza.serializers.JsonSerdeV2;
import org.apache.samza.serializers.KVSerde;
import org.apache.samza.serializers.StringSerde;
import org.apache.samza.util.CommandLine;

import java.time.Duration;
import java.util.function.Supplier;


/**
 * Example code to implement window-based counter
 */
public class PageViewCounterExample implements StreamApplication {

  @Override public void init(StreamGraph graph, Config config) {
    MessageStream<PageViewEvent> pageViewEvents =
        graph.getInputStream("pageViewEventStream", new JsonSerdeV2<>(PageViewEvent.class));
    OutputStream<KV<String, PageViewCount>> pageViewEventPerMemberStream =
        graph.getOutputStream("pageViewEventPerMemberStream",
            KVSerde.of(new StringSerde(), new JsonSerdeV2<>(PageViewCount.class)));

    Supplier<Integer> initialValue = () -> 0;
    FoldLeftFunction<PageViewEvent, Integer> foldLeftFn = (m, c) -> c + 1;
    pageViewEvents
        .window(Windows.keyedTumblingWindow(m -> m.memberId, Duration.ofSeconds(10), initialValue, foldLeftFn)
            .setEarlyTrigger(Triggers.repeat(Triggers.count(5)))
            .setAccumulationMode(AccumulationMode.DISCARDING))
        .map(windowPane -> KV.of(windowPane.getKey().getKey(), new PageViewCount(windowPane)))
        .sendTo(pageViewEventPerMemberStream);
  }

  // local execution mode
  public static void main(String[] args) {
    CommandLine cmdLine = new CommandLine();
    Config config = cmdLine.loadConfig(cmdLine.parser().parse(args));
    LocalApplicationRunner localRunner = new LocalApplicationRunner(config);
    localRunner.run(new PageViewCounterExample());
  }

  class PageViewEvent {
    String pageId;
    String memberId;
    long timestamp;

    PageViewEvent(String pageId, String memberId, long timestamp) {
      this.pageId = pageId;
      this.memberId = memberId;
      this.timestamp = timestamp;
    }
  }

  class PageViewCount {
    String memberId;
    long timestamp;
    int count;

    PageViewCount(WindowPane<String, Integer> m) {
      this.memberId = m.getKey().getKey();
      this.timestamp = Long.valueOf(m.getKey().getPaneId());
      this.count = m.getMessage();
    }
  }
}
