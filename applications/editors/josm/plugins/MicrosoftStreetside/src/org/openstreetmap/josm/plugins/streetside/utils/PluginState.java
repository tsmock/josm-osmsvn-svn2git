// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

/**
 * @author nokutu
 *
 */
public final class PluginState {

  private static final Logger LOGGER = Logger.getLogger(PluginState.class.getCanonicalName());

  private static boolean submittingChangeset;

  private static int runningDownloads;
  /**
   * Images that have to be uploaded.
   */
  private static int imagesToUpload;
  /**
   * Images that have been uploaded.
   */
  private static int imagesUploaded;

  private PluginState() {
    // Empty constructor to avoid instantiation
  }

  /**
   * Called when a download is started.
   */
  public static void startDownload() {
    runningDownloads++;
  }

  /**
   * Called when a download is finished.
   */
  public static void finishDownload() {
    if (runningDownloads == 0) {
      LOGGER.log(Logging.LEVEL_WARN, I18n.tr("The amount of running downloads is equal to 0"));
      return;
    }
    runningDownloads--;
  }

  /**
   * Checks if there is any running download.
   *
   * @return true if the plugin is downloading; false otherwise.
   */
  public static boolean isDownloading() {
    return runningDownloads > 0;
  }

  /**
   * Checks if there is a changeset being submitted.
   *
   * @return true if the plugin is submitting a changeset false otherwise.
   */
  public static boolean isSubmittingChangeset() {
    return submittingChangeset;
  }

  public static void setSubmittingChangeset(boolean isSubmitting) {
    submittingChangeset = isSubmitting;
  }

  /**
   * Checks if there is any running upload.
   *
   * @return true if the plugin is uploading; false otherwise.
   */
  public static boolean isUploading() {
    return imagesToUpload > imagesUploaded;
  }

  /**
   * Sets the amount of images that are going to be uploaded.
   *
   * @param amount The amount of images that are going to be uploaded.
   */
  public static void addImagesToUpload(int amount) {
    if (imagesToUpload <= imagesUploaded) {
      imagesToUpload = 0;
      imagesUploaded = 0;
    }
    imagesToUpload += amount;
  }

  public static int getImagesToUpload() {
    return imagesToUpload;
  }

  public static int getImagesUploaded() {
    return imagesUploaded;
  }

  /**
   * Called when an image is uploaded.
   */
  public static void imageUploaded() {
    imagesUploaded++;
    if (imagesToUpload == imagesUploaded) {
      finishedUploadDialog(imagesUploaded);
    }
  }

  private static void finishedUploadDialog(int numImages) {
    JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
        tr("You have successfully uploaded {0} images to Bing.com", numImages), tr("Finished upload"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  public static void notLoggedInToMapillaryDialog() {
    JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
        tr("You are not logged in, please log in to Streetside in the preferences"),
        tr("Not Logged in to Streetside"), JOptionPane.WARNING_MESSAGE);
  }

  /**
   * Returns the text to be written in the status bar.
   *
   * @return The {@code String} that is going to be written in the status bar.
   */
  public static String getUploadString() {
    return tr("Uploading: {0}", "(" + imagesUploaded + "/" + imagesToUpload + ")");
  }
}
