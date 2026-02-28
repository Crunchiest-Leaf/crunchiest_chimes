package com.crunchiest.command;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.crunchiest.CrunchiestChimes;
import com.crunchiest.service.CustomJukeboxService;
import com.crunchiest.ui.SoundMenuManager;
import com.crunchiest.util.Messages;

/**
 * Handles the /chimes command for menu access and configuration reload.
 */
public class ChimesCommand implements CommandExecutor
{
  private final CrunchiestChimes plugin;
  private final CustomJukeboxService jukeboxService;
  private final SoundMenuManager menuManager;

  /**
   * Creates the command executor.
   *
   * @param plugin owning plugin instance
   * @param jukeboxService jukebox state service
   * @param menuManager menu manager for category/sound selection
   */
  public ChimesCommand(CrunchiestChimes plugin, CustomJukeboxService jukeboxService, SoundMenuManager menuManager)
  {
    this.plugin=plugin;
    this.jukeboxService=jukeboxService;
    this.menuManager=menuManager;
  }

  /**
   * Executes /chimes and /chimes reload.
   *
   * @param sender command sender
   * @param command executed command
   * @param label command alias label
   * @param args command arguments
   * @return true when the command is handled
   */
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
  {
    // Guard against unexpected null senders from external command dispatchers.
    if (sender == null)
    {
      return true;
    }

    // Keep reload logic as the only subcommand so command usage remains simple.
    if (args.length == 1 && "reload".equalsIgnoreCase(args[0]))
    {
      if (!sender.hasPermission("crunchiestchimes.reload"))
      {
        Messages.send(sender, "<red>You do not have permission to reload CrunchiestChimes.</red>");
        return true;
      }

      plugin.reloadConfig();
      Messages.send(sender, "<green>CrunchiestChimes config reloaded.</green>");
      return true;
    }

    if (!(sender instanceof Player))
    {
      Messages.send(sender, "<yellow>Only players can open the sound selector UI.</yellow>");
      return true;
    }

    Player player=(Player) sender;
    // Command targeting is line-of-sight based to avoid additional arguments.
    Block targetBlock=player.getTargetBlockExact(6);
    if (targetBlock == null || targetBlock.getType() != Material.JUKEBOX)
    {
      Messages.send(player, "<yellow>Look at a custom jukebox and run /chimes.</yellow>");
      return true;
    }

    if (!jukeboxService.isCustomJukebox(targetBlock.getLocation()))
    {
      Messages.send(player, "<yellow>That jukebox is not a custom CrunchiestChimes jukebox.</yellow>");
      return true;
    }

    String locationKey=jukeboxService.getLocationKey(targetBlock.getLocation());
    menuManager.openCategoryMenu(player, locationKey);
    return true;
  }
}
