package com.crunchiest.ui;

/**
 * Shared slot layout and pagination utilities for inventory-based menus.
 */
public final class PagedMenuLayout
{
  public static final int INVENTORY_SIZE=54;
  public static final int CONTENT_SLOTS=45;
  public static final int PREVIOUS_SLOT=45;
  public static final int BACK_SLOT=48;
  public static final int PAGE_INFO_SLOT=49;
  public static final int NEXT_SLOT=53;

  private PagedMenuLayout()
  {
  }

  /**
   * Calculates total page count for the given number of items.
   *
   * @param totalItems total content entries
   * @return total pages (minimum 1)
   */
  public static int totalPages(int totalItems)
  {
    if (totalItems <= 0)
    {
      return 1;
    }
    return (int) Math.ceil(totalItems / (double) CONTENT_SLOTS);
  }

  /**
   * Calculates content start index for a page.
   *
   * @param page zero-based page index
   * @return start item index
   */
  public static int startIndex(int page)
  {
    return page * CONTENT_SLOTS;
  }

  /**
   * Clamps a requested page index into valid page bounds.
   *
   * @param requestedPage requested page index
   * @param totalPages total available pages
   * @return clamped page index
   */
  public static int clampPage(int requestedPage, int totalPages)
  {
    if (requestedPage < 0)
    {
      return 0;
    }

    if (requestedPage >= totalPages)
    {
      return totalPages - 1;
    }

    return requestedPage;
  }
}
