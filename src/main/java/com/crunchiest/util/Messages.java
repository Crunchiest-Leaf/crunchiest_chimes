/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/** Utility helper for sending and building MiniMessage-based Adventure components. */
public final class Messages
{
  private static final MiniMessage MINI_MESSAGE=MiniMessage.miniMessage();

  private Messages()
  {
  }

  /**
   * Sends a MiniMessage string to the given audience.
   *
   * @param audience audience that receives the message
   * @param miniMessage MiniMessage-formatted string
   */
  public static void send(Audience audience, String miniMessage)
  {
    audience.sendMessage(MINI_MESSAGE.deserialize(miniMessage));
  }

  /**
   * Sends a MiniMessage string with placeholder resolvers to the given audience.
   *
   * @param audience audience that receives the message
   * @param miniMessage MiniMessage-formatted string
   * @param resolvers MiniMessage placeholder resolvers
   */
  public static void send(Audience audience, String miniMessage, TagResolver... resolvers)
  {
    audience.sendMessage(MINI_MESSAGE.deserialize(miniMessage, resolvers));
  }

  /**
   * Deserializes a MiniMessage string into an Adventure component.
   *
   * @param miniMessage MiniMessage-formatted string
   * @return deserialized component
   */
  public static Component component(String miniMessage)
  {
    return MINI_MESSAGE.deserialize(miniMessage);
  }

  /**
   * Deserializes a MiniMessage string with placeholders into an Adventure component.
   *
   * @param miniMessage MiniMessage-formatted string
   * @param resolvers MiniMessage placeholder resolvers
   * @return deserialized component
   */
  public static Component component(String miniMessage, TagResolver... resolvers)
  {
    return MINI_MESSAGE.deserialize(miniMessage, resolvers);
  }
}
