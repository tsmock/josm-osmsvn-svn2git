package org.openstreetmap.josm.plugins.mapillary.downloads;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.mapillary.MapillaryAbstractImage;
import org.openstreetmap.josm.plugins.mapillary.MapillaryImage;
import org.openstreetmap.josm.plugins.mapillary.MapillaryImportedImage;

/**
 * Writes the images from the queue in the file system.
 * 
 * @author nokutu
 * @see MapillaryExportManager
 */
public class MapillaryExportWriterThread extends Thread {

  private final String path;
  private final ArrayBlockingQueue<BufferedImage> queue;
  private final ArrayBlockingQueue<MapillaryAbstractImage> queueImages;
  private final int amount;
  private final ProgressMonitor monitor;

  public MapillaryExportWriterThread(String path,
      ArrayBlockingQueue<BufferedImage> queue,
      ArrayBlockingQueue<MapillaryAbstractImage> queueImages, int amount,
      ProgressMonitor monitor) {
    this.path = path;
    this.queue = queue;
    this.queueImages = queueImages;
    this.amount = amount;
    this.monitor = monitor;
  }

  @Override
  public void run() {
    monitor.setCustomText("Downloaded 0/" + amount);
    File tempFile = null;
    BufferedImage img;
    MapillaryAbstractImage mimg = null;
    String finalPath = "";
    for (int i = 0; i < amount; i++) {
      try {
        img = queue.take();
        mimg = queueImages.take();
        if (path == null && mimg instanceof MapillaryImportedImage) {
          String path = ((MapillaryImportedImage) mimg).getFile().getPath();
          finalPath = path.substring(0, path.lastIndexOf('.'));
        } else if (mimg instanceof MapillaryImage)
          finalPath = path + "/" + ((MapillaryImage) mimg).getKey();
        else if (mimg instanceof MapillaryImportedImage)
          finalPath = path + "/"
              + ((MapillaryImportedImage) mimg).getFile().getName();
        ;
        // Creates a temporal file that is going to be deleted after
        // writing the EXIF tags.
        tempFile = new File(finalPath + ".tmp");
        ImageIO.write(img, "jpg", tempFile);

        // Write EXIF tags
        TiffOutputSet outputSet = null;
        TiffOutputDirectory exifDirectory = null;
        // If the image is imported, loads the rest of the EXIF data.
        if (mimg instanceof MapillaryImportedImage) {
          final ImageMetadata metadata = Imaging
              .getMetadata(((MapillaryImportedImage) mimg).getFile());
          final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
          if (null != jpegMetadata) {
            final TiffImageMetadata exif = jpegMetadata.getExif();
            if (null != exif) {
              outputSet = exif.getOutputSet();
            }
          }
        }
        if (null == outputSet) {
          outputSet = new TiffOutputSet();
        }
        exifDirectory = outputSet.getOrCreateExifDirectory();

        exifDirectory
            .removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF);
        exifDirectory.add(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF,
            GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF_VALUE_TRUE_NORTH);

        exifDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION);
        exifDirectory.add(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION,
            RationalNumber.valueOf(mimg.getCa()));

        exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
        if (mimg instanceof MapillaryImportedImage)
          exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL,
              ((MapillaryImportedImage) mimg).getDate("yyyy/MM/dd hh:mm:ss"));
        else if (mimg instanceof MapillaryImage)
          exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL,
              ((MapillaryImage) mimg).getDate("yyyy/MM/dd hh/mm/ss"));
        outputSet.setGPSInDegrees(mimg.getLatLon().lon(), mimg.getLatLon()
            .lat());
        OutputStream os = new BufferedOutputStream(new FileOutputStream(
            finalPath + ".jpg"));
        new ExifRewriter().updateExifMetadataLossless(tempFile, os, outputSet);
        tempFile.delete();
        os.close();
      } catch (InterruptedException e) {
        Main.info("Mapillary export cancelled");
        return;
      } catch (IOException e) {
        Main.error(e);
      } catch (ImageWriteException e) {
        Main.error(e);
      } catch (ImageReadException e) {
        Main.error(e);
      }

      // Increases the progress bar.
      monitor.worked(PleaseWaitProgressMonitor.PROGRESS_BAR_MAX / amount);
      monitor.setCustomText("Downloaded " + (i + 1) + "/" + amount);
    }
  }
}
