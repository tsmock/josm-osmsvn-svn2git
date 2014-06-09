package nanolog;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Add NanoLog opening menu item and the panel.
 * 
 * @author zverik
 */
public class NanoLogPlugin extends Plugin {
    public NanoLogPlugin( PluginInformation info ) {
        super(info);
        Main.main.menu.fileMenu.insert(new OpenNanoLogLayerAction(), 4);
    }
    
    @Override
    public void mapFrameInitialized( MapFrame oldFrame, MapFrame newFrame ) {
        if( oldFrame == null && newFrame != null ) {
            NanoLogPanel panel = new NanoLogPanel();
            newFrame.addToggleDialog(panel);
            MapView.addLayerChangeListener(panel);
        }
    }
    
    private class OpenNanoLogLayerAction extends JosmAction {

        public OpenNanoLogLayerAction() {
            super(tr("Open NanoLog file..."), "nanolog.png", tr("Open NanoLog file..."), null, false);
        }

        public void actionPerformed( ActionEvent e ) {
            JFileChooser fc = new JFileChooser();
            if( fc.showOpenDialog(Main.parent) == JFileChooser.APPROVE_OPTION ) {
                try {
                    List<NanoLogEntry> entries = NanoLogLayer.readNanoLog(fc.getSelectedFile());
                    if( !entries.isEmpty() ) {
                        NanoLogLayer layer = new NanoLogLayer(entries);
                        Main.main.addLayer(layer);
                    }
                } catch( IOException ex ) {
                    JOptionPane.showMessageDialog(Main.parent, tr("Could not read NanoLog file:") + "\n" + ex.getMessage());
                }
            }
        }        
    }
}
