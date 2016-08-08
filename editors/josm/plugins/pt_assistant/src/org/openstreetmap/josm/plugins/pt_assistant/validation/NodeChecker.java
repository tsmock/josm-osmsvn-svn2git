package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SelectCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopUtils;

public class NodeChecker extends Checker {

	protected NodeChecker(Node node, Test test) {
		super(node, test);

	}

	/**
	 * Checks if the given stop_position node belongs to any way
	 * 
	 * @param n
	 */
	protected void performSolitaryStopPositionTest() {

		List<OsmPrimitive> referrers = node.getReferrers();

		for (OsmPrimitive referrer : referrers) {
			if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
				Way referrerWay = (Way) referrer;
				if (RouteUtils.isWaySuitableForPublicTransport(referrerWay)) {
					return;
				}

			}
		}

		List<OsmPrimitive> primitives = new ArrayList<>(1);
		primitives.add(node);
		TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop_position is not part of a way"),
				PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION, primitives);
		errors.add(e);

	}

	/**
	 * Checks if the given platform node belongs to any way
	 * 
	 * @param n
	 */
	protected void performPlatformPartOfWayTest() {

		List<OsmPrimitive> referrers = node.getReferrers();

		for (OsmPrimitive referrer : referrers) {
			List<Node> primitives = new ArrayList<>(1);
			primitives.add(node);
			if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
				Way referringWay = (Way) referrer;
				if (RouteUtils.isWaySuitableForPublicTransport(referringWay)) {
					TestError e = new TestError(this.test, Severity.WARNING,
							tr("PT: Platform should not be part of a way"),
							PTAssistantValidatorTest.ERROR_CODE_PLATFORM_PART_OF_HIGHWAY, primitives);
					errors.add(e);
					return;
				}
			}
		}
	}
	
	/**
	 * Checks if the given stop_position node belongs to any stop_area relation
	 * 
	 * @param n
	 */
	protected void performNodePartOfStopAreaTest() {
				
		if (!StopUtils.verifyIfMemberOfStopArea(node)) {
		
			List<OsmPrimitive> primitives = new ArrayList<>(1);
			primitives.add(node);
			TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop position or platform is not part of a stop area relation"),
					PTAssistantValidatorTest.ERROR_CODE_NODE_PART_OF_STOP_AREA, primitives);
			errors.add(e);
		}
	}
	
	/**
	 * Checks if the given stop_position belongs to the same route relations as it's related platform(s).	 * 
	 * @param n
	 */
	protected void performStopPositionComparePlatformRelations() {

		HashMap<Long, Long> stopPositionRelationIds = new HashMap<>();
		HashMap<Long, Long> platformRelationIds = new HashMap<>();

		// Loop through all referrer relations
		for (Relation referrer : OsmPrimitive.getFilteredList(node.getReferrers(), Relation.class)) {

			// Create list of relations the stop position belongs to
			if (referrer.get("type") == "route") {
				stopPositionRelationIds.put(referrer.getId(), referrer.getId());
			}
			
			// Create list of relations the related platform(s) belongs to
			else if (referrer.get("public_transport") == "stop_area") {
				for (RelationMember stopAreaMember : referrer.getMembers()) {
					Node stopAreaMemberFoo = stopAreaMember.getNode();
					if (stopAreaMemberFoo.get("public_transport") == "platform") {
						for (Relation stopAreaMemberReferrer : OsmPrimitive.getFilteredList(stopAreaMemberFoo.getReferrers(), Relation.class)) {
							if (stopAreaMemberReferrer.get("type") == "route") {
								platformRelationIds.put(stopAreaMemberReferrer.getId(), stopAreaMemberReferrer.getId());
							}
						}
					}
				}
			}
		}

		// Check if route relation lists are identical
		if (stopPositionRelationIds.equals(platformRelationIds)) {
			return;
		}
		
		List<OsmPrimitive> primitives = new ArrayList<>(1);
		primitives.add(node);
		TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop position and it's related platform(s) have different route relations"),
				PTAssistantValidatorTest.ERROR_CODE_STOP_POSITION_COMPARE_RELATIONS, primitives);
		errors.add(e);
	}
	

	/**
	 * Fixes errors: solitary stop position and platform which is part of a way.
	 * Asks the user first.
	 * 
	 * @param testError
	 * @return
	 */
	protected static Command fixError(TestError testError) {

		if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION
				&& testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_PLATFORM_PART_OF_HIGHWAY) {
			return null;
		}

		Node problematicNode = (Node) testError.getPrimitives().iterator().next();
		ArrayList<OsmPrimitive> primitivesToSelect = new ArrayList<>(1);
		primitivesToSelect.add(problematicNode);
		SelectCommand selectCommand = new SelectCommand(primitivesToSelect);
		selectCommand.executeCommand();

		final int[] userSelection = { JOptionPane.YES_OPTION };
		final TestError errorParameter = testError;
		if (SwingUtilities.isEventDispatchThread()) {

			userSelection[0] = showFixNodeTagDialog(errorParameter);

		} else {

			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						userSelection[0] = showFixNodeTagDialog(errorParameter);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}

		if (userSelection[0] == JOptionPane.YES_OPTION) {

			Node modifiedNode = new Node(problematicNode);
			if (testError.getCode() == PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION) {
				modifiedNode.put("public_transport", "platform");
				ChangeCommand command = new ChangeCommand(problematicNode, modifiedNode);
				return command;
			} else {
				modifiedNode.put("public_transport", "stop_position");
				ChangeCommand command = new ChangeCommand(problematicNode, modifiedNode);
				return command;
			}
		}

		return null;

	}

	private static int showFixNodeTagDialog(TestError e) {
		Node problematicNode = (Node) e.getPrimitives().iterator().next();
		Main.map.mapView.zoomTo(problematicNode.getCoor());

		String[] options = { tr("Yes"), tr("No") };
		String message;
		if (e.getCode() == PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION) {
			message = "Do you want to change the tag public_transport=stop_position to public_transport=platform?";
		} else {
			message = "Do you want to change the tag public_transport=platform to public_transport=stop_position?";
		}
		return JOptionPane.showOptionDialog(null, message, tr("PT_Assistant Message"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, 0);
	}

}
