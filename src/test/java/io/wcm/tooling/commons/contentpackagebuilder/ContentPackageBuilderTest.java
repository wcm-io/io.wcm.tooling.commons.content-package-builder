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
import static io.wcm.tooling.commons.contentpackagebuilder.XmlUnitUtil.assertXpathExists;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.w3c.dom.Document;

import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElementImpl;

class ContentPackageBuilderTest {

  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
  static {
    DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
  }

  private ContentPackageBuilder underTest;
  private File destDir;
  private File testFile;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    destDir = new File("target/test-" + getClass().getSimpleName() + "-" + testInfo.getTestMethod().get().getName());
    destDir.mkdirs();

    testFile = new File(destDir, "output.zip");
    if (testFile.exists()) {
      testFile.delete();
    }

    underTest = new ContentPackageBuilder();
  }

  @Test
  void testMetadata() throws Exception {
    underTest.name("myName");
    underTest.group("myGroup");
    underTest.description("myDescription");
    underTest.createdBy("myUser");
    underTest.version("1.2.3");
    underTest.rootPath("/content/mypath");
    underTest.acHandling(AcHandling.OVERWRITE);
    underTest.property("my.prop", "myvalue1");
    underTest.packageType("container");
    underTest.requiresRoot(true);
    underTest.requiresRestart(true);
    underTest.allowIndexDefinitions(true);

    try (InputStream is = getClass().getResourceAsStream("/thumbnail.png")) {
      underTest.thumbnailImage(is);
    }

    try (ContentPackage contentPackage = underTest.build(testFile)) {
      // just build empty content package to test meta data
      assertEquals("/content/mypath", contentPackage.getRootPath());
    }

    // validate metadata files
    Document configXml = getXmlFromZip("META-INF/vault/config.xml");
    assertXpathEvaluatesTo("1.1", "/vaultfs/@version", configXml);

    Document filterXml = getXmlFromZip("META-INF/vault/filter.xml");
    assertXpathEvaluatesTo("1", "count(/workspaceFilter/filter)", filterXml);
    assertXpathEvaluatesTo("/content/mypath", "/workspaceFilter/filter[1]/@root", filterXml);

    Document propsXml = getXmlFromZip("META-INF/vault/properties.xml");
    assertXpathEvaluatesTo("myGroup", "/properties/entry[@key='group']", propsXml);
    assertXpathEvaluatesTo("myName", "/properties/entry[@key='name']", propsXml);
    assertXpathEvaluatesTo("myDescription", "/properties/entry[@key='description']", propsXml);
    assertXpathExists("/properties/entry[@key='created']", propsXml);
    assertXpathEvaluatesTo("myUser", "/properties/entry[@key='createdBy']", propsXml);
    assertXpathEvaluatesTo("1.2.3", "/properties/entry[@key='version']", propsXml);
    assertXpathEvaluatesTo("true", "/properties/entry[@key='requiresRoot']", propsXml);
    assertXpathEvaluatesTo("true", "/properties/entry[@key='requiresRestart']", propsXml);
    assertXpathEvaluatesTo("true", "/properties/entry[@key='allowIndexDefinitions']", propsXml);
    assertXpathEvaluatesTo("overwrite", "/properties/entry[@key='acHandling']", propsXml);
    assertXpathEvaluatesTo("myvalue1", "/properties/entry[@key='my.prop']", propsXml);
    assertXpathEvaluatesTo("container", "/properties/entry[@key='packageType']", propsXml);

    Document settingsXml = getXmlFromZip("META-INF/vault/settings.xml");
    assertXpathEvaluatesTo("1.0", "/vault/@version", settingsXml);

    Document definitionXml = getXmlFromZip("META-INF/vault/definition/.content.xml");
    assertEquals("vlt:PackageDefinition", definitionXml.getDocumentElement().getAttribute("jcr:primaryType"));

    byte[] thumbnailImage = getDataFromZip("META-INF/vault/definition/thumbnail.png");
    assertNotNull(thumbnailImage);
  }

  @Test
  void testAddPagesContentElement() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add two content pages
      contentPackage.addPage("/content/page1", new ContentElementImpl(null, Map.of("var1", "v1")));
      contentPackage.addPage("/content/ns:page2", new ContentElementImpl(null, Map.of("var2", "v2")));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/page1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/jcr:content/@var1", page1Xml);

    Document page2Xml = getXmlFromZip("jcr_root/content/_ns_page2/.content.xml");
    assertXpathEvaluatesTo("v2", "/jcr:root/jcr:content/@var2", page2Xml);
  }

  @Test
  void testAddPages() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add two content pages
      contentPackage.addPage("/content/page1", Map.of("var1", "v1"));
      contentPackage.addPage("/content/ns:page2", Map.of("var2", "v2"));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/page1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/jcr:content/@var1", page1Xml);

    Document page2Xml = getXmlFromZip("jcr_root/content/_ns_page2/.content.xml");
    assertXpathEvaluatesTo("v2", "/jcr:root/jcr:content/@var2", page2Xml);
  }

  @Test
  void testAddContentMultipleFilters() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName")
        .filter(new PackageFilter("/path1"))
        .filter(new PackageFilter("/path2"));
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add some content
      contentPackage.addContent("/content/node1", Map.of("var1", "v1"));
      assertEquals(2, contentPackage.getFilters().size());
    }

    // validate resulting XML
    Document filterXml = getXmlFromZip("META-INF/vault/filter.xml");
    assertXpathEvaluatesTo("2", "count(/workspaceFilter/filter)", filterXml);
    assertXpathEvaluatesTo("/path1", "/workspaceFilter/filter[1]/@root", filterXml);
    assertXpathEvaluatesTo("/path2", "/workspaceFilter/filter[2]/@root", filterXml);
  }

  @Test
  void testAddContentContentElement() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add some content
      contentPackage.addContent("/content/node1", new ContentElementImpl(null, Map.of("var1", "v1")));
      contentPackage.addContent("/content/node2", new ContentElementImpl(null, Map.of("var2", "v2")));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/node1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/@var1", page1Xml);

    Document page2Xml = getXmlFromZip("jcr_root/content/node2/.content.xml");
    assertXpathEvaluatesTo("v2", "/jcr:root/@var2", page2Xml);
  }

  @Test
  void testAddContent() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add some content
      contentPackage.addContent("/content/node1", Map.of("var1", "v1"));
      contentPackage.addContent("/content/node2", Map.of("var2", "v2"));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/node1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/@var1", page1Xml);

    Document page2Xml = getXmlFromZip("jcr_root/content/node2/.content.xml");
    assertXpathEvaluatesTo("v2", "/jcr:root/@var2", page2Xml);
  }

  @Test
  void testAddContent_SplitContent() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add some content
      contentPackage.addContent("/content/node1",
          Map.of(JCR_PRIMARYTYPE, "sling:Folder", "var1", "v1",
              "node11", Map.of(JCR_PRIMARYTYPE, "sling:Folder", "var11", "v11",
                  "node111", Map.of(JCR_PRIMARYTYPE, "sling:Folder", "var111", "v111"))));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/node1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/@var1", page1Xml);

    Document page11Xml = getXmlFromZip("jcr_root/content/node1/node11/.content.xml");
    assertXpathEvaluatesTo("v11", "/jcr:root/@var11", page11Xml);

    Document page111Xml = getXmlFromZip("jcr_root/content/node1/node11/node111/.content.xml");
    assertXpathEvaluatesTo("v111", "/jcr:root/@var111", page111Xml);
  }

  @Test
  void testAddContentCustomNamespace() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test")
        .xmlNamespace(XmlUnitUtil.CUSTOM_NS_PREFIX, XmlUnitUtil.CUSTOM_NS_URI);
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add some content
      contentPackage.addContent("/content/node1", Map.of("myns:var1", "v1"));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/node1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/@myns:var1", page1Xml);
  }

  @Test
  void testIllegalRootPath() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test/*");
      try (ContentPackage contentPackage = builder.build(testFile)) {
        // add some content
        contentPackage.addContent("/content/node1", Map.of("var1", "v1"));
      }
    });
  }

  @Test
  void testIllegalContentPath() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
      try (ContentPackage contentPackage = builder.build(testFile)) {
        // add some content
        contentPackage.addContent("/content/*", Map.of("var1", "v1"));
      }
    });
  }

  @Test
  void testAddBinaries() throws Exception {

    byte[] data1 = "content1".getBytes(StandardCharsets.UTF_8);
    byte[] data2 = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {

      // add two binary files
      try (InputStream is1 = new ByteArrayInputStream(data1)) {
        contentPackage.addFile("/content/file1.txt", is1, "text/plain;charset=UTF-8");
      }
      try (InputStream is2 = new ByteArrayInputStream(data2)) {
        contentPackage.addFile("/content/file2.bin", is2);
      }

    }

    // validate resulting files
    assertArrayEquals(data1, getDataFromZip("jcr_root/content/file1.txt"));
    Document metaXml = getXmlFromZip("jcr_root/content/file1.txt.dir/.content.xml");
    assertXpathEvaluatesTo("text/plain", "/jcr:root/jcr:content/@jcr:mimeType", metaXml);
    assertXpathEvaluatesTo("UTF-8", "/jcr:root/jcr:content/@jcr:encoding", metaXml);

    assertArrayEquals(data2, getDataFromZip("jcr_root/content/file2.bin"));
  }

  private byte[] getDataFromZip(String path) throws Exception {
    return ContentPackageTestUtil.getDataFromZip(testFile, path);
  }

  private Document getXmlFromZip(String path) throws Exception {
    return ContentPackageTestUtil.getXmlFromZip(testFile, path);
  }
}
