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

import java.io.File;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.java.SonarComponents;
import org.sonar.java.caching.CacheContextImpl;
import org.sonar.java.reporting.AnalyzerMessage;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.plugins.java.api.JavaVersion;
import org.sonar.plugins.java.api.ModuleScannerContext;
import org.sonar.plugins.java.api.ProjectContextModelReader;
import org.sonar.plugins.java.api.caching.CacheContext;

public class DefaultModuleScannerContext implements ModuleScannerContext {
  protected final SonarComponents sonarComponents;
  protected final JavaVersion javaVersion;
  protected final boolean inAndroidContext;
  protected final CacheContext cacheContext;
  private final ProjectContextModelReader projectContextModelReader;

  /**
   * Constructs a DefaultModuleScannerContext configured with the provided Sonar components, Java version,
   * Android context flag, and cache context. The ProjectContextModelReader is not set.
   *
   * @param sonarComponents Sonar components used to access project and runtime context, or `null` if unavailable
   * @param javaVersion the Java version to use for analysis
   * @param inAndroidContext `true` when running in an Android-specific scanning context
   * @param cacheContext cache context to use for incremental/cache-aware operations, or `null` to use a default
   */
  public DefaultModuleScannerContext(@Nullable SonarComponents sonarComponents, JavaVersion javaVersion, boolean inAndroidContext,
    @Nullable CacheContext cacheContext) {
    this(sonarComponents, javaVersion, inAndroidContext, cacheContext, null);
  }

  /**
   * Creates a DefaultModuleScannerContext with the given Sonar components, Java version, Android flag,
   * cache context and optional project context model reader.
   *
   * If `cacheContext` is null, a CacheContext is created via CacheContextImpl.of(sonarComponents).
   *
   * @param sonarComponents             Sonar components used to access project and runtime services; may be null
   * @param javaVersion                 Java version to associate with this context
   * @param inAndroidContext            true when scanning in an Android-specific context
   * @param cacheContext                cache context to use; if null a default CacheContext is created
   * @param projectContextModelReader   optional reader for the project context model; may be null
   */
  public DefaultModuleScannerContext(@Nullable SonarComponents sonarComponents, JavaVersion javaVersion, boolean inAndroidContext,
    @Nullable CacheContext cacheContext, @Nullable ProjectContextModelReader projectContextModelReader) {
    this.sonarComponents = sonarComponents;
    this.javaVersion = javaVersion;
    this.inAndroidContext = inAndroidContext;
    if (cacheContext != null) {
      this.cacheContext = cacheContext;
    } else {
      this.cacheContext = CacheContextImpl.of(sonarComponents);
    }
    this.projectContextModelReader = projectContextModelReader;
  }

  /**
   * Adds an issue to the current project for the specified Java check with the provided message.
   *
   * The issue is reported at the project level (no specific file or line).
   *
   * @param check   the Java check (rule) the issue relates to
   * @param message the human-readable issue message
   */
  public void addIssueOnProject(JavaCheck check, String message) {
    sonarComponents.addIssue(getProject(), check, -1, message, 0);
  }

  public JavaVersion getJavaVersion() {
    return this.javaVersion;
  }

  public boolean inAndroidContext() {
    return inAndroidContext;
  }

  public InputComponent getProject() {
    return sonarComponents.project();
  }

  @Override
  public File getWorkingDirectory() {
    return sonarComponents.projectLevelWorkDir();
  }

  public CacheContext getCacheContext() {
    return cacheContext;
  }

  public void reportIssue(AnalyzerMessage message) {
    sonarComponents.reportIssue(message);
  }

  @Override
  public File getRootProjectWorkingDirectory() {
    return sonarComponents.projectLevelWorkDir();
  }

  @Override
  public String getModuleKey() {
    return sonarComponents.getModuleKey();
  }

  /**
   * Retrieve the SonarProduct from the current SonarComponents runtime, or `null` when unavailable.
   *
   * @return `null` if `sonarComponents` or its context is `null`; otherwise the current `SonarProduct`
   */
  @CheckForNull
  @Override
  public SonarProduct sonarProduct() {
    // In production, sonarComponents and sonarComponents.context() should never be null.
    // However, in testing contexts, this can happen and calling this method should not cause tests to fail.
    if (sonarComponents == null) {
      return null;
    }

    var context = sonarComponents.context();
    if (context == null) {
      return null;
    }

    return context.runtime().getProduct();
  }

  /**
   * Accesses the project's context model reader.
   *
   * @return the ProjectContextModelReader associated with this context, or `null` if none was provided
   */
  @Nullable
  @Override
  public ProjectContextModelReader getProjectContextModel() {
    return projectContextModelReader;
  }
}