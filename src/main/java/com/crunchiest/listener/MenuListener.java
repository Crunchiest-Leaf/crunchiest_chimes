/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.crunchiest.ui.SoundMenuManager;

/**
 * Delegates menu inventory events to the menu manager.
 */
public class MenuListener implements Listener
{
  private final SoundMenuManager menuManager;

  /**
   * Creates the menu listener.
   *
   * @param menuManager menu manager
   */
  public MenuListener(SoundMenuManager menuManager)
  {
    this.menuManager=menuManager;
  }

  /**
   * Handles inventory click events for managed menus.
   *
   * @param event inventory click event
   */
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event)
  {
    menuManager.handleInventoryClick(event);
  }

  /**
   * Clears active menu session state when managed menus are closed.
   *
   * @param event inventory close event
   */
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event)
  {
    if (menuManager.isManagedView(event.getView()))
    {
      menuManager.clearSession(event.getPlayer().getUniqueId());
    }
  }
}
