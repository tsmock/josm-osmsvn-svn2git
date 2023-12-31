// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

public class Command {
    public String name;                         // Command name
    public String run;                          // Executable file with arguments ("nya.exe {arg1} {arg2} ... {argn}")
    public String icon;                         // Icon file name
    public ArrayList<Parameter> parameters;    // Required parameters list
    public ArrayList<Parameter> optParameters;    // Optional parameters list
    public int currentParameterNum;
    public boolean tracks;
    public boolean asynchronous;

    public Command() {
        parameters = new ArrayList<>();
        optParameters = new ArrayList<>();
        currentParameterNum = 0;
        tracks = false;
        asynchronous = false;
        icon = "";
    }

    @SuppressWarnings("unchecked")
    public boolean loadObject(Object obj) {
        Parameter currentParameter = parameters.get(currentParameterNum);
        if (currentParameter.maxInstances == 1) {
            if (isPair(obj, currentParameter)) {
                currentParameter.setValue(obj);
                return true;
            }
        } else {
            ArrayList<OsmPrimitive> multiValue = currentParameter.getValueList();
            if (obj instanceof Collection) {
                if (((Collection<?>) obj).size() > currentParameter.maxInstances && currentParameter.maxInstances != 0)
                    return false;
                multiValue.clear();
                multiValue.addAll((Collection<OsmPrimitive>) obj);
                return true;
            } else if (obj instanceof OsmPrimitive) {
                if (multiValue.size() < currentParameter.maxInstances || currentParameter.maxInstances == 0) {
                    if (isPair(obj, currentParameter)) {
                        multiValue.add((OsmPrimitive) obj);
                        return true;
                    } else if (nextParameter() && multiValue.size() > 0) {
                        return loadObject(obj);
                    }
                } else if (nextParameter()) {
                    return loadObject(obj);
                }
            } else if (obj instanceof String) {
                if (isPair(obj, currentParameter)) {
                    currentParameter.setValue(obj);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean nextParameter() {
        currentParameterNum++;
        return (currentParameterNum < parameters.size()) ? true : false;
    }

    public boolean hasNextParameter() {
        return ((currentParameterNum + 1) < parameters.size()) ? true : false;
    }

    public void resetLoading() {
        currentParameterNum = 0;
        for (Parameter parameter : parameters) {
            if (parameter.maxInstances != 1)
                parameter.getValueList().clear();
        }
    }

    private static boolean isPair(Object obj, Parameter parameter) {
        switch (parameter.type) {
        case POINT:
            if (obj instanceof String) {
                Pattern p = Pattern.compile("(-?\\d*\\.?\\d*,-?\\d*\\.?\\d*;?)*");
                Matcher m = p.matcher((String) obj);
                return m.matches();
            }
            break;
        case NODE:
            if (obj instanceof Node) return true;
            break;
        case WAY:
            if (obj instanceof Way) return true;
            break;
        case RELATION:
            if (obj instanceof Relation) return true;
            break;
        case ANY:
            if (obj instanceof Node || obj instanceof Way || obj instanceof Relation) return true;
            break;
        case LENGTH:
            if (obj instanceof String) {
                Pattern p = Pattern.compile("\\d*\\.?\\d*");
                Matcher m = p.matcher((String) obj);
                if (m.matches()) {
                    Float value = Float.parseFloat((String) obj);
                    if (parameter.minVal != 0 && value < parameter.minVal)
                        break;
                    if (parameter.maxVal != 0 && value > parameter.maxVal)
                        break;
                    return true;
                }
            }
            break;
        case NATURAL:
            if (obj instanceof String) {
                Pattern p = Pattern.compile("\\d*");
                Matcher m = p.matcher((String) obj);
                if (m.matches()) {
                    Integer value = Integer.parseInt((String) obj);
                    if (parameter.minVal != 0 && value < parameter.minVal)
                        break;
                    if (parameter.maxVal != 0 && value > parameter.maxVal)
                        break;
                    return true;
                }
            }
            break;
        case STRING:
            if (obj instanceof String) return true;
            break;
        case RELAY:
            if (obj instanceof String) {
                if (parameter.getRawValue() instanceof Relay) {
                    if (((Relay) parameter.getRawValue()).isCorrectValue((String) obj))
                        return true;
                }
            }
            break;
        case USERNAME:
            if (obj instanceof String) return true;
            break;
        case IMAGERYURL:
            if (obj instanceof String) return true;
            break;
        case IMAGERYOFFSET:
            if (obj instanceof String) return true;
            break;
        default:
            break;
        }
        return false;
    }

    public Collection<OsmPrimitive> getDepsObjects() {
        ArrayList<OsmPrimitive> depsObjects = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (!parameter.isOsm())
                continue;
            if (parameter.maxInstances == 1) {
                depsObjects.addAll(getDepsObjects(depsObjects, (OsmPrimitive) parameter.getRawValue()));
            } else {
                for (OsmPrimitive primitive : parameter.getValueList()) {
                    depsObjects.addAll(getDepsObjects(depsObjects, primitive));
                }
            }
        }
        return depsObjects;
    }

    public Collection<OsmPrimitive> getDepsObjects(Collection<OsmPrimitive> currentObjects, OsmPrimitive primitive) {
        ArrayList<OsmPrimitive> depsObjects = new ArrayList<>();
        if (!currentObjects.contains(primitive)) {
            if (primitive instanceof Way) {
                depsObjects.addAll(((Way) primitive).getNodes());
            } else if (primitive instanceof Relation) {
                Collection<OsmPrimitive> relationMembers = ((Relation) primitive).getMemberPrimitives();
                for (OsmPrimitive member : relationMembers) {
                    if (!currentObjects.contains(member)) {
                        depsObjects.add(member);
                        depsObjects.addAll(getDepsObjects(currentObjects, member));
                    }
                }
            }
        }
        return depsObjects;
    }
}
