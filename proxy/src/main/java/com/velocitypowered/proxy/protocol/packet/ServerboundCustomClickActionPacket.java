/*
 * Copyright (C) 2018-2025 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.protocol.packet;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.util.Codec;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ServerboundCustomClickActionPacket implements MinecraftPacket {

  private static final int MAX_PAYLOAD_SIZE = 64 * 1024;

  private static final Codec<BinaryTag, String, IOException, IOException> SNBT_CODEC = Codec.codec(
      encoded -> TagStringIO.tagStringIO().asTag(encoded),
      decoded -> TagStringIO.tagStringIO().asString(decoded)
  );

  private Key identifier;
  private int payloadSize;
  private BinaryTag payload;

  public ServerboundCustomClickActionPacket() {
  }

  public Key getIdentifier() {
    return this.identifier;
  }

  public @Nullable BinaryTagHolder payloadBinaryTagHolder() {
    if (this.payload != null && !(payload instanceof EndBinaryTag)) {
      try {
        return BinaryTagHolder.encode(this.payload, SNBT_CODEC);
      } catch (IOException e) {
        throw new RuntimeException("Could not encode tag payload", e);
      }
    }
    return null;
  }

  @Override
  public void decode(ByteBuf buf, Direction direction, ProtocolVersion version) {
    this.identifier = ProtocolUtils.readKey(buf);
    this.payloadSize = ProtocolUtils.readVarInt(buf);
    if (this.payloadSize > MAX_PAYLOAD_SIZE) {
      throw new IllegalArgumentException("Payload size " + this.payloadSize
          + " exceeds maximum of " + MAX_PAYLOAD_SIZE);
    }
    this.payload = ProtocolUtils.readBinaryTag(buf, version, BinaryTagIO.reader(this.payloadSize));
  }

  @Override
  public void encode(ByteBuf buf, Direction direction, ProtocolVersion version) {
    ProtocolUtils.writeKey(buf, this.identifier);
    ProtocolUtils.writeVarInt(buf, this.payloadSize);
    ProtocolUtils.writeBinaryTag(buf, version,
        this.payload == null ? EndBinaryTag.endBinaryTag() : this.payload);
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }

  @Override
  public int encodeSizeHint(Direction direction, ProtocolVersion version) {
    return ProtocolUtils.stringSizeHint(this.identifier.asString())
        + ProtocolUtils.varIntBytes(this.payloadSize)
        + this.payloadSize;
  }
}
