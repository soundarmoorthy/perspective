/*
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
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2015-01-14 17:53:03 UTC)
 * on 2015-03-23 at 19:42:44 UTC 
 * Modify at your own risk.
 */

package com.flicq.tennis.appengine.model;

/**
 * Model definition for Shot.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the flicq. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class Shot extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String counter;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String k;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String x;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String y;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String z;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getCounter() {
    return counter;
  }

  /**
   * @param counter counter or {@code null} for none
   */
  public Shot setCounter(java.lang.String counter) {
    this.counter = counter;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public Shot setId(java.lang.String id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getK() {
    return k;
  }

  /**
   * @param k k or {@code null} for none
   */
  public Shot setK(java.lang.String k) {
    this.k = k;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getX() {
    return x;
  }

  /**
   * @param x x or {@code null} for none
   */
  public Shot setX(java.lang.String x) {
    this.x = x;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getY() {
    return y;
  }

  /**
   * @param y y or {@code null} for none
   */
  public Shot setY(java.lang.String y) {
    this.y = y;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getZ() {
    return z;
  }

  /**
   * @param z z or {@code null} for none
   */
  public Shot setZ(java.lang.String z) {
    this.z = z;
    return this;
  }

  @Override
  public Shot set(String fieldName, Object value) {
    return (Shot) super.set(fieldName, value);
  }

  @Override
  public Shot clone() {
    return (Shot) super.clone();
  }

}