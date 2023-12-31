package ext_tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.tools.Logging;

public class ToolsInformation {
    String filename;

    ArrayList<ExtTool> tools = new ArrayList<>();

    public ToolsInformation(String filename) {
        this.filename = filename;
        load();
    }

    public void load() {
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rdr.readLine()) != null) {
                sb.append(line).append('\n');
                if (line.equals("")) {
                    tools.add(ExtTool.unserialize(sb.toString()));
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e) {
            Logging.warn("Ext_Tools warning: can not load file "+filename);
        }
    }

    public void save() {
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")) {
            for (ExtTool tool : tools)
                w.write(tool.serialize());
        } catch (Exception e) {
            Logging.warn("Ext_Tools warning: can not save file "+filename);
        }
    }

    public List<ExtTool> getToolsList() {
        return tools;
    }

    public void addTool(ExtTool tool) {
        tools.add(tool);
    }

    public void removeTool(ExtTool tool) {
        tool.setEnabled(false);
        tools.remove(tool);
    }
}
