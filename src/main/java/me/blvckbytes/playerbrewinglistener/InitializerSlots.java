/*
 * MIT License
 *
 * Copyright (c) 2023 BlvckBytes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.blvckbytes.playerbrewinglistener;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class InitializerSlots {

  private @Nullable WeakReference<Player> slot0, slot1, slot2;

  private @Nullable Player resolveWeakReference(@Nullable WeakReference<Player> reference, Runnable nullifier) {
    if (reference == null)
      return null;

    Player player = reference.get();

    // Item has been garbage collected
    if (player == null) {
      nullifier.run();
      return null;
    }

    // Player object hasn't yet been garbage-collected and the player went offline in the meantime
    if (!player.isOnline()) {
      nullifier.run();
      return null;
    }

    return player;
  }

  public void reset() {
    this.slot0 = null;
    this.slot1 = null;
    this.slot2 = null;
  }

  public @Nullable Player getSlotById(int id) {
    switch (id) {
      case 0:
        return resolveWeakReference(slot0, () -> slot0 = null);

      case 1:
        return resolveWeakReference(slot1, () -> slot1 = null);

      case 2:
        return resolveWeakReference(slot2, () -> slot2 = null);

      default:
        throw new IllegalStateException("Slot out of range [0;2]");
    }
  }

  public void setSlotById(int id, Player initializer) {
    switch (id) {
      case 0:
        slot0 = new WeakReference<>(initializer);
        break;

      case 1:
        slot1 = new WeakReference<>(initializer);
        break;

      case 2:
        slot2 = new WeakReference<>(initializer);
        break;

      default:
        throw new IllegalStateException("Slot out of range [0;2]");
    }
  }
}
