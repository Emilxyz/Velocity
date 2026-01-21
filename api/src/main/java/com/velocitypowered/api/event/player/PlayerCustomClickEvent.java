/*
 * Copyright (C) 2018-2026 Velocity Contributors
 *
 * The Velocity API is licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in the api top-level directory.
 */

package com.velocitypowered.api.event.player;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.annotation.AwaitingEvent;
import com.velocitypowered.api.event.player.PlayerCustomClickEvent.ForwardResult;
import com.velocitypowered.api.proxy.Player;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Fired when a {@link Player} sends a {@code minecraft:custom} click action. Velocity will wait on
 * this event to finish firing before discarding the custom click action (if handled)
 * or forwarding it to the backend server.
 */
@Beta
@AwaitingEvent
public final class PlayerCustomClickEvent implements ResultedEvent<ForwardResult> {

  private final Player player;
  private final Key identifier;
  private final @Nullable BinaryTagHolder payload;
  private ForwardResult result;

  /**
   * Constructs a PlayerCustomClickEvent.
   *
   * @param player     the player
   * @param identifier the identifier of the custom click action
   * @param payload    the payload of the custom click action, if any
   */
  public PlayerCustomClickEvent(Player player, Key identifier, @Nullable BinaryTagHolder payload) {
    this.player = Preconditions.checkNotNull(player, "player");
    this.identifier = Preconditions.checkNotNull(identifier, "identifier");
    this.payload = payload;
    this.result = ForwardResult.forward();
  }

  /**
   * {@return the player who sent the custom click action}
   */
  public Player getPlayer() {
    return this.player;
  }

  /**
   * {@return the identifier of the custom click action}
   */
  public Key getIdentifier() {
    return this.identifier;
  }

  /**
   * {@return the payload of the custom click action, if any}
   */
  public Optional<BinaryTagHolder> getPayload() {
    return Optional.ofNullable(this.payload);
  }

  @Override
  public ForwardResult getResult() {
    return this.result;
  }

  @Override
  public void setResult(ForwardResult result) {
    this.result = Preconditions.checkNotNull(result, "result");
  }

  @Override
  public String toString() {
    return "PlayerCustomClickEvent{"
        + "player=" + this.player
        + ", identifier=" + this.identifier
        + ", payload=" + this.payload
        + ", result=" + this.result
        + '}';
  }

  /**
   * A result determining whether or not to forward this message on.
   */
  public static final class ForwardResult implements ResultedEvent.Result {

    private static final ForwardResult ALLOWED = new ForwardResult(true);
    private static final ForwardResult DENIED = new ForwardResult(false);

    private final boolean status;

    private ForwardResult(boolean b) {
      this.status = b;
    }

    @Override
    public boolean isAllowed() {
      return status;
    }

    @Override
    public String toString() {
      return status ? "forward to server" : "handled at proxy";
    }

    /**
     * Allows the custom click action to be forwarded to the backend server.
     *
     * @return the forward result
     */
    public static ForwardResult forward() {
      return ALLOWED;
    }

    /**
     * Prevents the custom click action from being forwarded to the backend server,
     * marking it as handled at the proxy.
     *
     * @return the forward result
     */
    public static ForwardResult handled() {
      return DENIED;
    }
  }
}
