/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.BeforeClass;

public class AbstractFlexMojosTests
{

    protected static File projectsSource;

    protected static File projectsWorkdir;

    private static Properties props;

    @BeforeClass
    public static void initFolders()
        throws IOException
    {
        props = new Properties();
        ClassLoader cl = AbstractFlexMojosTests.class.getClassLoader();
        InputStream is = cl.getResourceAsStream( "baseTest.properties" );
        if ( is != null )
        {
            try
            {
                props.load( is );
            }
            finally
            {
                is.close();
            }
        }

        projectsSource = new File( getProperty( "projects-source" ) );
        projectsWorkdir = new File( getProperty( "projects-target" ) );
    }

    protected static synchronized String getProperty( String key )
        throws IOException
    {
        return props.getProperty( key );
    }

    @SuppressWarnings( "unchecked" )
    protected static void test( File projectDirectory, String goal, String... args )
        throws Exception
    {
        Verifier verifier = getVerifier( projectDirectory );
        verifier.getCliOptions().addAll( Arrays.asList( args ) );
        verifier.executeGoal( goal );
        verifier.verifyErrorFreeLog();
    }

    protected static Verifier getVerifier( File projectDirectory )
        throws IOException, VerificationException
    {
        System.setProperty( "maven.home", getProperty( "fake-maven" ) );

        Verifier verifier = new Verifier( projectDirectory.getAbsolutePath() );
        // verifier.getCliOptions().add( "-s" + rootFolder.getAbsolutePath() + "/settings.xml" );
        // verifier.getCliOptions().add( "-o" );
        verifier.getVerifierProperties().put( "use.mavenRepoLocal", "true" );
        verifier.setLocalRepo( getProperty( "fake-repo" ) );
        return verifier;
    }

    @SuppressWarnings( "unchecked" )
    protected File getProject( String projectName )
        throws IOException
    {
        File projectFolder = new File( projectsSource, projectName );
        Assert.assertTrue( "Project " + projectName + " folder not found.\n" + projectFolder.getAbsolutePath(),
                           projectFolder.isDirectory() );

        File destDir = new File( projectsWorkdir, projectName );
        FileUtils.copyDirectory( projectFolder, destDir );

        // projects filtering
        Collection<File> poms =
            FileUtils.listFiles( destDir, new WildcardFileFilter( "pom.xml" ), DirectoryFileFilter.INSTANCE );
        for ( File pom : poms )
        {
            String pomContent = FileUtils.readFileToString( pom );
            pomContent = pomContent.replace( "%{flex-mojos.version}", getProperty( "version" ) );
            FileUtils.writeStringToFile( pom, pomContent );
        }

        return destDir;
    }

}