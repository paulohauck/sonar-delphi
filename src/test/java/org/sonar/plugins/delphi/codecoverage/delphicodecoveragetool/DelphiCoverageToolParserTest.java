/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.codecoverage.delphicodecoveragetool;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiCoverageToolParserTest 
{
	  private Project project;
	  private DebugSensorContext context;
	  private File baseDir;

	  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
	  private static final String REPORT_FILE = "/org/sonar/plugins/delphi/SimpleDelphiProject/reports/Coverage.xml";

	  @Before
	  public void init() {

		context = new DebugSensorContext();
		  
	    project = mock(Project.class);
	    ProjectFileSystem pfs = mock(ProjectFileSystem.class);

	    baseDir = DelphiUtils.getResource(ROOT_NAME);

	    List<File> sourceDirs = new ArrayList<File>();

	    sourceDirs.add(baseDir); // include baseDir

	    when(project.getFileSystem()).thenReturn(pfs);

	    when(pfs.getBasedir()).thenReturn(baseDir);
	    when(pfs.getSourceDirs()).thenReturn(sourceDirs);
	 }
	
	
	@Test
	public void parseTest() {
		File reportFile = DelphiUtils.getResource(REPORT_FILE);
		DelphiCodeCoverageToolParser parser = new DelphiCodeCoverageToolParser(project, reportFile);		
		parser.parse(project, context);

	    String coverage_names[] = { "Globals.pas:coverage", "MainWindow.pas:coverage" };
	    double coverage_values[] = { 100.00, 50.00 };
	    String lineHits_names[] = { "Globals.pas:coverage_line_hits_data", "MainWindow.pas:coverage_line_hits_data" };
	    String lineHits_values[] = { "19=1;20=1", "36=1;37=0;38=1;39=0" };

	    for (int i = 0; i < coverage_names.length; ++i) { // % of coverage
	      assertEquals(coverage_names[i] + "-coverage", coverage_values[i], context.getMeasure(coverage_names[i]).getValue(), 0.0);
	      assertEquals(coverage_names[i] + "-lineHits", lineHits_values[i], context.getMeasure(lineHits_names[i]).getData());
	    }
	}
	
	
	
}
