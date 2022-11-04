/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.testing.http.javanet;

import com.google.api.client.testing.http.HttpTesting;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/** Tests {@link MockHttpURLConnection}. */
public class MockHttpUrlConnectionTest extends TestCase {

  private static final String HEADER_NAME = "Custom-Header";

  public void testSetGetHeaders() throws IOException {
    MockHttpURLConnection connection =
        MockHttpURLConnection.createSuccessful(new URL(HttpTesting.SIMPLE_URL), 200, null);
    connection.addHeader(HEADER_NAME, "100");
    assertEquals("100", connection.getHeaderField(HEADER_NAME));
  }

  public void testSetGetMultipleHeaders() throws IOException {
    MockHttpURLConnection connection =
        MockHttpURLConnection.createSuccessful(new URL(HttpTesting.SIMPLE_URL), 200, null);
    List<String> values = Arrays.asList("value1", "value2", "value3");
    for (String value : values) {
      connection.addHeader(HEADER_NAME, value);
    }
    Map<String, List<String>> headers = connection.getHeaderFields();
    assertEquals(3, headers.get(HEADER_NAME).size());
    for (int i = 0; i < 3; i++) {
      assertEquals(values.get(i), headers.get(HEADER_NAME).get(i));
    }
  }

  public void testGetNonExistingHeader() throws IOException {
    MockHttpURLConnection connection =
        MockHttpURLConnection.createSuccessful(new URL(HttpTesting.SIMPLE_URL), 200, null);
    assertNull(connection.getHeaderField(HEADER_NAME));
  }
}
