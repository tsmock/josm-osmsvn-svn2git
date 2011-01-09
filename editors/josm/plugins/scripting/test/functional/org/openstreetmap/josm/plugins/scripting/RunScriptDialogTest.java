package org.openstreetmap.josm.plugins.scripting;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.contourmerge.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.RunScriptDialog;


public class RunScriptDialogTest extends JFrame {
    
    private JOSMFixture fixture;
    
    public RunScriptDialogTest() {
        getContentPane().setLayout(new FlowLayout());
        setSize(100,100);
        JButton btn = new JButton("Launch");
        getContentPane().add(btn);
        btn.addActionListener(new ActionListener() {			
            @Override
            public void actionPerformed(ActionEvent e) {
                RunScriptDialog dialog = new RunScriptDialog(RunScriptDialogTest.this);
                dialog.setVisible(true);
            }
        });
        
        fixture = JOSMFixture.createUnitTestFixture();
        fixture.init();				
    }
        
    static public void main(String args[]) {
        RunScriptDialogTest app = new RunScriptDialogTest();
        app.setVisible(true);
    }
}
