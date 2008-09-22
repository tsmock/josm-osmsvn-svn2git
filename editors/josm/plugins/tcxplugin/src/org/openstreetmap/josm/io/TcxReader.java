// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.io.tcx.ActivityLapT;
import org.openstreetmap.josm.io.tcx.ActivityT;
import org.openstreetmap.josm.io.tcx.CourseT;
import org.openstreetmap.josm.io.tcx.TrackT;
import org.openstreetmap.josm.io.tcx.TrackpointT;
import org.openstreetmap.josm.io.tcx.TrainingCenterDatabaseT;

/**
 * TCX Reader. This class is based on code genarated by the Java Architecture
 * for XML Binding (JAXB). For this class to work you will need the API und IMPL
 * Jars from the RI. JAXB can be downloaded at <a
 * href="https://jaxb.dev.java.net/">https://jaxb.dev.java.net/</a>. This class
 * has been developed using JAXB version 2.1.7.
 * <p>
 * Additional information and tutorial are available at: <a
 * href="http://java.sun.com/developer/technicalArticles/WebServices/jaxb/">http://java.sun.com/developer/technicalArticles/WebServices/jaxb/</a>
 * <p>
 * The Garmin TCX Schema file can be downloaded from: <a
 * href="http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd">http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd</a>
 * The command used to generate the code is: <code>
 * xjc.bat -p org.openstreetmap.josm.io.tcx TrainingCenterDatabasev2.xsd -d <path to the src folder of JOSM>
 * </code>
 * <p>
 * Note: if you get an exception that JAXB 2.1 is not supported on your system, you will have to add the jaxb-api.jar
 * to the endorsed directory (create it if necessary) of your JRE. Usually it is something like this:
 * \<program files>\Java\jre<java version>\lib\endorsed
 * 
 * @author adrian <as@nitegate.de>
 * 
 */
public class TcxReader {

	private File tcxFile;

	private GpxData gpxData;

	/**
	 * @param tcxFile
	 */
	public TcxReader(File tcxFile) {
		super();
		this.tcxFile = tcxFile;
		parseFile();
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked") private void parseFile() {
		try {
			JAXBContext jc = JAXBContext
			        .newInstance(TrainingCenterDatabaseT.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			JAXBElement<TrainingCenterDatabaseT> element = (JAXBElement<TrainingCenterDatabaseT>)unmarshaller
			        .unmarshal(tcxFile);

			TrainingCenterDatabaseT tcd = element.getValue();

			gpxData = new GpxData();

			// Usually logged activities are in the activities tag.
			parseDataFromActivities(tcd);
			// GPS tracks in the course tag are generated by the user.
			// Maybe not a good idea to import them.
			parseDataFromCourses(tcd);

		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param tcd
	 */
	private void parseDataFromActivities(TrainingCenterDatabaseT tcd) {
		if ((tcd.getActivities() != null)
		        && (tcd.getActivities().getActivity() != null)) {
			for (ActivityT activity : tcd.getActivities().getActivity()) {
				if (activity.getLap() != null) {
					for (ActivityLapT activityLap : activity.getLap()) {
						if (activityLap.getTrack() != null) {
							GpxTrack currentTrack = new GpxTrack();
							gpxData.tracks.add(currentTrack);
							for (TrackT track : activityLap.getTrack()) {
								if (track.getTrackpoint() != null) {
									Collection<WayPoint> currentTrackSeg = new ArrayList<WayPoint>();
									currentTrack.trackSegs.add(currentTrackSeg);
									for (TrackpointT trackpoint : track
									        .getTrackpoint()) {
										// Some trackspoints don't have a
										// position.
										// Check it
										// first to avoid an NPE!
										if (trackpoint.getPosition() != null) {
											LatLon latLon = new LatLon(
											        trackpoint
											                .getPosition()
											                .getLatitudeDegrees(),
											        trackpoint
											                .getPosition()
											                .getLongitudeDegrees());
											WayPoint currentWayPoint = new WayPoint(
											        latLon);
											// We usually have altitude info
											// here
											// (trackpoint.getAltitudeMeters())
											// Don't know how to add it to
											// the GPX
											// Data...
											currentTrackSeg
											        .add(currentWayPoint);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @param tcd
	 */
	private void parseDataFromCourses(TrainingCenterDatabaseT tcd) {
		if ((tcd.getCourses() != null)
		        && (tcd.getCourses().getCourse() != null)) {
			for (CourseT course : tcd.getCourses().getCourse()) {
				if (course.getTrack() != null) {
					GpxTrack currentTrack = new GpxTrack();
					gpxData.tracks.add(currentTrack);
					for (TrackT track : course.getTrack()) {
						if (track.getTrackpoint() != null) {
							Collection<WayPoint> currentTrackSeg = new ArrayList<WayPoint>();
							currentTrack.trackSegs.add(currentTrackSeg);
							for (TrackpointT trackpoint : track.getTrackpoint()) {
								// Some trackspoints don't have a position.
								// Check it
								// first to avoid an NPE!
								if (trackpoint.getPosition() != null) {
									LatLon latLon = new LatLon(
									        trackpoint.getPosition()
									                .getLatitudeDegrees(),
									        trackpoint.getPosition()
									                .getLongitudeDegrees());
									WayPoint currentWayPoint = new WayPoint(
									        latLon);
									// We usually have altitude info here
									// (trackpoint.getAltitudeMeters())
									// Don't know how to add it to the GPX
									// Data...
									currentTrackSeg.add(currentWayPoint);
								}
							}
						}
					}
				}
			}
		}
	}

	public GpxData getGpxData() {
		return gpxData;
	}
}
