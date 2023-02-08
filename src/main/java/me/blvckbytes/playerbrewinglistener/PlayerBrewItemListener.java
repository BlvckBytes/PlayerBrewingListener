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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerBrewItemListener implements Listener {

  private final Map<Block, InitializerSlots> initializersByBlock;

  public PlayerBrewItemListener() {
    this.initializersByBlock = new HashMap<>();
  }

  @EventHandler
  public void onDrag(InventoryDragEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
      return;

    Player player = (Player) e.getWhoClicked();

    Inventory inventory = e.getInventory();
    if (inventory.getType() != InventoryType.BREWING)
      return;

    BrewingStand brewingStand = ((BrewerInventory) inventory).getHolder();
    if (brewingStand == null)
      return;

    Block brewingStandBlock = brewingStand.getBlock();
    InitializerSlots initializerSlots = initializersByBlock.computeIfAbsent(brewingStandBlock, k -> new InitializerSlots());

    for (int slot : e.getRawSlots()) {
      if (!isPotionSlot(slot))
        continue;

      initializerSlots.setSlotById(slot, player);
    }
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
      return;

    Player player = (Player) e.getWhoClicked();

    Inventory topInventory = player.getOpenInventory().getTopInventory();
    if (topInventory.getType() != InventoryType.BREWING)
      return;

    BrewingStand brewingStand = ((BrewerInventory) topInventory).getHolder();
    if (brewingStand == null)
      return;

    Inventory clickedInventory = e.getClickedInventory();
    InventoryAction action = e.getAction();
    int slot = e.getSlot();

    Block brewingStandBlock = brewingStand.getBlock();
    InitializerSlots initializerSlots = initializersByBlock.computeIfAbsent(brewingStandBlock, k -> new InitializerSlots());

    // Moved to another inventory, take the item that has been shift clicked on
    if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {

      // Moved from bottom up, figure out which slot this item went into
      if (clickedInventory != null && clickedInventory != topInventory) {
        ItemStack shifted = clickedInventory.getItem(e.getSlot());

        // 0-3 should be the three potion slots (let's hope this is X-version)
        for (int i = 0; i < 3; i++) {
          ItemStack item = topInventory.getItem(i);

          if (
            // Vacant slot
            (item == null || item.getType() == Material.AIR) ||
            // Or stackable
            (item.getAmount() < item.getMaxStackSize() && item.isSimilar(shifted))
          ) {
            initializerSlots.setSlotById(i, player);
            return;
          }
        }
      }

      return;
    }

    // Not a potion slot, ignore
    if (!isPotionSlot(slot))
      return;

    if (
      // Placed an item into the slot
      (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_SOME || action == InventoryAction.PLACE_ONE) ||
      // Swapped with the slot but in effect placed something else there again
      action == InventoryAction.SWAP_WITH_CURSOR
    ) {
      // Has to happen in the brewing stand, of course
      if (e.getClickedInventory() == topInventory)
        initializerSlots.setSlotById(e.getSlot(), player);

      return;
    }

    // Swapped with a slot in the hotbar by hovering over the item and pressing a key in [1;9]
    if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
      ItemStack hotbarItem = player.getInventory().getItem(e.getHotbarButton());

      // Swapped with a potion or put a potion in
      if (hotbarItem != null && hotbarItem.getType().name().contains("POTION"))
        initializerSlots.setSlotById(slot, player);
    }
  }

  @EventHandler
  public void onBrew(BrewEvent e) {
    InitializerSlots initializerSlots = initializersByBlock.get(e.getBlock());

    if (initializerSlots == null)
      return;

    for (int i = 0; i <= 2; i++) {
      Player player = initializerSlots.getSlotById(i);
      ItemStack item = e.getResults().get(i);

      // This slot yields no result, skip it
      if (player == null || item == null || item.getType() == Material.AIR)
        continue;

      PlayerBrewEvent event = new PlayerBrewEvent(player, item, e);
      Bukkit.getPluginManager().callEvent(event);
    }

    // Reset slot initializer state after brewing to start anew
    initializerSlots.reset();
  }

  @EventHandler
  public void onBreak(BlockBreakEvent e) {
    // Release the initializer tracker if the underlying block is destroyed
    initializersByBlock.remove(e.getBlock());
  }

  private boolean isPotionSlot(int slot) {
    return slot <= 2;
  }
}
