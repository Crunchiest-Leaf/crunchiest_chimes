package com.crunchiest.ui;

/**
 * Immutable per-player menu session snapshot used for pagination and view navigation.
 */
public class MenuSession
{
  private final String locationKey;
  private final MenuViewType viewType;
  private final String category;
  private final int page;

  /**
   * Creates a session snapshot.
   *
   * @param locationKey tracked jukebox location key
   * @param viewType active menu type
   * @param category selected category when in sound view
   * @param page active page index
   */
  public MenuSession(String locationKey, MenuViewType viewType, String category, int page)
  {
    this.locationKey=locationKey;
    this.viewType=viewType;
    this.category=category;
    this.page=page;
  }

  /**
   * Returns the tracked jukebox location key.
   *
   * @return location key
   */
  public String getLocationKey()
  {
    return locationKey;
  }

  /**
   * Returns current menu view type.
   *
   * @return view type
   */
  public MenuViewType getViewType()
  {
    return viewType;
  }

  /**
   * Returns selected category for sound view sessions.
   *
   * @return category name or null
   */
  public String getCategory()
  {
    return category;
  }

  /**
   * Returns current page index.
   *
   * @return zero-based page index
   */
  public int getPage()
  {
    return page;
  }
}
