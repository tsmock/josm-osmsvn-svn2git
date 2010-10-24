package org.openstreetmap.josm.plugins.addressEdit;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Shortcut;
import static org.openstreetmap.josm.tools.I18n.tr;

public class AddressEditAction extends JosmAction implements
        SelectionChangedListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AddressEditAction() {
        super(tr("Address Editor"), "addressedit_24", tr("Handy Address Editing Functions"),
                Shortcut.registerShortcut("tools:AddressEdit", tr("Tool: {0}", tr("Address Edit")),
                        KeyEvent.VK_A, Shortcut.GROUP_MENU,
                        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), false);
        setEnabled(false);
        DataSet.addSelectionListener(this);
    }

    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        synchronized (this) {
            AddressVisitor addrVisitor = new AddressVisitor();
            
            for (OsmPrimitive osm : newSelection) {
                osm.visit(addrVisitor);
            }
            //generateTagCode(addrVisitor);
            
            addrVisitor.resolveAddresses();
        }
    }

    private void generateTagCode(AddressVisitor addrVisitor) {
        /* This code is abused to generate tag utility code */
        for (String tag : addrVisitor.getTags()) {
            String methodName = createMethodName(tag);			
            System.out.println(String.format("/** Check if OSM primitive has a tag '%s'.\n * @param osmPrimitive The OSM entity to check.*/\npublic static boolean has%sTag(OsmPrimitive osmPrimitive) {\n return osmPrimitive != null ? osmPrimitive.hasKey(%s_TAG) : false;\n}\n",
                    tag,
                    methodName, 
                    tag.toUpperCase().replaceAll(":", "_")));
            System.out.println(String.format("/** Gets the value of tag '%s'.\n * @param osmPrimitive The OSM entity to check.*/\npublic static String get%sValue(OsmPrimitive osmPrimitive) {\n return osmPrimitive != null ? osmPrimitive.get(%s_TAG) : null;\n}\n",
                    tag,
                    methodName, 
                    tag.toUpperCase().replaceAll(":", "_")));
        }
        
        for (String tag : addrVisitor.getTags()) {
            System.out.println(String.format("public static final String %s_TAG = \"%s\";", tag.toUpperCase().replaceAll(":", "_"), tag));
        }
    }
    
    private String createMethodName(String osmName) {
        StringBuffer result = new StringBuffer(osmName.length());
        boolean nextUp = false;
        for (int i = 0; i < osmName.length(); i++) {
            char c = osmName.charAt(i);
            if (i == 0) {
                result.append(Character.toUpperCase(c));
                continue;
            }
            if (c == '_' || c == ':') {
                nextUp = true;
            } else {
                if (nextUp) {
                    result.append(Character.toUpperCase(c));
                    nextUp = false;
                } else {
                    result.append(c);		
                }
            }
        }
        
        return result.toString();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

    }

}
