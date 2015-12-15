/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.examples.spring.batch.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

/**
 * Created by ceposta 
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
@Component
public class RestCamelRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet").bindingMode(RestBindingMode.json)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "User API")
                .apiProperty("api.version", "1.2.3")
                .apiProperty("api.description", "REST API for controlling the jobs")
                .apiProperty("cors", "true")

                // this doesn't seem to work correctly in camel-2.16.1 for camel-swagger-java
                // opened a jira here https://issues.apache.org/jira/browse/CAMEL-9425
                .apiProperty("base.path", "/camel")
                .apiProperty("schemas", "http")
                .apiProperty("host", "postamac.local")
        ;

        rest().produces("application/json")
                .get("/hello")
                .to("direct:foo");

        from("direct:foo").transform(constant("Yay"));
    }
}
