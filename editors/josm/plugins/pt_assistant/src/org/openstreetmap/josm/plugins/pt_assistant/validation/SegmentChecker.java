package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SelectCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.GenericRelationEditor;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteDataManager;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteSegment;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.plugins.pt_assistant.gui.PTAssistantLayer;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;

/**
 * Performs tests of a route at the level of route segments (the stop-by-stop
 * approach).
 * 
 * @author darya
 *
 */
public class SegmentChecker extends Checker {

    /* PTRouteSegments that have been validated and are correct */
    private static List<PTRouteSegment> correctSegments = new ArrayList<PTRouteSegment>();

    /* PTRouteSegments that are wrong, stored in case the user calls the fix */
    private static HashMap<TestError, PTRouteSegment> wrongSegments = new HashMap<TestError, PTRouteSegment>();

    /* Manager of the PTStops and PTWays of the current route */
    private PTRouteDataManager manager;

    /* Assigns PTStops to nearest PTWays and stores that correspondence */
    private StopToWayAssigner assigner;

    private List<PTWay> unusedWays = new ArrayList<>();

    private HashMap<TestError, PTWay> erroneousPTWays = new HashMap<>();

    private HashMap<TestError, Node> firstNodeOfErroneousPTWay = new HashMap<>();

    public SegmentChecker(Relation relation, Test test) {

        super(relation, test);

        this.manager = new PTRouteDataManager(relation);

        for (RelationMember rm : manager.getFailedMembers()) {
            List<Relation> primitives = new ArrayList<>(1);
            primitives.add(relation);
            List<OsmPrimitive> highlighted = new ArrayList<>(1);
            highlighted.add(rm.getMember());
            TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Relation member roles do not match tags"),
                    PTAssistantValidatorTest.ERROR_CODE_RELAITON_MEMBER_ROLES, primitives, highlighted);
            this.errors.add(e);
        }

        this.assigner = new StopToWayAssigner(manager.getPTWays());

        unusedWays.addAll(manager.getPTWays());

    }

    /**
     * Returns the number of route segments that have been already successfully
     * verified
     * 
     * @return
     */
    public static int getCorrectSegmentCount() {
        return correctSegments.size();
    }

    /**
     * Adds the given correct segment to the list of correct segments without
     * checking its correctness
     * 
     * @param segment
     *            to add to the list of correct segments
     */
    public static void addCorrectSegment(PTRouteSegment segment) {
        for (PTRouteSegment correctSegment : correctSegments) {
            if (correctSegment.equalsRouteSegment(segment)) {
                return;
            }
        }
        correctSegments.add(segment);
    }

    public void performFirstStopTest() {

        performEndStopTest(manager.getFirstStop());

    }

    public void performLastStopTest() {

        performEndStopTest(manager.getLastStop());

    }

    private void performEndStopTest(PTStop endStop) {

        if (endStop == null) {
            return;
        }

        /*
         * This test checks: (1) that a stop position exists; (2) that it is the
         * first or last node of its parent ways which belong to this route.
         */

        if (endStop.getStopPosition() == null) {

            List<Node> potentialStopPositionList = endStop.findPotentialStopPositions();
            List<Node> stopPositionsOfThisRoute = new ArrayList<>();
            boolean containsAtLeastOneStopPositionAsFirstOrLastNode = false;

            for (Node potentialStopPosition : potentialStopPositionList) {

                int belongsToWay = belongsToAWayOfThisRoute(potentialStopPosition);

                if (belongsToWay == 0) {
                    stopPositionsOfThisRoute.add(potentialStopPosition);
                    containsAtLeastOneStopPositionAsFirstOrLastNode = true;
                }

                if (belongsToWay == 1) {
                    stopPositionsOfThisRoute.add(potentialStopPosition);
                }
            }

            if (stopPositionsOfThisRoute.isEmpty()) {
                List<Relation> primitives = new ArrayList<>(1);
                primitives.add(relation);
                List<OsmPrimitive> highlighted = new ArrayList<>(1);
                highlighted.add(endStop.getPlatform());
                TestError e = new TestError(this.test, Severity.WARNING,
                        tr("PT: Route should start and end with a stop_position"),
                        PTAssistantValidatorTest.ERROR_CODE_END_STOP, primitives, highlighted);
                this.errors.add(e);
                return;
            }

            if (stopPositionsOfThisRoute.size() == 1) {
                endStop.setStopPosition(stopPositionsOfThisRoute.get(0));
            }

            // At this point, there is at least one stop_position for this
            // endStop:
            if (!containsAtLeastOneStopPositionAsFirstOrLastNode) {
                List<Relation> primitives = new ArrayList<>(1);
                primitives.add(relation);
                List<OsmPrimitive> highlighted = new ArrayList<>();
                highlighted.addAll(stopPositionsOfThisRoute);

                TestError e = new TestError(this.test, Severity.WARNING, tr("PT: First or last way needs to be split"),
                        PTAssistantValidatorTest.ERROR_CODE_SPLIT_WAY, primitives, highlighted);
                this.errors.add(e);
            }

        } else {

            // if the stop_position is known:
            int belongsToWay = this.belongsToAWayOfThisRoute(endStop.getStopPosition());

            if (belongsToWay == 1) {

                List<Relation> primitives = new ArrayList<>(1);
                primitives.add(relation);
                List<OsmPrimitive> highlighted = new ArrayList<>();
                highlighted.add(endStop.getStopPosition());
                TestError e = new TestError(this.test, Severity.WARNING, tr("PT: First or last way needs to be split"),
                        PTAssistantValidatorTest.ERROR_CODE_SPLIT_WAY, primitives, highlighted);
                this.errors.add(e);
            }
        }

    }

    /**
     * Checks if the given node belongs to the ways of this route.
     * 
     * @param node
     *            Node to be checked
     * @return 1 if belongs only as an inner node, 0 if belongs as a first or
     *         last node for at least one way, -1 if does not belong to any way.
     */
    private int belongsToAWayOfThisRoute(Node node) {

        boolean contains = false;

        List<PTWay> ptways = manager.getPTWays();
        for (PTWay ptway : ptways) {
            List<Way> ways = ptway.getWays();
            for (Way way : ways) {
                if (way.containsNode(node)) {

                    if (way.firstNode().equals(node) || way.lastNode().equals(node)) {
                        return 0;
                    }

                    contains = true;
                }
            }
        }

        if (contains) {
            return 1;
        }

        return -1;
    }

    public void performStopNotServedTest() {
        for (PTStop stop : manager.getPTStops()) {
            Way way = assigner.get(stop);
            if (way == null) {
                createStopError(stop);
            }
        }
    }

    /**
     * Performs the stop-by-stop test by visiting each segment between two
     * consecutive stops and checking if the ways between them are correct
     */
    public void performStopByStopTest() {

        if (manager.getPTStopCount() < 2) {
            return;
        }

        // Check each route segment:
        for (int i = 1; i < manager.getPTStopCount(); i++) {

            PTStop startStop = manager.getPTStops().get(i - 1);
            PTStop endStop = manager.getPTStops().get(i);

            Way startWay = assigner.get(startStop);
            Way endWay = assigner.get(endStop);
            if (startWay == null || endWay == null) {
                continue;
            }

            List<PTWay> segmentWays = manager.getPTWaysBetween(startWay, endWay);

            Node firstNode = findFirstNodeOfRouteSegmentInDirectionOfTravel(segmentWays.get(0));
            if (firstNode == null) {
                // check if this error has just been reported:
                if (!this.errors.isEmpty() && this.errors.get(this.errors.size() - 1).getHighlighted().size() == 1
                        && this.errors.get(this.errors.size() - 1).getHighlighted().iterator().next() == startWay) {
                    // do nothing, this error has already been reported in
                    // the previous route segment
                } else {
                    List<Relation> primitives = new ArrayList<>(1);
                    primitives.add(relation);
                    List<OsmPrimitive> highlighted = new ArrayList<>();
                    highlighted.add(startWay);
                    TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Problem in the route segment"),
                            PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP, primitives, highlighted);
                    this.errors.add(e);
                    PTRouteSegment routeSegment = new PTRouteSegment(startStop, endStop, segmentWays);
                    wrongSegments.put(e, routeSegment);
                    erroneousPTWays.put(e, manager.getPTWay(startWay));
                    firstNodeOfErroneousPTWay.put(e, null);
                }
                continue;
            }

            boolean sortingCorrect = existingWaySortingIsCorrect(segmentWays.get(0), firstNode,
                    segmentWays.get(segmentWays.size() - 1));
            if (sortingCorrect) {
                PTRouteSegment routeSegment = new PTRouteSegment(startStop, endStop, segmentWays);
                addCorrectSegment(routeSegment);
                unusedWays.removeAll(segmentWays);
            } else {
                PTRouteSegment routeSegment = new PTRouteSegment(startStop, endStop, segmentWays);
                TestError error = this.errors.get(this.errors.size() - 1);
                wrongSegments.put(error, routeSegment);
            }
        }
    }

    /**
     * Creates a TestError and adds it to the list of errors for a stop that is
     * not served.
     * 
     * @param stop
     */
    private void createStopError(PTStop stop) {
        List<Relation> primitives = new ArrayList<>(1);
        primitives.add(relation);
        List<OsmPrimitive> highlighted = new ArrayList<>();
        OsmPrimitive stopPrimitive = stop.getPlatform();
        if (stopPrimitive == null) {
            stopPrimitive = stop.getStopPosition();
        }
        highlighted.add(stopPrimitive);
        TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop not served"),
                PTAssistantValidatorTest.ERROR_CODE_STOP_NOT_SERVED, primitives, highlighted);
        this.errors.add(e);
    }

    private Node findFirstNodeOfRouteSegmentInDirectionOfTravel(PTWay startWay) {

        // 1) at first check if one of the first or last node of the first ptway
        // is a deadend node:
        Node[] startWayEndnodes = startWay.getEndNodes();
        if (isDeadendNode(startWayEndnodes[0])) {
            return startWayEndnodes[0];
        }
        if (isDeadendNode(startWayEndnodes[1])) {
            return startWayEndnodes[1];
        }

        // 2) failing that, check which node this startWay shares with the
        // following way:
        PTWay nextWay = manager.getNextPTWay(startWay);
        if (nextWay == null) {
            return null;
        }
        PTWay wayAfterNext = manager.getNextPTWay(nextWay);
        Node[] nextWayEndnodes = nextWay.getEndNodes();
        if ((startWayEndnodes[0] == nextWayEndnodes[0] && startWayEndnodes[1] == nextWayEndnodes[1])
                || (startWayEndnodes[0] == nextWayEndnodes[1] && startWayEndnodes[1] == nextWayEndnodes[0])) {
            // if this is a split roundabout:
            Node[] wayAfterNextEndnodes = wayAfterNext.getEndNodes();
            if (startWayEndnodes[0] == wayAfterNextEndnodes[0] || startWayEndnodes[0] == wayAfterNextEndnodes[1]) {
                return startWayEndnodes[0];
            }
            if (startWayEndnodes[1] == wayAfterNextEndnodes[0] || startWayEndnodes[1] == wayAfterNextEndnodes[1]) {
                return startWayEndnodes[1];
            }
        }

        if (startWayEndnodes[0] == nextWayEndnodes[0] || startWayEndnodes[0] == nextWayEndnodes[1]) {
            return startWayEndnodes[1];
        }
        if (startWayEndnodes[1] == nextWayEndnodes[0] || startWayEndnodes[1] == nextWayEndnodes[1]) {
            return startWayEndnodes[0];
        }

        return null;

    }

    private boolean isDeadendNode(Node node) {
        int count = 0;
        for (PTWay ptway : manager.getPTWays()) {
            List<Way> ways = ptway.getWays();
            for (Way way : ways) {
                if (way.firstNode() == node || way.lastNode() == node) {
                    count++;
                }
            }
        }
        return count == 1;
    }

    /**
     * Finds the deadend node closest to the given node represented by its
     * coordinates
     * 
     * @param coord
     *            coordinates of the givenn node
     * @param deadendNodes
     * @return the closest deadend node
     */
    @SuppressWarnings("unused")
    private Node findClosestDeadendNode(LatLon coord, List<Node> deadendNodes) {

        Node closestDeadendNode = null;
        double minSqDistance = Double.MAX_VALUE;
        for (Node deadendNode : deadendNodes) {
            double distanceSq = coord.distanceSq(deadendNode.getCoor());
            if (distanceSq < minSqDistance) {
                minSqDistance = distanceSq;
                closestDeadendNode = deadendNode;
            }
        }
        return closestDeadendNode;

    }

    /**
     * Checks if the existing sorting of the given route segment is correct
     * 
     * @param start
     *            PTWay assigned to the first stop of the segment
     * @param startWayPreviousNodeInDirectionOfTravel
     *            Node if the start way which is furthest away from the rest of
     *            the route
     * @param end
     *            PTWay assigned to the end stop of the segment
     * @return true if the sorting is correct, false otherwise.
     */
    private boolean existingWaySortingIsCorrect(PTWay start, Node startWayPreviousNodeInDirectionOfTravel, PTWay end) {

        if (start == end) {
            // if both PTStops are on the same PTWay
            return true;
        }

        PTWay current = start;
        Node currentNode = startWayPreviousNodeInDirectionOfTravel;

        while (!current.equals(end)) {
            // "equals" is used here instead of "==" because when the same way
            // is passed multiple times by the bus, the algorithm should stop no
            // matter which of the geometrically equal PTWays it finds

            PTWay nextPTWayAccortingToExistingSorting = manager.getNextPTWay(current);

            // if current contains an unsplit roundabout:
            if (current.containsUnsplitRoundabout()) {
                currentNode = manager.getCommonNode(current, nextPTWayAccortingToExistingSorting);
                if (currentNode == null) {
                    List<Relation> primitives = new ArrayList<>(1);
                    primitives.add(relation);
                    List<OsmPrimitive> highlighted = new ArrayList<>();
                    highlighted.addAll(current.getWays());
                    TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Problem in the route segment"),
                            PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP, primitives, highlighted);
                    this.errors.add(e);
                    erroneousPTWays.put(e, current);
                    return false;
                }
            } else {
                // if this is a regular way, not an unsplit roundabout

                // find the next node in direction of travel (which is part of
                // the PTWay start):
                currentNode = getOppositeEndNode(current, currentNode);

                List<PTWay> nextWaysInDirectionOfTravel = this.findNextPTWaysInDirectionOfTravel(current, currentNode);

                if (!nextWaysInDirectionOfTravel.contains(nextPTWayAccortingToExistingSorting)) {
                    List<Relation> primitives = new ArrayList<>(1);
                    primitives.add(relation);
                    List<OsmPrimitive> highlighted = new ArrayList<>();

                    highlighted.addAll(current.getWays());

                    TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Problem in the route segment"),
                            PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP, primitives, highlighted);
                    this.errors.add(e);
                    return false;

                }
            }

            current = nextPTWayAccortingToExistingSorting;

        }

        return true;
    }

    /**
     * Will return the same node if the way is an unsplit roundabout
     * 
     * @param way
     * @param node
     * @return
     */
    private Node getOppositeEndNode(Way way, Node node) {

        if (node == way.firstNode()) {
            return way.lastNode();
        }

        if (node == way.lastNode()) {
            return way.firstNode();
        }

        return null;
    }

    /**
     * Does not work correctly for unsplit roundabouts
     * 
     * @param ptway
     * @param node
     * @return
     */
    private Node getOppositeEndNode(PTWay ptway, Node node) {
        if (ptway.isWay()) {
            return getOppositeEndNode(ptway.getWays().get(0), node);
        }

        Way firstWay = ptway.getWays().get(0);
        Way lastWay = ptway.getWays().get(ptway.getWays().size() - 1);
        Node oppositeNode = node;
        if (firstWay.firstNode() == node || firstWay.lastNode() == node) {
            for (int i = 0; i < ptway.getWays().size(); i++) {
                oppositeNode = getOppositeEndNode(ptway.getWays().get(i), oppositeNode);
            }
            return oppositeNode;
        } else if (lastWay.firstNode() == node || lastWay.lastNode() == node) {
            for (int i = ptway.getWays().size() - 1; i >= 0; i--) {
                oppositeNode = getOppositeEndNode(ptway.getWays().get(i), oppositeNode);
            }
            return oppositeNode;
        }

        return null;

    }

    /**
     * 
     * @param way
     * @param nodeInDirectionOfTravel
     * @return
     */
    private List<PTWay> findNextPTWaysInDirectionOfTravel(PTWay currentWay, Node nextNodeInDirectionOfTravel) {

        List<PTWay> nextPtways = new ArrayList<>();

        List<PTWay> ptways = manager.getPTWays();

        for (PTWay ptway : ptways) {

            if (ptway != currentWay) {
                for (Way way : ptway.getWays()) {
                    if (way.containsNode(nextNodeInDirectionOfTravel)) {
                        nextPtways.add(ptway);
                    }
                }
            }
        }

        return nextPtways;

    }

    protected static boolean isFixable(TestError testError) {

        /*-
         * When is an error fixable?
         * - if there is a correct segment
         * - if it can be fixed by sorting
         * - if the route is compete even without some ways
         * - if simple routing closes the gap
         */

        if (testError.getCode() == PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP) {
            return true;
        }

        return false;

    }

    @SuppressWarnings("unused")
    private static boolean isFixableByUsingCorrectSegment(TestError testError) {
        PTRouteSegment wrongSegment = wrongSegments.get(testError);
        PTRouteSegment correctSegment = null;
        // TODO: now just the first correctSegment is taken over. Change
        // that the segment is selected.
        for (PTRouteSegment segment : correctSegments) {
            if (wrongSegment.getFirstStop().equalsStop(segment.getFirstStop())
                    && wrongSegment.getLastStop().equalsStop(segment.getLastStop())) {
                correctSegment = segment;
                break;
            }
        }
        return correctSegment != null;
    }

    @SuppressWarnings("unused")
    private static boolean isFixableBySortingAndRemoval(TestError testError) {
        PTRouteSegment wrongSegment = wrongSegments.get(testError);
        List<List<PTWay>> fixVariants = wrongSegment.getFixVariants();
        if (!fixVariants.isEmpty()) {
            return true;
        }
        return false;
    }

    protected void findFixes() {
        for (TestError error : wrongSegments.keySet()) {
            findFix(error);
        }
    }

    /**
     * This method assumes that the first and the second ways of the route
     * segment are correctly connected. If they are not, the error will be
     * marked as not fixable.
     * 
     * @param testError
     */
    private void findFix(TestError testError) {

        PTRouteSegment wrongSegment = wrongSegments.get(testError);
        PTWay startPTWay = wrongSegment.getFirstPTWay();
        PTWay endPTWay = wrongSegment.getLastPTWay();

        Node previousNode = findFirstNodeOfRouteSegmentInDirectionOfTravel(startPTWay);
        if (previousNode == null) {
            // TODO: sort route ways
            return;
        }

        List<List<PTWay>> initialFixes = new ArrayList<>();
        List<PTWay> initialFix = new ArrayList<>();
        initialFix.add(startPTWay);
        initialFixes.add(initialFix);

        List<List<PTWay>> allFixes = findWaysForFix(initialFixes, initialFix, previousNode, endPTWay);
        for (List<PTWay> fix : allFixes) {
            if (!fix.isEmpty() && fix.get(fix.size() - 1).equals(endPTWay)) {
                wrongSegment.addFixVariant(fix);
            }
        }

    }

    /**
     * Recursive method to parse the route segment
     * 
     * @param allFixes
     * @param currentFix
     * @param previousNode
     * @param endWay
     * @return
     */
    private List<List<PTWay>> findWaysForFix(List<List<PTWay>> allFixes, List<PTWay> currentFix, Node previousNode,
            PTWay endWay) {

        PTWay currentWay = currentFix.get(currentFix.size() - 1);
        Node nextNode = getOppositeEndNode(currentWay, previousNode);

        List<PTWay> nextWays = this.findNextPTWaysInDirectionOfTravel(currentWay, nextNode);

        if (nextWays.size() > 1) {
            for (int i = 1; i < nextWays.size(); i++) {
                List<PTWay> newFix = new ArrayList<>();
                newFix.addAll(currentFix);
                newFix.add(nextWays.get(i));
                allFixes.add(newFix);
                if (!nextWays.get(i).equals(endWay) && !currentFix.contains(nextWays.get(i))) {
                    allFixes = findWaysForFix(allFixes, newFix, nextNode, endWay);
                }
            }
        }

        if (!nextWays.isEmpty()) {
            boolean contains = currentFix.contains(nextWays.get(0));
            currentFix.add(nextWays.get(0));
            if (!nextWays.get(0).equals(endWay) && !contains) {
                allFixes = findWaysForFix(allFixes, currentFix, nextNode, endWay);
            }
        }

        return allFixes;
    }

    /**
     * Fixes the error by first searching in the list of correct segments and
     * then trying to sort and remove existing route relation members
     * 
     * @param testError
     * @return
     */
    protected static Command fixError(TestError testError) {

        PTRouteSegment wrongSegment = wrongSegments.get(testError);

        // 1) try to fix by using the correct segment:
        List<PTRouteSegment> correctSegmentsForThisError = new ArrayList<>();
        for (PTRouteSegment segment : correctSegments) {
            if (wrongSegment.getFirstStop().equalsStop(segment.getFirstStop())
                    && wrongSegment.getLastStop().equalsStop(segment.getLastStop())) {
                correctSegmentsForThisError.add(segment);
            }
        }

        if (!correctSegmentsForThisError.isEmpty()) {

            List<PTWay> fix = null;

            if (correctSegmentsForThisError.size() > 1) {
                fix = displayCorrectSegmentVariants(correctSegmentsForThisError, testError);
                if (fix == null) {
                    return null;
                }
            } else {
                fix = correctSegmentsForThisError.get(0).getPTWays();
                final Collection<OsmPrimitive> waysToZoom = new ArrayList<>();
                for (Object highlightedPrimitive: testError.getHighlighted()) {
                    waysToZoom.add((OsmPrimitive)highlightedPrimitive);
                }
                if (SwingUtilities.isEventDispatchThread()) {
                    AutoScaleAction.zoomTo(waysToZoom);
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            AutoScaleAction.zoomTo(waysToZoom);
                        }
                    });
                }
                synchronized(SegmentChecker.class) {
                    try {
                        SegmentChecker.class.wait(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            Relation originalRelation = (Relation) testError.getPrimitives().iterator().next();
            Relation modifiedRelation = new Relation(originalRelation);
            List<RelationMember> originalRelationMembers = originalRelation.getMembers();
            List<RelationMember> modifiedRelationMembers = new ArrayList<>();

            // copy stops first:
            for (RelationMember rm : originalRelationMembers) {
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

            // copy PTWays next:
            List<RelationMember> waysOfOriginalRelation = new ArrayList<>();
            for (RelationMember rm : originalRelation.getMembers()) {
                if (RouteUtils.isPTWay(rm)) {
                    waysOfOriginalRelation.add(rm);
                }
            }

            for (int i = 0; i < waysOfOriginalRelation.size(); i++) {
                if (waysOfOriginalRelation.get(i).getWay() == wrongSegment.getPTWays().get(0).getWays().get(0)) {
                    for (PTWay ptway : fix) {
                        if (ptway.getRole().equals("forward") || ptway.getRole().equals("backward")) {
                            modifiedRelationMembers.add(new RelationMember("", ptway.getMember()));
                        } else {
                            modifiedRelationMembers.add(ptway);
                        }
                    }
                    i = i + wrongSegment.getPTWays().size() - 1;
                } else {
                    if (waysOfOriginalRelation.get(i).getRole().equals("forward")
                            || waysOfOriginalRelation.get(i).getRole().equals("backward")) {
                        modifiedRelationMembers.add(new RelationMember("", waysOfOriginalRelation.get(i).getMember()));
                    } else {
                        modifiedRelationMembers.add(waysOfOriginalRelation.get(i));
                    }
                }
            }

            modifiedRelation.setMembers(modifiedRelationMembers);
            // TODO: change the data model too
            wrongSegments.remove(testError);
            ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);
            return changeCommand;

        } else if (!wrongSegment.getFixVariants().isEmpty()) {

            List<PTWay> fix = null;

            if (wrongSegment.getFixVariants().size() > 1) {
                fix = displayFixVariants(wrongSegment.getFixVariants(), testError);
                if (fix == null) {
                    return null;
                }
            } else {
                fix = wrongSegment.getFixVariants().get(0);
                final Collection<OsmPrimitive> waysToZoom = new ArrayList<>();
                for (Object highlightedPrimitive: testError.getHighlighted()) {
                    waysToZoom.add((OsmPrimitive)highlightedPrimitive);
                }
                if (SwingUtilities.isEventDispatchThread()) {
                    AutoScaleAction.zoomTo(waysToZoom);
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            AutoScaleAction.zoomTo(waysToZoom);
                        }
                    });
                }
                synchronized(SegmentChecker.class) {
                    try {
                        SegmentChecker.class.wait(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            // 2) try to fix by using the sort & remove method:
            // TODO: ask user if the change should be undertaken
            Relation originalRelation = (Relation) testError.getPrimitives().iterator().next();
            Relation modifiedRelation = new Relation(originalRelation);
            List<RelationMember> originalRelationMembers = originalRelation.getMembers();
            List<RelationMember> modifiedRelationMembers = new ArrayList<>();

            // copy stops first:
            for (RelationMember rm : originalRelationMembers) {
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

            // copy PTWays next:
            List<RelationMember> waysOfOriginalRelation = new ArrayList<>();
            for (RelationMember rm : originalRelation.getMembers()) {
                if (RouteUtils.isPTWay(rm)) {
                    waysOfOriginalRelation.add(rm);
                }
            }

            for (int i = 0; i < waysOfOriginalRelation.size(); i++) {
                if (waysOfOriginalRelation.get(i).getWay() == wrongSegment.getPTWays().get(0).getWays().get(0)) {
                    for (PTWay ptway : fix) {
                        if (ptway.getRole().equals("forward") || ptway.getRole().equals("backward")) {
                            modifiedRelationMembers.add(new RelationMember("", ptway.getMember()));
                        } else {
                            modifiedRelationMembers.add(ptway);
                        }
                    }
                    i = i + wrongSegment.getPTWays().size() - 1;
                } else {
                    if (waysOfOriginalRelation.get(i).getRole().equals("forward")
                            || waysOfOriginalRelation.get(i).getRole().equals("backward")) {
                        modifiedRelationMembers.add(new RelationMember("", waysOfOriginalRelation.get(i).getMember()));
                    } else {
                        modifiedRelationMembers.add(waysOfOriginalRelation.get(i));
                    }
                }
            }

            modifiedRelation.setMembers(modifiedRelationMembers);
            // TODO: change the data model too
            wrongSegments.remove(testError);
            wrongSegment.setPTWays(wrongSegment.getFixVariants().get(0));
            addCorrectSegment(wrongSegment);
            ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);
            return changeCommand;

        }

        // if there is no fix:
        return fixErrorByZooming(testError);

    }

    /**
     * 
     * @param segments
     */
    private static List<PTWay> displayCorrectSegmentVariants(List<PTRouteSegment> segments, TestError testError) {
        List<List<PTWay>> fixVariantList = new ArrayList<>();
        for (PTRouteSegment segment : segments) {
            fixVariantList.add(segment.getPTWays());
        }
        return displayFixVariants(fixVariantList, testError);
    }

    /**
     * 
     * @param fixVariants
     */
    private static List<PTWay> displayFixVariants(List<List<PTWay>> fixVariants, TestError testError) {
        // find the letters of the fix variants:
        char alphabet = 'A';
        List<Character> allowedCharacters = new ArrayList<>();
        for (int i = 0; i < fixVariants.size(); i++) {
            allowedCharacters.add(alphabet);
            alphabet++;
        }

        // zoom to problem:
        final Collection<OsmPrimitive> waysToZoom = new ArrayList<>();
//		for (List<PTWay> fix : fixVariants) {
//			for (PTWay ptway : fix) {
//				waysToZoom.addAll(ptway.getWays());
//			}
//		}
        for (Object highlightedPrimitive: testError.getHighlighted()) {
            waysToZoom.add((OsmPrimitive)highlightedPrimitive);
        }
        if (SwingUtilities.isEventDispatchThread()) {
            AutoScaleAction.zoomTo(waysToZoom);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AutoScaleAction.zoomTo(waysToZoom);
                }
            });
        }

        // display the fix variants:
        PTAssistantValidatorTest test = (PTAssistantValidatorTest) testError.getTester();
        test.addFixVariants(fixVariants);
        PTAssistantLayer.getLayer().repaint((Relation) testError.getPrimitives().iterator().next());

        // get user input:
        Character userInput = getUserInput(allowedCharacters);
        if (userInput == null) {
            test.clearFixVariants();
            return null;
        }
        List<PTWay> selectedFix = test.getFixVariant(userInput);
        test.clearFixVariants();
        return selectedFix;
    }

    /**
     * Asks user to choose the fix variant and returns the choice
     * 
     * @param allowedCharacters
     * @return
     */
    private static Character getUserInput(List<Character> allowedCharacters) {
        final String[] userInput = { "" };

        while (userInput[0] == null || userInput[0].length() != 1 || userInput[0].equals("")
                || !allowedCharacters.contains(userInput[0].toUpperCase().toCharArray()[0])) {
            if (SwingUtilities.isEventDispatchThread()) {

                userInput[0] = JOptionPane.showInputDialog("Enter a letter to select the fix variant: ");

            } else {

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {

                            userInput[0] = JOptionPane.showInputDialog("Enter a letter to select the fix variant: ");

                        }
                    });
                } catch (InvocationTargetException | InterruptedException e1) {
                    break;
                }

            }
            if (userInput[0] == null) {
                break;
            }
        }

        if (userInput[0] == null) {
            return null;
        }
        return userInput[0].toCharArray()[0];

    }

    /**
     * 
     * @param testError
     * @return
     */
    protected static Command fixErrorByZooming(TestError testError) {

        if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP) {
            return null;
        }

        Collection<? extends OsmPrimitive> primitives = testError.getPrimitives();
        Relation originalRelation = (Relation) primitives.iterator().next();
        ArrayList<OsmPrimitive> primitivesToZoom = new ArrayList<>();
        for (Object primitiveToZoom : testError.getHighlighted()) {
            primitivesToZoom.add((OsmPrimitive) primitiveToZoom);
        }

        SelectCommand command = new SelectCommand(primitivesToZoom);

        List<OsmDataLayer> listOfLayers = Main.getLayerManager().getLayersOfType(OsmDataLayer.class);
        for (OsmDataLayer osmDataLayer : listOfLayers) {
            if (osmDataLayer.data == originalRelation.getDataSet()) {

                final OsmDataLayer layerParameter = osmDataLayer;
                final Relation relationParameter = originalRelation;
                final Collection<OsmPrimitive> zoomParameter = primitivesToZoom;

                if (SwingUtilities.isEventDispatchThread()) {

                    showRelationEditorAndZoom(layerParameter, relationParameter, zoomParameter);

                } else {

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {

                            showRelationEditorAndZoom(layerParameter, relationParameter, zoomParameter);

                        }
                    });

                }

                return command;
            }
        }

        return null;

    }

    private static void showRelationEditorAndZoom(OsmDataLayer layer, Relation r, Collection<OsmPrimitive> primitives) {

        // zoom to problem:
        AutoScaleAction.zoomTo(primitives);

        // put stop-related members to the front and edit roles if necessary:
        List<RelationMember> sortedRelationMembers = listStopMembers(r);
        sortedRelationMembers.addAll(listNotStopMembers(r));
        r.setMembers(sortedRelationMembers);

        // create editor:
        GenericRelationEditor editor = (GenericRelationEditor) RelationEditor.getEditor(layer, r,
                r.getMembersFor(primitives));

        // open editor:
        editor.setVisible(true);

        // make the current relation purple in the pt_assistant layer:
        PTAssistantLayer.getLayer().repaint(r);

    }

}