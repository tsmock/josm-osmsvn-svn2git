package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SelectCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.OsmUtils;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.GenericRelationEditor;
import org.openstreetmap.josm.gui.dialogs.relation.RelationDialogManager;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Performs tests of a route at the level of single ways: DirectionTest and RoadTypeTest
 * 
 * @author darya
 *
 */
public class WayChecker extends Checker {

    public WayChecker(Relation relation, Test test) {

        super(relation, test);

    }

    protected void performRoadTypeTest() {
        
        if (!relation.hasTag("route", "bus") && !relation.hasTag("route", "trolleybus")
                && !relation.hasTag("route", "share_taxi")) {
            return;
        }

        for (RelationMember rm : relation.getMembers()) {
            if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {

                Way way = rm.getWay();
                // at this point, the relation has already been checked to
                // be a route of public_transport:version 2

                boolean isCorrectRoadType = true;
                boolean isUnderConstruction = false;
                if (relation.hasTag("route", "bus") || relation.hasTag("route", "share_taxi")) {
                    if (!isWaySuitableForBuses(way)) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("highway", "construction") && way.hasKey("construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "trolleybus")) {
                    if (!(isWaySuitableForBuses(way) && way.hasTag("trolley_wire", "yes"))) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("highway", "construction") && way.hasKey("construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "tram")) {
                    if (!way.hasTag("railway", "tram")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "subway")) {
                    if (!relation.hasTag("railway", "subway")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "light_rail")) {
                    if (!relation.hasTag("raiilway", "subway")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "light_rail")) {
                    if (!relation.hasTag("railway", "light_rail")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "train")) {
                    if (!relation.hasTag("railway", "train")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
                        isUnderConstruction = true;
                    }
                }

                if (!isCorrectRoadType && !isUnderConstruction) {

                    List<Relation> primitives = new ArrayList<>(1);
                    primitives.add(relation);
                    List<Way> highlighted = new ArrayList<>(1);
                    highlighted.add(way);
                    TestError e = new TestError(this.test, Severity.WARNING,
                            tr("PT: Route type does not match the type of the road it passes on"),
                            PTAssitantValidatorTest.ERROR_CODE_ROAD_TYPE, primitives, highlighted);
                    errors.add(e);

                }
                
                if (isUnderConstruction) {
                    List<Relation> primitives = new ArrayList<>(1);
                    primitives.add(relation);
                    List<Way> highlighted = new ArrayList<>(1);
                    highlighted.add(way);
                    TestError e = new TestError(this.test, Severity.WARNING,
                            tr("PT: Road is under construction"),
                            PTAssitantValidatorTest.ERROR_CODE_CONSTRUCTION, primitives, highlighted);
                    errors.add(e);
                }
            }
        }

    }

    protected void performDirectionTest() {

        List<RelationMember> waysToCheck = new ArrayList<>();

        for (RelationMember rm : relation.getMembers()) {
            if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {
                waysToCheck.add(rm);
            }
        }

        if (waysToCheck.isEmpty()) {
            return;
        }

        WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
        final List<WayConnectionType> links = connectionTypeCalculator.updateLinks(waysToCheck);

        for (int i = 0; i < links.size(); i++) {
            if ((OsmUtils.isTrue(waysToCheck.get(i).getWay().get("oneway"))
                    && links.get(i).direction.equals(WayConnectionType.Direction.BACKWARD))
                    || (OsmUtils.isReversed(waysToCheck.get(i).getWay().get("oneway"))
                            && links.get(i).direction.equals(WayConnectionType.Direction.FORWARD))) {

                // At this point, the PTWay is going against the oneway
                // direction. Check if this road allows buses to disregard
                // the oneway restriction:

                if (!waysToCheck.get(i).getWay().hasTag("busway", "lane")
                        && !waysToCheck.get(i).getWay().hasTag("busway:left", "lane")
                        && !waysToCheck.get(i).getWay().hasTag("busway:right", "lane")
                        && !waysToCheck.get(i).getWay().hasTag("oneway:bus", "no")
                        && !waysToCheck.get(i).getWay().hasTag("busway", "opposite_lane")
                        && !waysToCheck.get(i).getWay().hasTag("oneway:psv", "no")
                        && !waysToCheck.get(i).getWay().hasTag("trolley_wire", "backward")) {
                    List<Relation> primitives = new ArrayList<>(1);
                    primitives.add(relation);
                    List<Way> highlighted = new ArrayList<>(1);
                    highlighted.add(waysToCheck.get(i).getWay());
                    TestError e = new TestError(this.test, Severity.WARNING,
                            tr("PT: Route passes a oneway road in the wrong direction"),
                            PTAssitantValidatorTest.ERROR_CODE_DIRECTION, primitives, highlighted);
                    this.errors.add(e);
                    return;
                }

            }
        }

    }

    /**
     * Checks if the type of the way is suitable for buses to go on it. The
     * direction of the way (i.e. one-way roads) is irrelevant for this test.
     * 
     * @param way
     *            to be checked
     * @return true if the way is suitable for buses, false otherwise.
     */
    private boolean isWaySuitableForBuses(Way way) {
        if (way.hasTag("highway", "motorway") || way.hasTag("highway", "trunk") || way.hasTag("highway", "primary")
                || way.hasTag("highway", "secondary") || way.hasTag("highway", "tertiary")
                || way.hasTag("highway", "unclassified") || way.hasTag("highway", "road")
                || way.hasTag("highway", "residential") || way.hasTag("highway", "service")
                || way.hasTag("highway", "motorway_link") || way.hasTag("highway", "trunk_link")
                || way.hasTag("highway", "primary_link") || way.hasTag("highway", "secondary_link")
                || way.hasTag("highway", "tertiary_link") || way.hasTag("highway", "living_street")
                || way.hasTag("highway", "bus_guideway") || way.hasTag("highway", "road")
                || way.hasTag("cycleway", "share_busway") || way.hasTag("cycleway", "shared_lane")) {
            return true;
        }

        if (way.hasTag("highway", "pedestrian") && (way.hasTag("bus", "yes") || way.hasTag("psv", "yes")
                || way.hasTag("bus", "designated") || way.hasTag("psv", "designated"))) {
            return true;
        }

        return false;
    }
    
    protected static Command fixErrorByRemovingWay(TestError testError) {

        if (testError.getCode() != PTAssitantValidatorTest.ERROR_CODE_ROAD_TYPE && testError.getCode() != PTAssitantValidatorTest.ERROR_CODE_DIRECTION) {
            return null;
        }

        Collection<? extends OsmPrimitive> primitives = testError.getPrimitives();
        Relation originalRelation = (Relation) primitives.iterator().next();
        Collection<?> highlighted = testError.getHighlighted();
        Way wayToRemove = (Way) highlighted.iterator().next();

        Relation modifiedRelation = new Relation(originalRelation);
        List<RelationMember> modifiedRelationMembers = new ArrayList<>(originalRelation.getMembersCount() - 1);

        // copy PT stops first, PT ways last:
        for (RelationMember rm : originalRelation.getMembers()) {
            if (RouteUtils.isPTStop(rm)) {

                if (rm.getRole().equals("stop_position")) {
                    if (rm.getType().equals(OsmPrimitiveType.NODE)) {
                        RelationMember newMember = new RelationMember("stop", rm.getNode());
                        modifiedRelationMembers.add(newMember);
                    } else { // if it is a way:
                        RelationMember newMember = new RelationMember("stop", rm.getWay());
                        modifiedRelationMembers.add(newMember);
                    }
                } else {
                    // if the relation member does not have the role
                    // "stop_position":
                    modifiedRelationMembers.add(rm);
                }

            }
        }

        // now copy PT ways:
        for (RelationMember rm : originalRelation.getMembers()) {
            if (RouteUtils.isPTWay(rm)) {
                Way wayToCheck = rm.getWay();
                if (wayToCheck != wayToRemove) {
                    if (rm.getRole().equals("forward") || rm.getRole().equals("backward")) {
                        RelationMember modifiedMember = new RelationMember("", wayToCheck);
                        modifiedRelationMembers.add(modifiedMember);
                    } else {
                        modifiedRelationMembers.add(rm);
                    }
                }
            }
        }

        modifiedRelation.setMembers(modifiedRelationMembers);

        ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);

        return changeCommand;
    }
    

    protected static Command fixErrorByZooming(TestError testError) {
        
        if (testError.getCode() != PTAssitantValidatorTest.ERROR_CODE_DIRECTION) {
            return null;
        }
        
         long startTime = System.currentTimeMillis();
        
        Collection<? extends OsmPrimitive> primitives = testError.getPrimitives();
        Relation originalRelation = (Relation) primitives.iterator().next();
        Collection<?> highlighted = testError.getHighlighted();
        Way wayToHighlight = (Way) highlighted.iterator().next();
        ArrayList<OsmPrimitive> primitivesToHighlight = new ArrayList<>(1);
        primitivesToHighlight.add(wayToHighlight);
        
        long endTime = System.currentTimeMillis();
        System.out.println("obtaining primitives: " + (endTime - startTime));
        startTime = endTime;
        
        SelectCommand command = new SelectCommand(primitivesToHighlight);
        
        endTime = System.currentTimeMillis();
        System.out.println("constructing command: " + (endTime - startTime));
        startTime = endTime;
        
        AutoScaleAction.zoomTo(primitivesToHighlight);
        
        endTime = System.currentTimeMillis();
        System.out.println("AutoScaleAction done: " + (endTime - startTime));
        startTime = endTime;
        
        List<OsmDataLayer> listOfLayers = Main.getLayerManager().getLayersOfType(OsmDataLayer.class);
        for (OsmDataLayer osmDataLayer: listOfLayers) {
            if (osmDataLayer.data == originalRelation.getDataSet()) {
                endTime = System.currentTimeMillis();
                System.out.println("getting to the necessary layer: " + (endTime - startTime));
                startTime = endTime;
                
                GenericRelationEditor editor = new GenericRelationEditor(osmDataLayer, originalRelation, originalRelation.getMembersFor(primitivesToHighlight));
                endTime = System.currentTimeMillis();
                System.out.println("creating relation editor: " + (endTime - startTime));
                startTime = endTime;
                
                RelationDialogManager.getRelationDialogManager().register(osmDataLayer, originalRelation, editor);
                
                endTime = System.currentTimeMillis();
                System.out.println("registering relation editor: " + (endTime - startTime));
                startTime = endTime;
                
                editor.setVisible(true);
                
                endTime = System.currentTimeMillis();
                System.out.println("setting editor visible: " + (endTime - startTime));
                startTime = endTime;
                
                break;
            }
        }
        
        endTime = System.currentTimeMillis();
        System.out.println("whatever else: " + (endTime - startTime));
        startTime = endTime;
                        
        return command;
        
    }
    
}
