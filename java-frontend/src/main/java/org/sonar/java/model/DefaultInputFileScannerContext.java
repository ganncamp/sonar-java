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
package org.sonar.java.model;

import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.java.SonarComponents;
import org.sonar.plugins.java.api.InputFileScannerContext;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.plugins.java.api.JavaVersion;
import org.sonar.plugins.java.api.ProjectContextModelReader;
import org.sonar.plugins.java.api.caching.CacheContext;

public class DefaultInputFileScannerContext extends DefaultModuleScannerContext implements InputFileScannerContext {
  protected final InputFile inputFile;

  /**
   * Create a scanner context bound to a specific input file.
   *
   * @param inputFile the input file associated with this context
   * @param javaVersion the Java language level to use for this context
   * @param inAndroidContext `true` if the file is analyzed in an Android project context, `false` otherwise
   */
  public DefaultInputFileScannerContext(@Nullable SonarComponents sonarComponents, InputFile inputFile, JavaVersion javaVersion, boolean inAndroidContext,
                                        @Nullable CacheContext cacheContext) {
    super(sonarComponents, javaVersion, inAndroidContext, cacheContext);
    this.inputFile = inputFile;
  }

  /**
   * Creates a scanner context bound to a specific input file.
   *
   * @param inputFile the input file associated with this scanner context
   * @param inAndroidContext whether the file is being scanned in an Android-specific context
   * @param projectContextModel optional project-level model reader used to access shared project information
   */
  public DefaultInputFileScannerContext(@Nullable SonarComponents sonarComponents, InputFile inputFile, JavaVersion javaVersion, boolean inAndroidContext,
    @Nullable CacheContext cacheContext, @Nullable ProjectContextModelReader projectContextModel) {
    super(sonarComponents, javaVersion, inAndroidContext, cacheContext, projectContextModel);
    this.inputFile = inputFile;
  }

  /**
   * Registers an issue on the current input file that is not associated with a specific line.
   *
   * @param javaCheck the rule or check reporting the issue
   * @param message   a human-readable description of the issue
   */
  @Override
  public void addIssueOnFile(JavaCheck javaCheck, String message) {
    addIssue(-1, javaCheck, message);
  }

  @Override
  public void addIssue(int line, JavaCheck javaCheck, String message) {
    addIssue(line, javaCheck, message, null);
  }

  @Override
  public void addIssue(int line, JavaCheck javaCheck, String message, @Nullable Integer cost) {
    sonarComponents.addIssue(inputFile, javaCheck, line, message, cost);
  }

  @Override
  public InputFile getInputFile() {
    return inputFile;
  }

}