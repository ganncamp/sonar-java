/*
 * SonarQube Java
 * Copyright (C) 2012-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.sonar.java.reporting.AnalyzerMessage;

public class ProjectContextModel implements org.sonar.plugins.java.api.ProjectContextModelReader {

  public final Set<String> springComponents = new HashSet<>();
  public final Set<String> springRepositories = new HashSet<>();
  public final Map<String, Properties> propertiesFiles = new HashMap<>();

  public record Location(AnalyzerMessage analyzerMessage) {}
  public final Map<String, Set<Location>> injections = new HashMap<>();
  public final Map<String, Set<String>> availableImpls = new HashMap<>();

  /**
   * Determines whether the provided fully qualified class name is registered as a Spring component in this project context.
   *
   * @param fullyQualifiedName the fully qualified name of the class to check
   * @return true if the class is registered as a Spring component, false otherwise
   */
  @Override
  public boolean isSpringComponent(String fullyQualifiedName) {
    return springComponents.contains(fullyQualifiedName);
  }

  /**
   * Indicates whether the type with the specified fully qualified name is configured as a Spring repository.
   *
   * @param fullyQualifiedName the fully qualified name of the type to check
   * @return `true` if the type is configured as a Spring repository (for example via `@Repository` or XML), `false` otherwise
   */
  @Override
  public boolean isSpringRepository(String fullyQualifiedName) {
    return springRepositories.contains(fullyQualifiedName);
  }

  /**
   * Retrieve the set of property file paths registered in this model.
   *
   * @return an unmodifiable set of property file path strings present in the model
   */
  @Override
  public Set<String> getPropertiesFilePaths() {
    return Collections.unmodifiableSet(propertiesFiles.keySet());
  }

  /**
   * Retrieve the Properties object associated with the specified properties file path.
   *
   * The returned Properties instance is the live, modifiable object stored in this model.
   *
   * @param filePath the properties file path key
   * @return the Properties for the specified file path, or {@code null} if no entry exists
   */
  @Override
  public Properties getProperties(String filePath) {
    //careful this is modifiable
    return propertiesFiles.get(filePath);
  }

  /**
   * Mapping from a type (typically an interface or injection point) to the set of available implementation class names.
   *
   * @return the live map whose keys are fully qualified type names and whose values are sets of fully qualified implementation class names; the returned map is the modifiable backing map
   */
  @Override
  public Map<String, Set<String>> availableImpls() {
    return availableImpls;
  }


}