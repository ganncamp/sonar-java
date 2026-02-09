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
package org.sonar.java.checks.spring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.java.model.DefaultJavaFileScannerContext;
import org.sonar.java.model.DefaultModuleScannerContext;
import org.sonar.java.reporting.AnalyzerMessage;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.ModuleScannerContext;
import org.sonar.plugins.java.api.internal.EndOfAnalysis;
import org.sonar.plugins.java.api.semantic.SymbolMetadata;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

@Rule(key = "S4606")
public class SpringInnovationCheck extends IssuableSubscriptionVisitor implements EndOfAnalysis {
  private static final String[] SPRING_INJECTION_ANNOTATIONS = {
    "org.springframework.beans.factory.annotation.Autowired"
  };

  record Location(AnalyzerMessage analyzerMessage) {}
  Map<String, Set<Location>> injections = new HashMap<>();

  /**
   * Specify which AST node kinds the visitor will traverse.
   *
   * @return a list of `Tree.Kind` values to visit; contains only `Tree.Kind.CLASS`
   */
  @Override
  public List<Tree.Kind> nodesToVisit() {
    return List.of(Tree.Kind.CLASS);
  }

  /**
   * Collects fields annotated with Spring injection annotations and records analyzer messages grouped by field type for later reporting.
   *
   * For each visited class, finds variable members annotated with the configured Spring injection annotations, creates an
   * AnalyzerMessage anchored at the variable's simple name, and stores that message in the `injections` map under the
   * variable type's fully qualified name.
   */
  @Override
  public void visitNode(Tree tree) {
    if (tree instanceof ClassTree classTree) {
      DefaultJavaFileScannerContext defaultContext = (DefaultJavaFileScannerContext) context;

      for (Tree member: classTree.members()) {
        if (member instanceof VariableTree variableTree) {
          if (hasAnnotation(variableTree.symbol().metadata(), SPRING_INJECTION_ANNOTATIONS)) {
            String typeFqn = variableTree.symbol().type().fullyQualifiedName();

            Set<Location> locations = injections.computeIfAbsent(typeFqn, k -> new HashSet<>());

            // Just on the `simpleName()`, because for simplicity we don't want to include the annotation.
            AnalyzerMessage message = defaultContext.createAnalyzerMessage(this, variableTree.simpleName(), "More than one candidate implementation");
            locations.add(new Location(message));
          }
        }
      }
    }
  }

  /**
   * Checks if the given symbol metadata has any of the specified annotations.
   *
   * @param classSymbolMetadata the symbol metadata to inspect
   * @param annotationName one or more annotation fully-qualified names to look for
   * @return `true` if any of the specified annotation names are present on the symbol, `false` otherwise
   */
  private static boolean hasAnnotation(SymbolMetadata classSymbolMetadata, String... annotationName) {
    return Arrays.stream(annotationName).anyMatch(classSymbolMetadata::isAnnotatedWith);
  }

  /**
   * Report previously collected injection locations when the injected type has more than one available implementation.
   *
   * Iterates over the recorded injections and, for each type whose project model lists more than one implementation,
   * reports the associated AnalyzerMessage for each recorded location.
   *
   * @param context the module scanner context providing access to the project model and reporting facilities
   */
  @Override
  public void endOfAnalysis(ModuleScannerContext context) {
    var defaultContext = (DefaultModuleScannerContext) context;

    for(Map.Entry<String,Set<Location>> entry : injections.entrySet()) {
      String typeFqn = entry.getKey();
      Set<Location> locations = entry.getValue();
      if (defaultContext.getProjectContextModel().availableImpls().get(typeFqn).size() > 1) {
        for(Location location :locations) {
          defaultContext.reportIssue(location.analyzerMessage());
        }
      }
    }
  }
}