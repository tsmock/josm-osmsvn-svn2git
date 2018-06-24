// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.actions;

import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideData;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.cache.CacheUtils;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;


/**
 * Thread containing the walk process.
 *
 * @author nokutu
 */
public class WalkThread extends Thread implements StreetsideDataListener {
  private final int interval;
  private final StreetsideData data;
  private boolean end;
  private final boolean waitForFullQuality;
  private final boolean followSelected;
  private final boolean goForward;
  private BufferedImage lastImage;
  private volatile boolean paused;

  /**
   * Main constructor.
   *
   * @param interval How often the images switch.
   * @param waitForPicture If it must wait for the full resolution picture or just the
   * thumbnail.
   * @param followSelected Zoom to each image that is selected.
   * @param goForward true to go forward; false to go backwards.
   */
  public WalkThread(int interval, boolean waitForPicture,
                    boolean followSelected, boolean goForward) {
    this.interval = interval;
    waitForFullQuality = waitForPicture;
    this.followSelected = followSelected;
    this.goForward = goForward;
    data = StreetsideLayer.getInstance().getData();
    data.addListener(this);
  }

  @Override
  public void run() {
    try {
      while (!end && data.getSelectedImage().next() != null) {
        StreetsideAbstractImage image = data.getSelectedImage();
        if (image != null && image.next() instanceof StreetsideImage) {
          // Predownload next 10 thumbnails.
          preDownloadImages((StreetsideImage) image.next(), 10, CacheUtils.PICTURE.THUMBNAIL);
          // TODO: WalkThread for cubemaps? @rrh
      	  //preDownloadCubemaps((StreetsideImage) image.next(), 10, CacheUtils.PICTURE.CUBEMAP);
          if (waitForFullQuality) {
            // Start downloading 3 next full images.
            StreetsideAbstractImage currentImage = image.next();
        	preDownloadImages((StreetsideImage) currentImage, 3, CacheUtils.PICTURE.FULL_IMAGE);
        	// TODO: WalkThread for cubemaps? @rrh
        	/*if (StreetsideProperties.PREDOWNLOAD_CUBEMAPS.get().booleanValue()) {
          	  preDownloadCubemaps((StreetsideImage) currentImage, 3, CacheUtils.PICTURE.CUBEMAP);
            }*/
          }
        }
        try {
          // Waits for full quality picture.
          final BufferedImage displayImage = StreetsideMainDialog.getInstance().getStreetsideImageDisplay().getImage();
          if (waitForFullQuality && image instanceof StreetsideImage) {
            while (displayImage == lastImage || displayImage == null || displayImage.getWidth() < 2048) {
              Thread.sleep(100);
            }
          } else { // Waits for thumbnail.
            while (displayImage == lastImage || displayImage == null || displayImage.getWidth() < 320) {
              Thread.sleep(100);
            }
          }
          while (paused) {
            Thread.sleep(100);
          }
          wait(interval);
          while (paused) {
            Thread.sleep(100);
          }
          lastImage = StreetsideMainDialog.getInstance().getStreetsideImageDisplay().getImage();
          if (goForward) {
            data.selectNext(followSelected);
          } else {
            data.selectPrevious(followSelected);
          }
        } catch (InterruptedException e) {
          return;
        }
      }

    	// TODO: WalkThread for cubemaps? @rrh
      	/*while (!end && data.getSelectedImage().next() != null) {
            StreetsideAbstractImage cubemap = data.getSelectedImage();
            if (cubemap != null && cubemap.next() instanceof StreetsideCubemap) {
              if (waitForFullQuality) {
                // Start downloading 3 next full images.

            	// TODO: cubemap handling @rrh
            	preDownloadCubemaps((StreetsideCubemap) cubemap.next(), 6, CacheUtils.PICTURE.CUBEMAP);
              }
            }
            try {
              // Waits for full quality picture.
              final BufferedImage[] displayCubemap = StreetsideMainDialog.getInstance().streetsideViewerDisplay.getCubemap();
              if (waitForFullQuality && cubemap instanceof StreetsideCubemap) {
                  // TODO: handle cubemap width? @rrh
            	  while (displayCubemap == lastCubemap || displayCubemap == null || displayCubemap.getWidth() < 2048) {
                  Thread.sleep(100);
                }
              } else { // Waits for thumbnail.
            	  // TODO: handle cubemap width? @rrh
            	  while (displayCubemap == lastCubemap || displayCubemap == null || displayCubemap.getWidth() < 320) {
                  Thread.sleep(100);
                }
              }
              while (paused) {
                Thread.sleep(100);
              }
              wait(interval);
              while (paused) {
                Thread.sleep(100);
              }
              lastCubemap = StreetsideMainDialog.getInstance().streetsideViewerDisplay.getCubemap();
              // TODO: forward / previous for cubemap? @rrh
              if (goForward) {
                data.selectNext(followSelected);
              } else {
                data.selectPrevious(followSelected);
              }
            } catch (InterruptedException e) {
              return;
            }
          }*/
    } catch (NullPointerException e) {
      // TODO: Avoid NPEs instead of waiting until they are thrown and then catching them
      return;
    }
    end();
  }

  private void preDownloadCubemaps(StreetsideImage startImage, int n, CacheUtils.PICTURE type) {
	  if (n >= 1 && startImage != null) {

		  for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 4; j++) {
					for (int k = 0; k < 4; k++) {

						CacheUtils.downloadPicture(startImage, type);
						if (startImage.next() instanceof StreetsideImage && n >= 2) {
							preDownloadImages((StreetsideImage) startImage.next(), n - 1, type);
						}
					}
				}
		  }
	  }
  }

/**
   * Downloads n images into the cache beginning from the supplied start-image (including the start-image itself).
   *
   * @param startImage the image to start with (this and the next n-1 images in the same sequence are downloaded)
   * @param n the number of images to download
   * @param type the quality of the image (full or thumbnail)
   */
  private static void preDownloadImages(StreetsideImage startImage, int n, CacheUtils.PICTURE type) {
    if (n >= 1 && startImage != null) {
      CacheUtils.downloadPicture(startImage, type);
      if (startImage.next() instanceof StreetsideImage && n >= 2) {
        preDownloadImages((StreetsideImage) startImage.next(), n - 1, type);
      }
    }
  }

  @Override
  public void imagesAdded() {
    // Nothing
  }

  @Override
  public void selectedImageChanged(StreetsideAbstractImage oldImage, StreetsideAbstractImage newImage) {
    if (newImage != oldImage.next()) {
      end();
      interrupt();
    }
  }

  /**
   * Continues with the execution if paused.
   */
  public void play() {
    paused = false;
  }

  /**
   * Pauses the execution.
   */
  public void pause() {
    paused = true;
  }

  /**
   * Stops the execution.
   */
  public void stopWalk() {
    if (SwingUtilities.isEventDispatchThread()) {
      end();
      interrupt();
    } else {
      SwingUtilities.invokeLater(this::stopWalk);
    }
  }

  /**
   * Called when the walk stops by itself of forcefully.
   */
  public void end() {
    if (SwingUtilities.isEventDispatchThread()) {
      end = true;
      data.removeListener(this);
      StreetsideMainDialog.getInstance().setMode(StreetsideMainDialog.MODE.NORMAL);
    } else {
      SwingUtilities.invokeLater(this::end);
    }
    // TODO: WalkThread for Cubemaps? @rrh
    /*if (Platform.isEventDispatchThread()) {
        end = true;
        data.removeListener(this);
        StreetsideViewerDialog.getInstance().setMode(StreetsideViewerDialog.MODE.NORMAL);
      } else {
        Platform.invokeLater(this::end);
      }*/
  }
}
