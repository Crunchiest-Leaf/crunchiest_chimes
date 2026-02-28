/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.util;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Immutable world-coordinate key used to identify custom note block locations.
 */
public final class LocationKey
{
  private final String worldName;
  private final int x;
  private final int y;
  private final int z;

  /**
   * Creates a location key.
   *
   * @param worldName world name
   * @param x block x-coordinate
   * @param y block y-coordinate
   * @param z block z-coordinate
   */
  public LocationKey(String worldName, int x, int y, int z)
  {
    this.worldName=worldName;
    this.x=x;
    this.y=y;
    this.z=z;
  }

  /**
   * Builds a key from a Bukkit location.
   *
   * @param location Bukkit location
   * @return corresponding key
   */
  public static LocationKey fromLocation(Location location)
  {
    World world=location.getWorld();
    String worldName=world != null ? world.getName() : "unknown";
    return new LocationKey(worldName, location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  /**
   * Parses a serialized location key.
   *
   * @param key serialized key in world;x;y;z format
   * @return parsed location key
   */
  public static LocationKey fromString(String key)
  {
    String[] parts=key.split(";", 4);
    if (parts.length != 4)
    {
      throw new IllegalArgumentException("Invalid location key: " + key);
    }
    return new LocationKey(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
  }

  /**
   * Returns world name.
   *
   * @return world name
   */
  public String getWorldName()
  {
    return worldName;
  }

  /**
   * Returns block x-coordinate.
   *
   * @return x-coordinate
   */
  public int getX()
  {
    return x;
  }

  /**
   * Returns block y-coordinate.
   *
   * @return y-coordinate
   */
  public int getY()
  {
    return y;
  }

  /**
   * Returns block z-coordinate.
   *
   * @return z-coordinate
   */
  public int getZ()
  {
    return z;
  }

  /**
   * Serializes this key to world;x;y;z format.
   *
   * @return serialized key
   */
  public String asString()
  {
    return worldName + ";" + x + ";" + y + ";" + z;
  }
}
