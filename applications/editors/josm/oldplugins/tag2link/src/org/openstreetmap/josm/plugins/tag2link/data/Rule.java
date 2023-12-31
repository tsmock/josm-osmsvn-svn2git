//    JOSM tag2link plugin.
//    Copyright (C) 2011-2012 Don-vip & FrViPofm
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.tag2link.data;

import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Tags;

public class Rule {
    public final Collection<Condition> conditions = new ArrayList<>();
    public final Collection<Link> links = new ArrayList<>();

    public static class MatchingTag {
        public String key;
        public String value;
        public final Map<String, String> params;
        private String prefix;
        public MatchingTag(String key, String value, String prefix) {
            this.key = key;
            this.value = value;
            this.params = new HashMap<>();
            this.prefix = prefix;
            addKeyValueParams();
        }
        public void addParams(Matcher m, String paramName) {
            for (int i = 1; i <= m.groupCount(); i++) {
                params.put(prefix+paramName+"."+i, m.group(i));
            }
        }
        private void addKeyValueParams() {
            params.put("k", key);
            params.put("v", value);
            if (!prefix.isEmpty()) {
                params.put(prefix+"k", key);
                params.put(prefix+"v", value);
            }
        }
        @Override
        public String toString() {
            return "MatchingTag [" + (key != null ? "key=" + key + ", " : "")
                    + (value != null ? "value=" + value + ", " : "")
                    + "params=" + params + ", "
                    + (prefix != null ? "prefix=" + prefix : "") + ']';
        }
    }

    public static class EvalResult {
        private final int conditionsNumber;
        public EvalResult(int conditionsNumber) {
            this.conditionsNumber = conditionsNumber;
        }
        public final Collection<MatchingTag> matchingTags = new ArrayList<>();
        public boolean matches() {
            return conditionsNumber > 0 && matchingTags.size() >= conditionsNumber;
        }
        @Override
        public String toString() {
            return "EvalResult [conditionsNumber=" + conditionsNumber
                    + ", matchingTags=" + matchingTags + ']';
        }
    }

    public EvalResult evaluates(Iterable<String> keys, Function<String, Iterable<String>> valuesFn) {
        EvalResult result = new EvalResult(conditions.size());
        for (Condition c : conditions) {
            for (String key : keys) {
                Matcher keyMatcher = c.keyPattern.matcher(key);
                if (keyMatcher.matches()) {
                    String idPrefix = c.id == null ? "" : c.id+".";
                    for (String value : valuesFn.apply(key)) {
                        MatchingTag tag = new MatchingTag(key, value, idPrefix);
                        tag.addParams(keyMatcher, "k");
                        boolean matchingTag = true;
                        if (c.valPattern != null) {
                            Matcher valMatcher = c.valPattern.matcher(tag.value);
                            if (valMatcher.matches()) {
                                tag.addParams(valMatcher, "v");
                            } else {
                                matchingTag = false;
                            }
                        }
                        if (matchingTag) {
                            result.matchingTags.add(tag);
                        }
                    }
                }
            }
        }
        return result;
    }

    public EvalResult evaluates(IPrimitive p) {
        return evaluates(p.keySet(), key -> singleton(p.get(key)));
    }

    public EvalResult evaluates(Tag tag) {
        return evaluates(singleton(tag.getKey()), x -> singleton(tag.getValue()));
    }

    public EvalResult evaluates(Tags tags) {
        return evaluates(singleton(tags.getKey()), x -> tags.getValues());
    }

    @Override
    public String toString() {
        return "Rule [conditions=" + conditions + ", links=" + links + ']';
    }
}
