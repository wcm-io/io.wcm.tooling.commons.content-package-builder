/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import static io.wcm.tooling.commons.contentpackagebuilder.NameUtil.ensureValidPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Filter for AEM content package. Defines which content parts of the content package should be extracted when
 * installing the package, and optionally rules can define further includes/excludes when doing this. Please refer to
 * the AEM documentation for full details what is possible with package filters.
 */
public final class PackageFilter {

  private final String rootPath;
  private final List<PackageFilterRule> rules = new ArrayList<>();

  /**
   * Create filter with root path.
   * @param rootPath Root path
   */
  public PackageFilter(String rootPath) {
    ensureValidPath(rootPath);
    this.rootPath = rootPath;
  }

  /**
   * Get root path.
   * @return Root path
   */
  public String getRootPath() {
    return this.rootPath;
  }

  /**
   * Add include rule.
   * @param pattern Rule pattern
   * @return this
   */
  public PackageFilter addIncludeRule(String pattern) {
    rules.add(new PackageFilterRule(pattern, true));
    return this;
  }

  /**
   * Add exclude rule.
   * @param pattern Rule pattern
   * @return this
   */
  public PackageFilter addExcludeRule(String pattern) {
    rules.add(new PackageFilterRule(pattern, false));
    return this;
  }

  /**
   * Get defined rules.
   * @return Get include/exclude rules
   */
  public List<PackageFilterRule> getRules() {
    return Collections.unmodifiableList(rules);
  }

}
