/*
 * Copyright 2006-2008 Kees de Kooter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.boplicity.xmleditor;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

/**
 * Thanks: http://groups.google.com/group/de.comp.lang.java/msg/2bbeb016abad270
 *
 * IMPORTANT NOTE: regex should contain 1 group.
 *
 * Using PlainView here because we don't want line wrapping to occur.
 *
 * @author kees
 */
public class XmlView extends PlainView {

    private static Map<Pattern, Color> patternColors;
    private static String GENERIC_XML_NAME = "[A-Za-z]+[A-Za-z0-9\\-_]*(:[A-Za-z]+[A-Za-z0-9\\-_]+)?";
    private static String TAG_PATTERN = "(</?" + GENERIC_XML_NAME + ")";
    private static String TAG_END_PATTERN = "(>|/>)";
    private static String TAG_ATTRIBUTE_PATTERN = "(" + GENERIC_XML_NAME + ")\\w*\\=";
    private static String TAG_ATTRIBUTE_VALUE = "\\w*\\=\\w*(\"[^\"]*\")";
    private static String TAG_COMMENT = "(<\\!--[\\w ]*-->)";
    private static String TAG_CDATA = "(<\\!\\[CDATA\\[.*\\]\\]>)";

    static {
        // NOTE: the order is important!
        patternColors = new LinkedHashMap<>();
        patternColors
                .put(Pattern.compile(TAG_PATTERN), new Color(63, 127, 127));
        patternColors.put(Pattern.compile(TAG_CDATA), Color.GRAY);
        patternColors.put(Pattern.compile(TAG_ATTRIBUTE_PATTERN), new Color(
                127, 0, 127));
        patternColors.put(Pattern.compile(TAG_ATTRIBUTE_VALUE), new Color(42,
                0, 255));
        patternColors.put(Pattern.compile(TAG_COMMENT), Color.BLUE);
    }

    /**
     * Constructs a new {@code XmlView}.
     * @param element the element
     */
    public XmlView(Element element) {

        super(element);

        // Set tabsize to 4 (instead of the default 8)
        getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);
    }

    @Override
    protected int drawUnselectedText(Graphics graphics, int x, int y, int p0, int p1)
            throws BadLocationException {

        Document doc = getDocument();
        String text = doc.getText(p0, p1 - p0);

        Segment segment = getLineBuffer();

        SortedMap<Integer, Integer> startMap = new TreeMap<>();
        SortedMap<Integer, Color> colorMap = new TreeMap<>();

        // Match all regexes on this snippet, store positions
        for (Map.Entry<Pattern, Color> entry : patternColors.entrySet()) {

            Matcher matcher = entry.getKey().matcher(text);

            while (matcher.find()) {
                startMap.put(matcher.start(1), matcher.end());
                colorMap.put(matcher.start(1), entry.getValue());
            }
        }
        /* Fix end tag duplication for CDATA and comments */
        Matcher matcher = Pattern.compile(TAG_END_PATTERN).matcher(text);
        Color c = new Color(63, 127, 127);
        Collection<Integer> ends = startMap.values();

        while (matcher.find()) {
            if(!ends.contains(matcher.end())) {
                startMap.put(matcher.start(1), matcher.end());
                colorMap.put(matcher.start(1), c);
            }
        }

        // TODO: check the map for overlapping parts

        int i = 0;

        // Colour the parts
        for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();

            if (i < start) {
                graphics.setColor(Color.black);
                doc.getText(p0 + i, start - i, segment);
                x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
            }

            graphics.setColor(colorMap.get(start));
            i = end;
            doc.getText(p0 + start, i - start, segment);
            x = Utilities.drawTabbedText(segment, x, y, graphics, this, start);
        }

        // Paint possible remaining text black
        if (i < text.length()) {
            graphics.setColor(Color.black);
            doc.getText(p0 + i, text.length() - i, segment);
            x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
        }

        return x;
    }

}
