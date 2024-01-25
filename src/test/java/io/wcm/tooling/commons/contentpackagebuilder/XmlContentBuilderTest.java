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

import static io.wcm.tooling.commons.contentpackagebuilder.XmlUnitUtil.assertXpathEvaluatesTo;
import static io.wcm.tooling.commons.contentpackagebuilder.XmlUnitUtil.assertXpathNotExists;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.apache.jackrabbit.util.ISO9075;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;
import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElementImpl;

@SuppressWarnings("java:S5778") // assert throws
class XmlContentBuilderTest {

  private XmlContentBuilder underTest;

  @BeforeEach
  void setUp() {
    underTest = new XmlContentBuilder(XmlNamespaces.DEFAULT_NAMESPACES);
  }

  @Test
  void testPageSimpleMap() throws Exception {
    Document doc = underTest.buildPage(Map.of(
        "var1", "v1",
        "var2", 55,
        "var3", new String[] {
            "v1", "v2", "v3"
        }));
    assertXpathEvaluatesTo("cq:Page", "/jcr:root/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("cq:PageContent", "/jcr:root/jcr:content/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v1", "/jcr:root/jcr:content/@var1", doc);
    assertXpathEvaluatesTo("{Long}55", "/jcr:root/jcr:content/@var2", doc);
    assertXpathEvaluatesTo("[v1,v2,v3]", "/jcr:root/jcr:content/@var3", doc);
  }

  @Test
  void testPageNestedMaps() throws Exception {
    Document doc = underTest.buildPage(Map.of(
        "var1", "v1",
        "var2", 55,
        "node1", Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "var3", "v3"),
        "node2", Map.of("var4", "v4",
            "node21", Map.of("var5", "v5"))
        ));

    assertXpathEvaluatesTo("cq:Page", "/jcr:root/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("cq:PageContent", "/jcr:root/jcr:content/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v1", "/jcr:root/jcr:content/@var1", doc);
    assertXpathEvaluatesTo("{Long}55", "/jcr:root/jcr:content/@var2", doc);

    assertXpathEvaluatesTo("myNodeType", "/jcr:root/jcr:content/node1/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v3", "/jcr:root/jcr:content/node1/@var3", doc);

    assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/jcr:content/node2/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v4", "/jcr:root/jcr:content/node2/@var4", doc);

    assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/jcr:content/node2/node21/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v5", "/jcr:root/jcr:content/node2/node21/@var5", doc);
  }

  @Test
  void testContentSimpleMap() throws Exception {
    Document doc = underTest.buildContent(Map.of(
        "jcr:primaryType", "myPrimaryType",
        "var1", "v1",
        "var2", 55,
        "var3", new String[] {
            "v1", "v2", "v3"
        },
        "granite:data", new String[]{
            "data-1", "data-2"
        }));
    assertXpathEvaluatesTo("myPrimaryType", "/jcr:root/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v1", "/jcr:root/@var1", doc);
    assertXpathEvaluatesTo("{Long}55", "/jcr:root/@var2", doc);
    assertXpathEvaluatesTo("[v1,v2,v3]", "/jcr:root/@var3", doc);
  }

  @Test
  void testContentWithSpecialElementNames() throws Exception {
    Document doc = underTest.buildContent(Map.of(
        "0abc", "v1",
        "abc#def", Map.of("prop1", "v2"),
        "äöäß€", 55));
    assertXpathEvaluatesTo("v1", "/jcr:root/@" + ISO9075.encode("0abc"), doc);
    assertXpathEvaluatesTo("v2", "/jcr:root/" + ISO9075.encode("abc#def") + "/@prop1", doc);
    assertXpathEvaluatesTo("{Long}55", "/jcr:root/@" + ISO9075.encode("äöäß€"), doc);
  }

  @Test
  void testContentNestedMaps() throws Exception {
    Document doc = underTest.buildContent(Map.of(
        "var1", "v1",
        "var2", 55,
        "node1", Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "var3", "v3"),
        "node2", Map.of("var4", "v4",
            "node21", Map.of("var5", "v5"))
        ));

    assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v1", "/jcr:root/@var1", doc);
    assertXpathEvaluatesTo("{Long}55", "/jcr:root/@var2", doc);

    assertXpathEvaluatesTo("myNodeType", "/jcr:root/node1/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v3", "/jcr:root/node1/@var3", doc);

    assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/node2/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v4", "/jcr:root/node2/@var4", doc);

    assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/node2/node21/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v5", "/jcr:root/node2/node21/@var5", doc);
  }

  @Test
  void testContentElementHierarchy() throws Exception {
    ContentElement root = new ContentElementImpl(null, Map.of(
        "var1", "v1",
        "var2", 55));
    ContentElement node1 = new ContentElementImpl("node1", Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "var3", "v3"));
    ContentElement node2 = new ContentElementImpl("node2", Map.of("var4", "v4"));
    ContentElement node21 = new ContentElementImpl("node21", Map.of("var5", "v5"));
    root.getChildren().put("node1", node1);
    root.getChildren().put("node2", node2);
    node2.getChildren().put("node21", node21);
    Document doc = underTest.buildContent(root);

    assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v1", "/jcr:root/@var1", doc);
    assertXpathEvaluatesTo("{Long}55", "/jcr:root/@var2", doc);

    assertXpathEvaluatesTo("myNodeType", "/jcr:root/node1/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v3", "/jcr:root/node1/@var3", doc);

    assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/node2/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v4", "/jcr:root/node2/@var4", doc);

    assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/node2/node21/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("v5", "/jcr:root/node2/node21/@var5", doc);
  }

  @Test
  void testNtFile() throws Exception {
    Document doc = underTest.buildNtFile("myMime", "myEncoding");

    assertXpathEvaluatesTo("nt:file", "/jcr:root/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("nt:resource", "/jcr:root/jcr:content/@jcr:primaryType", doc);

    assertXpathEvaluatesTo("myMime", "/jcr:root/jcr:content/@jcr:mimeType", doc);
    assertXpathEvaluatesTo("myEncoding", "/jcr:root/jcr:content/@jcr:encoding", doc);
  }

  @Test
  void testNtFileNoMime() throws Exception {
    Document doc = underTest.buildNtFile(null, null);

    assertXpathEvaluatesTo("nt:file", "/jcr:root/@jcr:primaryType", doc);
    assertXpathEvaluatesTo("nt:resource", "/jcr:root/jcr:content/@jcr:primaryType", doc);

    assertXpathNotExists("/jcr:root/jcr:content/@jcr:mimeType", doc);
    assertXpathNotExists("/jcr:root/jcr:content/@jcr:encoding", doc);
  }

  @Test
  void testBuildFilter() throws Exception {
    List<PackageFilter> filters = List.of(
        new PackageFilter("/path1"),
        new PackageFilter("/path2").addIncludeRule("/pattern1").addExcludeRule("/pattern2").addIncludeRule("/pattern3"));

    Document doc = underTest.buildFilter(filters);

    assertXpathEvaluatesTo("2", "count(/workspaceFilter/filter)", doc);
    assertXpathEvaluatesTo("/path1", "/workspaceFilter/filter[1]/@root", doc);
    assertXpathEvaluatesTo("/path2", "/workspaceFilter/filter[2]/@root", doc);

    assertXpathEvaluatesTo("3", "count(/workspaceFilter/filter[2]/*)", doc);
    assertXpathEvaluatesTo("include", "name(/workspaceFilter/filter[2]/*[1])", doc);
    assertXpathEvaluatesTo("/pattern1", "/workspaceFilter/filter[2]/*[1]/@pattern", doc);
    assertXpathEvaluatesTo("exclude", "name(/workspaceFilter/filter[2]/*[2])", doc);
    assertXpathEvaluatesTo("/pattern2", "/workspaceFilter/filter[2]/*[2]/@pattern", doc);
    assertXpathEvaluatesTo("include", "name(/workspaceFilter/filter[2]/*[3])", doc);
    assertXpathEvaluatesTo("/pattern3", "/workspaceFilter/filter[2]/*[3]/@pattern", doc);
  }

  @Test
  void testMapInvalidNodeName() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      underTest.buildContent(Map.of(
          "node1", Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "var1", "v1"),
          "*", Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "var2", "v2")));
    });
  }

  @Test
  void testMapInvalidAttributeName() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      underTest.buildContent(Map.of(
          "node1", Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "var1", "v1"),
          "node2", Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "my:illegal:attribute", "v2")));
    });
  }

  @Test
  void testContentElementInvalidNodeName() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      ContentElement root = new ContentElementImpl(null, Map.of(
          "var1", "v1",
          "var2", 55));
      ContentElement node1 = new ContentElementImpl("*",
          Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "var3", "v3"));
      root.getChildren().put("*", node1);
      underTest.buildContent(root);
    });
  }

  @Test
  void testContentElementAttributeName() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      ContentElement root = new ContentElementImpl(null, Map.of(
          "var1", "v1",
          "var2", 55));
      ContentElement node1 = new ContentElementImpl("node1",
          Map.of(XmlContentBuilder.PN_PRIMARY_TYPE, "myNodeType", "my:illegal:attribute", "v3"));
      root.getChildren().put("node1", node1);
      underTest.buildContent(root);
    });
  }

}
