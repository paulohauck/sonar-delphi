/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.delphi.utils;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.codehaus.plexus.util.DirectoryScanner;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.same;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.measures.Metric;
import org.sonar.plugins.delphi.core.DelphiLanguage;

public class TestUtils {

  private final static String OS = System.getProperty("os.name").toLowerCase();

  public static File loadResource(String resourceName) {
    URL resource = TestUtils.class.getResource(resourceName);
    File resourceAsFile = null;
    try {
      resourceAsFile = new File(resource.toURI());
    } catch (URISyntaxException e) {
      System.out.println("Cannot load resource: " + resourceName);
    }

    return resourceAsFile;
  }

  /**
   * Mocks the filesystem given the root directory of the project
   *
   * @param baseDir project root directory
   * @return mocked filesystem
   */
  public static DefaultFileSystem mockFileSystem(File baseDir) {
    return mockFileSystem(baseDir, null, null);
  }

  /**
   * Mocks the filesystem given the root directory of the project
   *
   * @param baseDir project root directory
   * @return mocked filesystem
   */
  public static DefaultFileSystem mockFileSystem(String rootDir) {
    return mockFileSystem(TestUtils.loadResource(rootDir), Arrays.asList(new File(".")), null);
  }

  /**
   * Mocks the filesystem given the root directory and lists of source and tests
   * directories. The latter are given just as in sonar-project.properties
   *
   * @param baseDir project root directory
   * @param sourceDirs List of source directories, relative to baseDir.
   * @param testDirs List of test directories, relative to baseDir.
   * @return mocked filesystem
   */
  public static DefaultFileSystem mockFileSystem(File baseDir,
    List<File> sourceDirs,
    List<File> testDirs) {
    DefaultFileSystem fs = new DefaultFileSystem(baseDir);
    fs.setEncoding(Charset.forName("UTF-8"));
    scanDirs(fs, sourceDirs, Type.MAIN);
    scanDirs(fs, testDirs, Type.TEST);
    return fs;
  }



  public static boolean isWindows() {
    return OS.contains("win");
  }

  public static DelphiLanguage mockDelphiLanguage() {
    DelphiLanguage language = Mockito.mock(DelphiLanguage.class);
    when(language.getKey()).thenReturn("delphi");
    when(language.getName()).thenReturn("delphi");
    when(language.getFileSuffixes())
      .thenReturn(new String[]{".cpp", ".hpp", ".h", ".cxx", ".c", ".cc", ".hxx", ".hh"});

    return language;
  }
  
  private static void scanDirs(DefaultFileSystem fs, List<File> dirs, Type ftype) {
    if (dirs == null) {
      return;
    }

    String[] suffixes = mockDelphiLanguage().getFileSuffixes();
    String[] includes = new String[suffixes.length];
    for (int i = 0; i < includes.length; ++i) {
      includes[i] = "**/*" + suffixes[i];
    }

    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setIncludes(includes);
    File target;
    for (File dir : dirs) {
      scanner.setBasedir(new File(fs.baseDir(), dir.getPath()));
      scanner.scan();
      for (String path : scanner.getIncludedFiles()) {
        target = new File(dir, path);
        fs.add(TestInputFileBuilder.create("ProjectKey", target.getPath()).setLanguage("cpp").setType(ftype).build());
      }
    }
  }

  /**
   * Search for a test resource in the classpath. For example
   * getResource("org/sonar/MyClass/foo.txt");
   *
   * @param path the starting slash is optional
   * @return the resource. Null if resource not found
   */
  @CheckForNull
  public static File getResource(String path) {
    String resourcePath = path;
    if (!resourcePath.startsWith("/")) {
      resourcePath = "/" + resourcePath;
    }
    URL url = TestUtils.class.getResource(resourcePath);
    if (url != null) {
      try {
        return new File(url.toURI());
      } catch (URISyntaxException e) {
        return null;
      }
    }
    return null;
  }

}
