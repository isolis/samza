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
package org.apache.samza.rest.model.yarn;

import java.util.HashMap;
import java.util.Map;
import org.apache.samza.rest.proxy.job.JobInstance;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import java.util.List;
import org.codehaus.jackson.map.annotate.JsonRootName;


@JsonRootName("apps")
public class YarnApplicationInfo {
  @JsonProperty("app")
  private List<YarnApplication> apps;

  public YarnApplicationInfo() {
  }

  public YarnApplicationInfo(List<YarnApplication> apps) {
    this.apps = apps;
  }

  /**
   * Returns a Map with all the apps and their names as the key.
   */
  public Map<String, YarnApplication> getApplications() {
    Map<String, YarnApplication> applications = new HashMap<>();
    for (YarnApplication app: this.apps) {
      applications.put(app.getName(), app);
    }
    return applications;
  }

  /**
   * Constructs the job name used in YARN. This is the value returned by the "name"
   * attribute form the Resource Manager API /ws/v1/cluster/apps.
   *
   * @param jobInstance the instance of the job.
   * @return the job name to use for the job in YARN.
   */
  public static String getQualifiedJobName(JobInstance jobInstance) {
    final String JOB_NAME_ID_FORMAT = "%s_%s";
    return String.format(JOB_NAME_ID_FORMAT, jobInstance.getJobName(), jobInstance.getJobId());
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class YarnApplication {
    private String state;
    private String name;

    public YarnApplication() {
    }

    public YarnApplication(String state, String name) {
      this.state = state;
      this.name = name;
    }

    public String getState() {
      return state;
    }

    public String getName() {
      return name;
    }
  }
}
