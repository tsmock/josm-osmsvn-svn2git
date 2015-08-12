package org.openstreetmap.josm.plugins.mapillary;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.mapillary.cache.CacheUtils;
import org.openstreetmap.josm.plugins.mapillary.gui.MapillaryMainDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Database class for all the MapillaryImage objects.
 *
 * @author nokutu
 * @see MapillaryAbstractImage
 * @see MapillarySequence
 *
 */
public class MapillaryData {

  private List<MapillaryAbstractImage> images;
  private MapillaryAbstractImage selectedImage;
  /** The image under the cursor */
  private MapillaryAbstractImage highlightedImage;
  private final List<MapillaryAbstractImage> multiSelectedImages;

  private CopyOnWriteArrayList<MapillaryDataListener> listeners = new CopyOnWriteArrayList<>();

  /** The bounds of the areas for which the pictures have been downloaded. */
  public CopyOnWriteArrayList<Bounds> bounds;

  /**
   * Main constructor.
   */
  protected MapillaryData() {
    this.images = new CopyOnWriteArrayList<>();
    this.multiSelectedImages = new ArrayList<>();
    this.selectedImage = null;

    addListener(MapillaryPlugin.walkAction);
    addListener(MapillaryPlugin.zoomAction);
    addListener(MapillaryPlugin.uploadAction);
    if (Main.main != null)
      addListener(MapillaryMainDialog.getInstance());
  }

  /**
   * Adds a set of MapillaryImages to the object, and then repaints mapView.
   *
   * @param images
   *          The set of images to be added.
   */
  public synchronized void add(List<MapillaryAbstractImage> images) {
    add(images, true);
  }

  /**
   * Adds an MapillaryImage to the object, and then repaints mapView.
   *
   * @param image
   *          The image to be added.
   */
  public synchronized void add(MapillaryAbstractImage image) {
    add(image, true);
  }

  /**
   * Removes an image from the database.
   *
   * @param image
   */
  public synchronized void remove(MapillaryAbstractImage image) {
    if (MapillaryMainDialog.getInstance().getImage() != null) {
      MapillaryMainDialog.getInstance().setImage(null);
      MapillaryMainDialog.getInstance().updateImage();
    }
    setSelectedImage(null);
    this.images.remove(image);
    if (image.getSequence() != null)
      image.getSequence().remove(image);
    if (Main.main != null)
      dataUpdated();
  }

  /**
   * Removes a set of images from the database.
   *
   * @param images
   */
  public synchronized void remove(List<MapillaryAbstractImage> images) {
    for (MapillaryAbstractImage img : images)
      remove(img);
  }

  /**
   * Adds a new listener.
   *
   * @param lis
   *          Listener to be added.
   */
  public void addListener(MapillaryDataListener lis) {
    this.listeners.add(lis);
  }

  /**
   * Removes a listener.
   *
   * @param lis
   *          Listener to be removed.
   */
  public void removeListener(MapillaryDataListener lis) {
    this.listeners.remove(lis);
  }

  /**
   * Adds a set of MapillaryImages to the object, but doesn't repaint mapView.
   * This is needed for concurrency.
   *
   * @param images
   *          The set of images to be added.
   * @param update
   *          Whether the map must be updated or no.
   */
  public synchronized void add(List<MapillaryAbstractImage> images,
      boolean update) {
    for (MapillaryAbstractImage image : images) {
      add(image, update);
    }
  }

  /**
   * Highlights the image under the cursor.
   *
   * @param image
   *          The image under the cursor.
   */
  public void setHighlightedImage(MapillaryAbstractImage image) {
    this.highlightedImage = image;
  }

  /**
   * Returns the image under the mouse cursor.
   *
   * @return The image under the mouse cursor.
   */
  public MapillaryAbstractImage getHighlightedImage() {
    return this.highlightedImage;
  }

  /**
   * Adds a MapillaryImage to the object, but doesn't repaint mapView. This is
   * needed for concurrency.
   *
   * @param image
   *          The image to be added.
   * @param update
   *          Whether the map must be updated or not.
   */
  public synchronized void add(MapillaryAbstractImage image, boolean update) {
    if (!this.images.contains(image)) {
      this.images.add(image);
    }
    if (update)
      dataUpdated();
    fireImagesAdded();
  }

  /**
   * Repaints mapView object.
   */
  public synchronized static void dataUpdated() {
    if (Main.main != null)
      Main.map.mapView.repaint();
  }

  /**
   * Returns a List containing all images.
   *
   * @return A List object containing all images.
   */
  public List<MapillaryAbstractImage> getImages() {
    return this.images;
  }

  /**
   * Returns the MapillaryImage object that is currently selected.
   *
   * @return The selected MapillaryImage object.
   */
  public MapillaryAbstractImage getSelectedImage() {
    return this.selectedImage;
  }

  private void fireImagesAdded() {
    if (this.listeners.isEmpty())
      return;
    for (MapillaryDataListener lis : this.listeners)
      if (lis != null)
        lis.imagesAdded();
  }

  /**
   * If the selected MapillaryImage is part of a MapillarySequence then the
   * following visible MapillaryImage is selected. In case there is none, does
   * nothing.
   *
   * @throws IllegalStateException
   *           if the selected image is null or the selected image doesn't
   *           belong to a sequence.
   */
  public void selectNext() {
    selectNext(Main.pref.getBoolean("mapillary.move-to-picture", true));
  }

  /**
   * If the selected MapillaryImage is part of a MapillarySequence then the
   * following visible MapillaryImage is selected. In case there is none, does
   * nothing.
   *
   * @param moveToPicture
   *          True if the view must me moved to the next picture.
   * @throws IllegalStateException
   *           if the selected image is null or the selected image doesn't
   *           belong to a sequence.
   */
  public void selectNext(boolean moveToPicture) {
    if (getSelectedImage() == null)
      throw new IllegalStateException();
    if (getSelectedImage().getSequence() == null)
      throw new IllegalStateException();
    MapillaryAbstractImage tempImage = this.selectedImage;
    while (tempImage.next() != null) {
      tempImage = tempImage.next();
      if (tempImage.isVisible()) {
        setSelectedImage(tempImage, moveToPicture);
        break;
      }
    }
  }

  /**
   * If the selected MapillaryImage is part of a MapillarySequence then the
   * previous visible MapillaryImage is selected. In case there is none, does
   * nothing.
   *
   * @throws IllegalStateException
   *           if the selected image is null or the selected image doesn't
   *           belong to a sequence.
   */
  public void selectPrevious() {
    selectPrevious(Main.pref.getBoolean("mapillary.move-to-picture", true));
  }

  /**
   * If the selected MapillaryImage is part of a MapillarySequence then the
   * previous visible MapillaryImage is selected. In case there is none, does
   * nothing. * @throws IllegalStateException if the selected image is null or
   * the selected image doesn't belong to a sequence.
   *
   * @param moveToPicture
   *          True if the view must me moved to the previous picture.
   * @throws IllegalStateException
   *           if the selected image is null or the selected image doesn't
   *           belong to a sequence.
   */
  public void selectPrevious(boolean moveToPicture) {
    if (getSelectedImage() == null)
      throw new IllegalStateException();
    if (getSelectedImage().getSequence() == null)
      throw new IllegalStateException();
    MapillaryAbstractImage tempImage = this.selectedImage;
    while (tempImage.previous() != null) {
      tempImage = tempImage.previous();
      if (tempImage.isVisible()) {
        setSelectedImage(tempImage, moveToPicture);
        break;
      }
    }
  }

  /**
   * Selects a new image.If the user does ctrl + click, this isn't triggered.
   *
   * @param image
   *          The MapillaryImage which is going to be selected
   *
   */
  public void setSelectedImage(MapillaryAbstractImage image) {
    setSelectedImage(image, false);
  }

  /**
   * Selects a new image.If the user does ctrl+click, this isn't triggered. You
   * can choose whether to center the view on the new image or not.
   *
   * @param image
   *          The {@link MapillaryImage} which is going to be selected.
   * @param zoom
   *          True if the view must be centered on the image; false otherwise.
   */
  public void setSelectedImage(MapillaryAbstractImage image, boolean zoom) {
    MapillaryAbstractImage oldImage = this.selectedImage;
    this.selectedImage = image;
    this.multiSelectedImages.clear();
    this.multiSelectedImages.add(image);
    if (image != null && Main.main != null) {
      if (image instanceof MapillaryImage) {
        MapillaryImage mapillaryImage = (MapillaryImage) image;
        // Downloading thumbnails of surrounding pictures.
        if (mapillaryImage.next() != null) {
          CacheUtils.downloadPicture((MapillaryImage) mapillaryImage.next());
          if (mapillaryImage.next().next() != null)
            CacheUtils.downloadPicture((MapillaryImage) mapillaryImage.next()
                .next());
        }
        if (mapillaryImage.previous() != null) {
          CacheUtils
              .downloadPicture((MapillaryImage) mapillaryImage.previous());
          if (mapillaryImage.previous().previous() != null)
            CacheUtils.downloadPicture((MapillaryImage) mapillaryImage
                .previous().previous());
        }
      }
    }
    if (zoom && Main.main != null)
      Main.map.mapView.zoomTo(getSelectedImage().getLatLon());
    if (Main.main != null)
      Main.map.mapView.repaint();
    fireSelectedImageChanged(oldImage, this.selectedImage);
  }

  private void fireSelectedImageChanged(MapillaryAbstractImage oldImage,
      MapillaryAbstractImage newImage) {
    if (this.listeners.isEmpty())
      return;
    for (MapillaryDataListener lis : this.listeners)
      if (lis != null)
        lis.selectedImageChanged(oldImage, newImage);
  }

  /**
   * Adds a MapillaryImage object to the list of selected images, (when ctrl +
   * click)
   *
   * @param image
   *          The MapillaryImage object to be added.
   */
  public void addMultiSelectedImage(MapillaryAbstractImage image) {
    if (!this.multiSelectedImages.contains(image)) {
      if (this.getSelectedImage() != null)
        this.multiSelectedImages.add(image);
      else
        this.setSelectedImage(image);
    }
    if (Main.main != null)
      Main.map.mapView.repaint();
  }

  /**
   * Adds a set of {@code MapillaryAbstractImage} objects to the list of
   * selected images.
   *
   * @param images
   *          A List object containing the set of images to be added.
   */
  public void addMultiSelectedImage(List<MapillaryAbstractImage> images) {
    for (MapillaryAbstractImage image : images)
      if (!this.multiSelectedImages.contains(image)) {
        if (this.getSelectedImage() != null)
          this.multiSelectedImages.add(image);
        else
          this.setSelectedImage(image);
      }
    Main.map.mapView.repaint();
  }

  /**
   * Returns a List containing all {@code MapillaryAbstractImage} objects
   * selected with ctrl + click.
   *
   * @return A List object containing all the images selected.
   */
  public List<MapillaryAbstractImage> getMultiSelectedImages() {
    return this.multiSelectedImages;
  }

  /**
   * Sets a new ArrayList object as the used set of images.
   *
   * @param images
   */
  public synchronized void setImages(List<MapillaryAbstractImage> images) {
    this.images = new ArrayList<>(images);
  }

  /**
   * Returns the amount of images contained by this object.
   *
   * @return The amount of images in stored.
   */
  public int size() {
    return this.images.size();
  }
}