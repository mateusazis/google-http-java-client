/*
 * Copyright (c) 2012 Google Inc.
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

import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Beta} <br>
 * Mock for {@link HttpURLConnection}.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
@Beta
public final class MockHttpURLConnection extends HttpURLConnection {

  /** Whether {@link #doOutput} was called. */
  private boolean doOutputCalled;
  /** Whether {@link #setFixedLengthStreamingMode(int)} was called. */
  private boolean setFixedLengthStreamingModeIntCalled = false;
  /** Whether {@link #setFixedLengthStreamingMode(long)} was called. */
  private boolean setFixedLengthStreamingModeLongCalled = false;

  /**
   * Output stream or {@code null} to throw an {@link UnknownServiceException} when {@link
   * #getOutputStream()} is called.
   */
  private OutputStream outputStream = new ByteArrayOutputStream(0);

  /**
   * The input byte array which represents the content when the status code is less then {@code
   * 400}.
   *
   * @deprecated As of 1.20. Use {@link #setInputStream(InputStream)} instead.
   */
  @Deprecated public static final byte[] INPUT_BUF = new byte[1];

  /**
   * The error byte array which represents the content when the status code is greater or equal to
   * {@code 400}.
   *
   * @deprecated As of 1.20. Use {@link #setErrorStream(InputStream)} instead.
   */
  @Deprecated public static final byte[] ERROR_BUF = new byte[5];

  /** The input stream. */
  private final InputStream inputStream;

  /** The error stream. */
  private final InputStream errorStream;

  private boolean connected = false;

  private final IOException responseCodeError;

  private Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

  /** @param u the URL or {@code null} for none */
  private MockHttpURLConnection(
      URL u,
      IOException responseCodeError,
      int responseCode,
      InputStream inputStream,
      InputStream errorStream) {
    super(u);
    this.responseCodeError = responseCodeError;
    this.responseCode = responseCode;
    this.inputStream = inputStream;
    this.errorStream = errorStream;
  }

  public static MockHttpURLConnection createHanging(URL u) {
    return new MockHttpURLConnection(
        u, null, /* responseCode= */ -1, /* inputStream= */ null, /* errorStream= */ null);
  }

  public static MockHttpURLConnection createWithResponseCodeError(
      URL u, IOException responseCodeError) {
    return new MockHttpURLConnection(
        u,
        responseCodeError,
        /* responseCode= */ -1,
        /* inputStream= */ null,
        /* errorStream= */ null);
  }

  public static MockHttpURLConnection createSuccessful(
      URL u, int responseCode, InputStream inputStream) {
    if (responseCode > 399) {
      throw new IllegalArgumentException(
          "Response code must be at most 399, but was: " + responseCode);
    }
    return new MockHttpURLConnection(u, null, responseCode, inputStream, /* errorStream= */ null);
  }

  public static MockHttpURLConnection createFailed(
      URL u, int responseCode, InputStream errorStream) {
    if (responseCode < 400) {
      throw new IllegalArgumentException(
          "Response code must be 400 or higher, but was: " + responseCode);
    }
    return new MockHttpURLConnection(u, null, responseCode, /* inputStream= */ null, errorStream);
  }

  @Override
  public void disconnect() {
    connected = false;
  }

  @Override
  public boolean usingProxy() {
    return false;
  }

  @Override
  public void connect() throws IOException {
    connected = true;
  }

  @Override
  public int getResponseCode() throws IOException {
    if (responseCodeError != null) {
      throw responseCodeError;
    }
    return responseCode;
  }

  @Override
  public void setDoOutput(boolean dooutput) {
    doOutputCalled = true;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (outputStream != null) {
      return outputStream;
    }
    return super.getOutputStream();
  }

  /** Returns whether {@link #doOutput} was called. */
  public final boolean doOutputCalled() {
    return doOutputCalled;
  }

  /**
   * Sets the output stream or {@code null} to throw an {@link UnknownServiceException} when {@link
   * #getOutputStream()} is called.
   *
   * <p>By default it is {@code null}.
   */
  public MockHttpURLConnection setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  /**
   * Sets a custom response header.
   *
   * @since 1.20
   */
  public MockHttpURLConnection addHeader(String name, String value) {
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(value);
    if (headers.containsKey(name)) {
      headers.get(name).add(value);
    } else {
      List<String> values = new ArrayList<String>();
      values.add(value);
      headers.put(name, values);
    }
    return this;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (responseCode < 400) {
      return inputStream;
    }
    throw new IOException();
  }

  @Override
  public InputStream getErrorStream() {
    return connected ? errorStream : null;
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    return headers;
  }

  @Override
  public String getHeaderField(String name) {
    List<String> values = headers.get(name);
    return values == null ? null : values.get(0);
  }

  public int getChunkLength() {
    return chunkLength;
  }

  @Override
  public void setFixedLengthStreamingMode(int contentLength) {
    this.setFixedLengthStreamingModeIntCalled = true;
    super.setFixedLengthStreamingMode(contentLength);
  }

  @Override
  public void setFixedLengthStreamingMode(long contentLength) {
    this.setFixedLengthStreamingModeLongCalled = true;
    super.setFixedLengthStreamingMode(contentLength);
  }

  public boolean isSetFixedLengthStreamingModeIntCalled() {
    return setFixedLengthStreamingModeIntCalled;
  }

  public boolean isSetFixedLengthStreamingModeLongCalled() {
    return setFixedLengthStreamingModeLongCalled;
  }
}
