/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
 * %%
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
 * #L%
 */
package io.wcm.tooling.commons.contentpackagebuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * XML namespaces supported by default.
 */
public final class XmlNamespaces {

  /**
   * JCR namespace
   */
  public static final String NS_JCR = "http://www.jcp.org/jcr/1.0";

  /**
   * JCR nodetype namespace
   */
  public static final String NS_JCR_NT = "http://www.jcp.org/jcr/nt/1.0";

  /**
   * JCR "rep" internal namespace
   */
  public static final String NS_JCR_REP = "internal";

  /**
   * CQ namespace
   */
  public static final String NS_CQ = "http://www.day.com/jcr/cq/1.0";

  /**
   * CRX namespace
   */
  public static final String NS_CRX = "http://www.day.com/crx/1.0";

  /**
   * DAM namespace
   */
  public static final String NS_DAM = "http://www.day.com/dam/1.0";

  /**
   * Sling namespace
   */
  public static final String NS_SLING = "http://sling.apache.org/jcr/sling/1.0";

  /**
   * Sling Event namespace
   */
  public static final String NS_SLING_EVENT = "http://sling.apache.org/jcr/event/1.0";

  /**
   * OAK namespace
   */
  public static final String NS_OAK = "http://jackrabbit.apache.org/oak/ns/1.0";

  /**
   * Granite namespace
   */
  public static final String NS_GRANITE = "http://www.adobe.com/jcr/granite/1.0";

  /**
   * wcm.io namespace
   */
  public static final String NS_WCMIO = "http://wcm.io/ns";

  /**
   * XML Namespaces support by default for JCR content XML.
   */
  public static final Map<String, String> DEFAULT_NAMESPACES;
  static {
    Map<String,String> ns = new HashMap<>();
    ns.put("jcr", NS_JCR);
    ns.put("nt", NS_JCR_NT);
    ns.put("rep", NS_JCR_REP);
    ns.put("cq", NS_CQ);
    ns.put("dam", NS_DAM);
    ns.put("crx", NS_CRX);
    ns.put("sling", NS_SLING);
    ns.put("slingevent", NS_SLING_EVENT);
    ns.put("oak", NS_OAK);
    ns.put("granite", NS_GRANITE);
    ns.put("wcmio", NS_WCMIO);
    DEFAULT_NAMESPACES = Collections.unmodifiableMap(ns);
  }

  private XmlNamespaces() {
    // constants only
  }

}
