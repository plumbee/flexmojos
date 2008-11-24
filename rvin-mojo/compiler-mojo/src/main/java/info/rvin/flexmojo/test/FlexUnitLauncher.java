/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.rvin.flexmojo.test;

import flash.util.StringUtils;
import info.rvin.flexmojos.utilities.MavenUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * This class is used to launch the FlexUnit tests.
 */
public class FlexUnitLauncher
{
    private static final String[] WINDOWS_CMD = new String[] { "rundll32", "url.dll,FileProtocolHandler" };

    private static final String[] MAC_CMD = new String[] { "open", "-g" };

    private static final String[] UNIX_CMD = new String[] { "xdg-open" };

    private String[] launcherCommand = new String[] {};
    private Log log;

    public FlexUnitLauncher()
    {
        launcherCommand = getPlatformDefaultCommand();
    }

    public FlexUnitLauncher( List<String> command, Log logger )
    {
        if ( command != null )
        {
            launcherCommand = command.toArray( new String[] {} );
        }
        else
        {
            launcherCommand = getPlatformDefaultCommand();
        }
    	log = logger;
    }

    private String[] getPlatformDefaultCommand()
    {
        if ( MavenUtils.isWindows() )
        {
            // Ideally we want to launch the SWF in the player so we can close
            // it, not so easy in a browser. We let 'rundll32' do the work based
            // on the extension of the file passed in.
            return WINDOWS_CMD;
        }
        else if ( MavenUtils.isMac() )
        {
            // Ideally we want to launch the SWF in the player so we can close
            // it, not so easy in a browser. We let 'open' do the work based
            // on the extension of the file passed in.
            return MAC_CMD;
        }
        else
        {
            // If we are running in UNIX the fallback is to the browser. To do
            // this Netscape must be running for the "-remote" flag to work. If
            // the browser is not running we need to start it.
            return UNIX_CMD;
        }
    }

    /**
     * Run the SWF that contains the FlexUnit tests.
     * 
     * @param swf the SWF.
     * @throws Exception if there is an error launching the tests.
     */
    public void runTests( File swf )
        throws Exception
    {
        if ( launcherCommand != null )
        {
            launcherCommand = StringUtils.concat( launcherCommand, new String[] { swf.getAbsolutePath() } );
            log.debug( "exec: " + Arrays.asList( launcherCommand ) );
            Commandline cl = new Commandline();
            cl.setExecutable( launcherCommand[0] );
            for ( int i = 1; i < launcherCommand.length; i++ )
            {
                cl.createArg().setValue( launcherCommand[i] );
            }
            StreamConsumer stdout = new StreamConsumer()
            {
                public void consumeLine( String line )
                {
                    log.debug( "[SYSOUT]: "+line );
                }
            };
            
            StreamConsumer stderr = new StreamConsumer()
            {
                public void consumeLine( String line )
                {
                    log.debug( "[SYSERR]: "+line );
                }
            };           
            log.debug("commandline: "+cl);
            int result = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
            log.debug("result: "+result);
        }
        else if ( MavenUtils.isWindows() )
        {
            // Ideally we want to launch the SWF in the player so we can close
            // it, not so easy in a browser. We let 'rundll32' do the work based
            // on the extension of the file passed in.
            Runtime.getRuntime().exec( StringUtils.concat( WINDOWS_CMD, new String[] { swf.getAbsolutePath() } ) );
        }
        else if ( MavenUtils.isMac() )
        {
            // Ideally we want to launch the SWF in the player so we can close
            // it, not so easy in a browser. We let 'open' do the work based
            // on the extension of the file passed in.
            Runtime.getRuntime().exec( StringUtils.concat( MAC_CMD, new String[] { swf.getAbsolutePath() } ) );
        }
        else
        {
            // If we are running in UNIX the fallback is to the browser. To do
            // this Netscape must be running for the "-remote" flag to work. If
            // the browser is not running we need to start it.
            Process p =
                Runtime.getRuntime().exec( StringUtils.concat( UNIX_CMD, new String[] { swf.getAbsolutePath() } ) );

            // If the exist code is '0', then the browser was running, otherwise
            // we need to start the browser.
            int exitValue = p.waitFor();

            if ( exitValue != 0 )
            {
                Runtime.getRuntime().exec( StringUtils.concat( UNIX_CMD, new String[] { swf.getAbsolutePath() } ) );
            }
        }
    }

}
