package org.openstreetmap.josm.plugins.mapillary;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MapillaryImportedImage extends MapillaryAbstractImage {

	private File file;
	
	public MapillaryImportedImage(double lat, double lon, double ca, File file) {
		super(lat, lon, ca);
		this.file = file;
	}
	
	public BufferedImage getImage() throws IOException {
		return ImageIO.read(file);
	}
}
