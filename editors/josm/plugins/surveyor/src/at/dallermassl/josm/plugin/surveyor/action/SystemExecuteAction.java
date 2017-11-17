// License: GPL. For details, see LICENSE file.
package at.dallermassl.josm.plugin.surveyor.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openstreetmap.josm.tools.Logging;

import at.dallermassl.josm.plugin.surveyor.GpsActionEvent;

/**
 * @author cdaller
 *
 */
public class SystemExecuteAction extends AbstractSurveyorAction {

    @Override
    public void actionPerformed(GpsActionEvent event) {
        final ProcessBuilder builder = new ProcessBuilder(getParameters());
        builder.directory(new File(System.getProperty("user.home")));

        Logging.debug("Directory : " + builder.directory());
        Thread executionThread = new Thread() {

            @Override
            public void run() {
                try {
                    final Process process = builder.start();
                    InputStream is = process.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;

                    while ((line = br.readLine()) != null) {
                        Logging.info(getClass().getSimpleName() + ": " + line);
                    }

                    Logging.info(getClass().getSimpleName() + "Program terminated!");
                } catch (Exception t) {
                    Logging.error(t);
                }
            }
        };
        executionThread.start();
    }
}
