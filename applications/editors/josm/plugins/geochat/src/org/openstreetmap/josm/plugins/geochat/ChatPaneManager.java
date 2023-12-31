// License: WTFPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geochat;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

/**
 *
 * @author zverik
 */
class ChatPaneManager {
    private static final String PUBLIC_PANE = "Public Pane";

    private final GeoChatPanel panel;
    private final JTabbedPane tabs;
    private final Map<String, ChatPane> chatPanes;
    private boolean collapsed;

    ChatPaneManager(GeoChatPanel panel, JTabbedPane tabs) {
        this.panel = panel;
        this.tabs = tabs;
        this.collapsed = panel.isDialogInCollapsedView();
        chatPanes = new HashMap<>();
        createChatPane(null);
        tabs.addChangeListener(e -> updateActiveTabStatus());
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        updateActiveTabStatus();
    }

    public boolean hasUser(String user) {
        return chatPanes.containsKey(user == null ? PUBLIC_PANE : user);
    }

    public Component getPublicChatComponent() {
        return chatPanes.get(PUBLIC_PANE).component;
    }

    public int getNotifyLevel() {
        int alarm = 0;
        for (ChatPane entry : chatPanes.values()) {
            if (entry.notify > alarm)
                alarm = entry.notify;
        }
        return alarm;
    }

    public void updateActiveTabStatus() {
        if (tabs.getSelectedIndex() >= 0)
            ((ChatTabTitleComponent) tabs.getTabComponentAt(tabs.getSelectedIndex())).updateAlarm();
    }

    public void notify(String user, int alarmLevel) {
        if (alarmLevel <= 0 || !hasUser(user))
            return;
        ChatPane entry = chatPanes.get(user == null ? PUBLIC_PANE : user);
        entry.notify = alarmLevel;
        int idx = tabs.indexOfComponent(entry.component);
        if (idx >= 0)
            GuiHelper.runInEDT(((ChatTabTitleComponent) tabs.getTabComponentAt(idx))::updateAlarm);
    }

    public static final int MESSAGE_TYPE_DEFAULT = 0;
    public static final int MESSAGE_TYPE_INFORMATION = 1;
    public static final int MESSAGE_TYPE_ATTENTION = 2;
    private static final Color COLOR_ATTENTION = new Color(0, 0, 192);

    private void addLineToChatPane(String userName, String line, final int messageType) {
        if (line.isEmpty())
            return;
        if (!chatPanes.containsKey(userName))
            createChatPane(userName);
        final String nline = line.startsWith("\n") ? line : "\n" + line;
        final JTextPane thepane = chatPanes.get(userName).pane;
        GuiHelper.runInEDT(() -> {
            Document doc = thepane.getDocument();
            try {
                SimpleAttributeSet attrs = null;
                if (messageType != MESSAGE_TYPE_DEFAULT) {
                    attrs = new SimpleAttributeSet();
                    if (messageType == MESSAGE_TYPE_INFORMATION)
                        StyleConstants.setItalic(attrs, true);
                    else if (messageType == MESSAGE_TYPE_ATTENTION)
                        StyleConstants.setForeground(attrs, COLOR_ATTENTION);
                }
                doc.insertString(doc.getLength(), nline, attrs);
            } catch (BadLocationException ex) {
                Logging.warn(ex);
            }
            thepane.setCaretPosition(doc.getLength());
        });
    }

    public void addLineToChatPane(String userName, String line) {
        addLineToChatPane(userName, line, MESSAGE_TYPE_DEFAULT);
    }

    public void addLineToPublic(String line) {
        addLineToChatPane(PUBLIC_PANE, line);
    }

    public void addLineToPublic(String line, int messageType) {
        addLineToChatPane(PUBLIC_PANE, line, messageType);
    }

    public void clearPublicChatPane() {
        chatPanes.get(PUBLIC_PANE).pane.setText("");
    }

    public void clearChatPane(String userName) {
        if (userName == null || userName.equals(PUBLIC_PANE))
            clearPublicChatPane();
        else
            chatPanes.get(userName).pane.setText("");
    }

    public void clearActiveChatPane() {
        clearChatPane(getActiveChatPane());
    }

    public ChatPane createChatPane(String userName) {
        JTextPane chatPane = new JTextPane();
        chatPane.setEditable(false);
        Font font = chatPane.getFont();
        float size = Config.getPref().getInt("geochat.fontsize", -1);
        if (size < 6)
            size += font.getSize2D();
        chatPane.setFont(font.deriveFont(size));
        //        DefaultCaret caret = (DefaultCaret)chatPane.getCaret(); // does not work
        //        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(chatPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatPane.addMouseListener(new GeoChatPopupAdapter(panel));

        ChatPane entry = new ChatPane();
        entry.pane = chatPane;
        entry.component = scrollPane;
        entry.notify = 0;
        entry.userName = userName;
        entry.isPublic = userName == null;
        chatPanes.put(userName == null ? PUBLIC_PANE : userName, entry);

        tabs.addTab(null, scrollPane);
        tabs.setTabComponentAt(tabs.getTabCount() - 1, new ChatTabTitleComponent(entry));
        tabs.setSelectedComponent(scrollPane);
        return entry;
    }

    /**
     * Returns key in chatPanes hash map for the currently active
     * chat pane, or null in case of an error.
     * @return key in chatPanes hash map for the currently active chat pane
     */
    public String getActiveChatPane() {
        Component c = tabs.getSelectedComponent();
        if (c == null)
            return null;
        for (Map.Entry<String, ChatPaneManager.ChatPane> entry : chatPanes.entrySet()) {
            if (c.equals(entry.getValue().component))
                return entry.getKey();
        }
        return null;
    }

    public String getRecipient() {
        String user = getActiveChatPane();
        return user == null || user.equals(PUBLIC_PANE) ? null : user;
    }

    public void closeChatPane(String user) {
        if (user == null || user.equals(PUBLIC_PANE) || !chatPanes.containsKey(user))
            return;
        tabs.remove(chatPanes.get(user).component);
        chatPanes.remove(user);
    }

    public void closeSelectedPrivatePane() {
        String pane = getRecipient();
        if (pane != null)
            closeChatPane(pane);
    }

    public void closePrivateChatPanes() {
        List<String> entries = new ArrayList<>(chatPanes.keySet());
        for (String user : entries) {
            if (!user.equals(PUBLIC_PANE))
                closeChatPane(user);
        }
    }

    public boolean hasSelectedText() {
        String user = getActiveChatPane();
        if (user != null) {
            JTextPane pane = chatPanes.get(user).pane;
            return pane.getSelectedText() != null;
        }
        return false;
    }

    public void copySelectedText() {
        String user = getActiveChatPane();
        if (user != null)
            chatPanes.get(user).pane.copy();
    }

    private class ChatTabTitleComponent extends JLabel {
        private final ChatPane entry;

        ChatTabTitleComponent(ChatPane entry) {
            super(entry.isPublic ? tr("Public") : entry.userName);
            this.entry = entry;
        }

        private Font normalFont;
        private Font boldFont;

        public void updateAlarm() {
            if (normalFont == null) {
                // prepare cached fonts
                normalFont = getFont().deriveFont(Font.PLAIN);
                boldFont = getFont().deriveFont(Font.BOLD);
            }
            if (entry.notify > 0 && !collapsed && tabs.getSelectedIndex() == tabs.indexOfComponent(entry.component))
                entry.notify = 0;
            setFont(entry.notify > 0 ? boldFont : normalFont);
            panel.updateTitleAlarm();
        }
    }

    static class ChatPane {
        public String userName;
        public boolean isPublic;
        public JTextPane pane;
        public JScrollPane component;
        public int notify;
    }
}
