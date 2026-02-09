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
package org.sonar.plugins.java.api;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public interface ProjectContextModelReader {
  /**
 * Determines whether the given fully qualified class name represents a Spring component.
 *
 * @param fullyQualifiedName the fully qualified name of the class (for example, "com.example.MyClass")
 * @return `true` if the class is recognized as a Spring component, `false` otherwise
 */
boolean isSpringComponent(String fullyQualifiedName);

  /**
 * Determines whether the specified fully-qualified type name represents a Spring repository.
 *
 * @param fullyQualifiedName the fully-qualified class or interface name to check
 * @return `true` if the given name corresponds to a Spring repository, `false` otherwise
 */
boolean isSpringRepository(String fullyQualifiedName);

  /**
 * List the file paths of properties files available in the project context.
 *
 * @return a set of file path strings for all properties files available in the project context, or an empty set if none
 */
Set<String> getPropertiesFilePaths();
  /**
 * Loads the Java properties from the properties file located at the given path.
 *
 * @param filePath path to the properties file (for example, one of the paths returned by {@link #getPropertiesFilePaths()})
 * @return the Properties loaded from the specified file
 */
Properties getProperties(String filePath);

  /**
 * Provides a mapping of available implementations by key.
 *
 * Each map key is a string identifying a type or role (for example an interface or service identifier),
 * and the corresponding value is the set of fully-qualified implementation class names available in the project.
 *
 * @return a map from type/identifier to the set of implementation class names
 */
Map<String, Set<String>>  availableImpls();
}