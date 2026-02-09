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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.java.model.declaration.ClassTreeImpl;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.SymbolMetadata;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.Tree;

public class JavaProjectContextModelVisitor extends IssuableSubscriptionVisitor {
  private static final String[] SPRING_BEAN_ANNOTATIONS = {
    "org.springframework.stereotype.Component"
  };

  private final ProjectContextModel projectContextModel;

  /**
   * Creates a visitor that populates the provided project context model while traversing Java class nodes.
   *
   * @param projectContextModel the ProjectContextModel used to collect discovered Spring components and available implementations
   */
  public JavaProjectContextModelVisitor(ProjectContextModel projectContextModel) {
    this.projectContextModel = projectContextModel;
  }

  /**
   * Lists the AST node kinds this visitor is interested in.
   *
   * @return a list containing only {@link Tree.Kind#CLASS}
   */
  @Override
  public List<Tree.Kind> nodesToVisit() {
    return List.of(Tree.Kind.CLASS);
  }

  /**
   * Processes an AST node and delegates class declaration nodes to {@link #visitClass(ClassTreeImpl)}.
   *
   * @param tree the AST node to visit; when the node is a class declaration it is handled by {@link #visitClass(ClassTreeImpl)}
   */
  @Override
  public void visitNode(Tree tree) {
    if (tree instanceof ClassTreeImpl classTree) {
      visitClass(classTree);
    }
  }

  /**
   * Processes a class AST node and records it in the project context when it represents a Spring component or bean.
   *
   * If the class is annotated with `org.springframework.stereotype.Component`, its fully qualified name is added to
   * {@code projectContextModel.springComponents}. If the class has any of the configured Spring bean annotations,
   * the class's fully qualified name is added as an available implementation for its own type and each of its
   * implemented interfaces in {@code projectContextModel.availableImpls}.
   *
   * @param classTree the class AST node to inspect and record into the project context
   */
  private void visitClass(ClassTreeImpl classTree) {
    if (classTree.modifiers().annotations().stream()
      .anyMatch(a -> a.symbolType().is("org.springframework.stereotype.Component"))) {
      projectContextModel.springComponents.add(classTree.symbol().type().fullyQualifiedName());
    }

    SymbolMetadata classSymbolMetadata = classTree.symbol().metadata();
    if (hasAnnotation(classSymbolMetadata, SPRING_BEAN_ANNOTATIONS)) {
      Symbol.TypeSymbol symbol = classTree.symbol();
      Set<String> types = getTypes(symbol);
      for (String type : types) {
        Set<String> impls = projectContextModel.availableImpls.computeIfAbsent(type, k -> new HashSet<>());
        impls.add(symbol.type().fullyQualifiedName());
      }
    }
  }

  /**
   * Checks whether the provided symbol metadata is annotated with any of the specified annotations.
   *
   * @param classSymbolMetadata metadata of the symbol to inspect
   * @param annotationName one or more annotation fully-qualified names to check for
   * @return `true` if the metadata is annotated with at least one of the given annotations, `false` otherwise
   */
  private static boolean hasAnnotation(SymbolMetadata classSymbolMetadata, String... annotationName) {
    return Arrays.stream(annotationName).anyMatch(classSymbolMetadata::isAnnotatedWith);
  }

  /**
   * Collects the fully-qualified type names for a type symbol and its directly implemented interfaces.
   *
   * @param symbol the type symbol whose type and interfaces to collect
   * @return a set containing the fully-qualified name of the given type and each directly implemented interface
   */
  private static Set<String> getTypes(Symbol.TypeSymbol symbol) {
    var result = new HashSet<String>();
    result.add(symbol.type().fullyQualifiedName());
    for (Type iface: symbol.interfaces()) {
      result.add(iface.fullyQualifiedName());
    }
    return result;
  }
}