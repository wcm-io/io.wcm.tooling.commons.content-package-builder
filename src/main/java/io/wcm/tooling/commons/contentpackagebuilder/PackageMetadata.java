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

import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_AC_HANDLING;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_CREATED;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_CREATED_BY;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_DESCRIPTION;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_GROUP;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_NAME;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_PACKAGE_TYPE;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_REQUIRES_RESTART;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_REQUIRES_ROOT;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_VERSION;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.ISO8601;

/**
 * Package metadata.
 */
final class PackageMetadata {

  private static final Set<String> VALID_PACKAGE_TYPES = Set.of(
      "application", "content", "container", "mixed");

  private String group;
  private String name;
  private String description;
  private String createdBy = "admin";
  private Date created = new Date();
  private String version = "1.0";
  private AcHandling acHandling;
  private String packageType;
  private boolean requiresRoot;
  private boolean requiresRestart;
  private boolean allowIndexDefinitions;
  private final List<PackageFilter> filters = new ArrayList<>();
  private final Map<String, String> xmlNamespaces = new HashMap<>();
  private final Map<String, Object> additionalProperties = new HashMap<>();
  private byte[] thumbnailImage;

  /**
   * Default constructor
   */
  PackageMetadata() {
    // register default XML namesapces
    xmlNamespaces.putAll(XmlNamespaces.DEFAULT_NAMESPACES);
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setAcHandling(AcHandling acHandling) {
    this.acHandling = acHandling;
  }

  public void setPackageType(String packageType) {
    this.packageType = packageType;
  }

  public void setRequiresRoot(boolean requiresRoot) {
    this.requiresRoot = requiresRoot;
  }

  public void setRequiresRestart(boolean requiresRestart) {
    this.requiresRestart = requiresRestart;
  }

  public void setAllowIndexDefinitions(boolean allowIndexDefinitions) {
    this.allowIndexDefinitions = allowIndexDefinitions;
  }

  public void addFilter(PackageFilter filter) {
    filters.add(filter);
  }

  public List<PackageFilter> getFilters() {
    return Collections.unmodifiableList(filters);
  }

  public void addXmlNamespace(String prefix, String uri) {
    xmlNamespaces.put(prefix, uri);
  }

  public void addProperty(String property, Object value) {
    additionalProperties.put(property, value);
  }

  /**
   * @return XML namespaces
   */
  public Map<String, String> getXmlNamespaces() {
    return Collections.unmodifiableMap(this.xmlNamespaces);
  }

  /**
   * @param thumbnailImage Thumbnail PNG image binary data
   */
  public void setThumbnailImage(byte[] thumbnailImage) {
    this.thumbnailImage = thumbnailImage;
  }

  /**
   * @return Thumbnail PNG image binary data
   */
  public byte[] getThumbnailImage() {
    return this.thumbnailImage;
  }

  /**
   * Validates that the mandatory properties are set.
   */
  public void validate() {
    if (StringUtils.isEmpty(name) || StringUtils.isEmpty(group)) {
      throw new IllegalArgumentException("Package name or group not set.");
    }
    if (filters.isEmpty()) {
      throw new IllegalArgumentException("No package filter defined / no package root path set.");
    }
    if (created == null) {
      throw new IllegalArgumentException("Package creation date not set.");
    }
    if (packageType != null && !VALID_PACKAGE_TYPES.contains(packageType)) {
      throw new IllegalArgumentException("Invalid packageType: " + packageType);
    }
  }

  /**
   * @return Variables for placeholder replacement in package metadata.
   */
  public Map<String, Object> getVars() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(created);
    Map<String, Object> vars = new HashMap<>();
    vars.put(NAME_GROUP, StringUtils.defaultString(group));
    vars.put(NAME_NAME, StringUtils.defaultString(name));
    vars.put(NAME_DESCRIPTION, StringUtils.defaultString(description));
    vars.put(NAME_CREATED, ISO8601.format(calendar));
    vars.put(NAME_CREATED_BY, StringUtils.defaultString(createdBy));
    vars.put(NAME_VERSION, StringUtils.defaultString(version));
    vars.put(NAME_AC_HANDLING, acHandling != null ? acHandling.getMode() : "");
    vars.put(NAME_PACKAGE_TYPE, StringUtils.defaultString(packageType));
    vars.put(NAME_REQUIRES_ROOT, requiresRoot);
    if (requiresRestart) {
      vars.put(NAME_REQUIRES_RESTART, requiresRestart);
    }
    vars.put("allowIndexDefinitions", allowIndexDefinitions);
    vars.putAll(additionalProperties);
    return Collections.unmodifiableMap(vars);
  }

}
