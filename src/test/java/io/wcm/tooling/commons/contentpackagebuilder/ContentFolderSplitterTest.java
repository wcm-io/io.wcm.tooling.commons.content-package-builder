/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;
import static org.apache.jackrabbit.JcrConstants.NT_FOLDER;
import static org.apache.jackrabbit.JcrConstants.NT_UNSTRUCTURED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import io.wcm.tooling.commons.contentpackagebuilder.ContentFolderSplitter.ContentPart;

class ContentFolderSplitterTest {

  @Test
  void testSplitMap_NoNodeTypes() {
    Map<String, Object> input = Map.of("k1", "v1", "o1", Map.of(
        "k11", "v11", "k12", "v12", "o13", Map.of(
            "k131", "v131", "k132", "v132")));
    List<ContentPart> result = ContentFolderSplitter.split(input);
    assertResult(result, "", input);
  }

  @Test
  void testSplitMap_Unstructured() {
    Map<String, Object> input = Map.of(JCR_PRIMARYTYPE, NT_UNSTRUCTURED, "k1", "v1", "o1", Map.of(
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED, "k11", "v11", "k12", "v12", "o13", Map.of(
            JCR_PRIMARYTYPE, NT_UNSTRUCTURED, "k131", "v131", "k132", "v132")));
    List<ContentPart> result = ContentFolderSplitter.split(input);
    assertResult(result, "", input);
  }

  @Test
  void testSplitMap_Folder() {
    Map<String, Object> input = Map.of(JCR_PRIMARYTYPE, NT_FOLDER, "k1", "v1", "o1", Map.of(
        JCR_PRIMARYTYPE, "sling:Folder", "k11", "v11", "k12", "v12", "o13", Map.of(
            JCR_PRIMARYTYPE, "sling:OrderedFolder", "k131", "v131", "k132", "v132")));
    List<ContentPart> result = ContentFolderSplitter.split(input);
    assertResult(result,
        "", Map.of(JCR_PRIMARYTYPE, NT_FOLDER, "k1", "v1"),
        "/o1", Map.of(JCR_PRIMARYTYPE, "sling:Folder", "k11", "v11", "k12", "v12"),
        "/o1/o13", Map.of(JCR_PRIMARYTYPE, "sling:OrderedFolder", "k131", "v131", "k132", "v132"));
  }

  @Test
  void testSplitMap_Page() {
    Map<String, Object> page1Content = Map.of("k1a", "v1a", "k1b", "v1b");
    Map<String, Object> page11Content = Map.of("k11a", "v11a", "k11b", "v11b");
    Map<String, Object> page111Content = Map.of("k111a", "v111a", "k111b", "v111b");
    Map<String, Object> page12Content = Map.of("k12a", "v12a", "k12b", "v12b");
    Map<String, Object> input = new TreeMap<>(Map.of(JCR_PRIMARYTYPE, "cq:Page", "jcr:content", page1Content,
        "page11", Map.of(JCR_PRIMARYTYPE, "cq:Page", "jcr:content", page11Content,
            "page111", Map.of(JCR_PRIMARYTYPE, "cq:Page", "jcr:content", page111Content)),
        "page12", Map.of(JCR_PRIMARYTYPE, "cq:Page", "jcr:content", page12Content)));

    List<ContentPart> result = ContentFolderSplitter.split(input);
    assertResult(result,
        "", Map.of(JCR_PRIMARYTYPE, "cq:Page", "jcr:content", page1Content),
        "/page11", Map.of(JCR_PRIMARYTYPE, "cq:Page", "jcr:content", page11Content),
        "/page11/page111", Map.of(JCR_PRIMARYTYPE, "cq:Page", "jcr:content", page111Content),
        "/page12", Map.of(JCR_PRIMARYTYPE, "cq:Page", "jcr:content", page12Content));
  }

  @SuppressWarnings("unchecked")
  private void assertResult(List<ContentPart> actual, Object... expectedNameMaps) {
    List<ContentPart> expected = new ArrayList<>();
    for (int i = 0; i < expectedNameMaps.length - 1; i = i + 2) {
      expected.add(new ContentPart((String)expectedNameMaps[i], (Map<String, Object>)expectedNameMaps[i + 1]));
    }
    assertEquals(expected, actual);
  }

}
