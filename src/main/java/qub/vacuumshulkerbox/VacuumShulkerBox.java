package qub.vacuumshulkerbox;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class VacuumShulkerBox extends JavaPlugin implements Listener {
    Set<Material> shulkerBoxes = Tag.SHULKER_BOXES.getValues();
    private FileConfiguration config;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        return true;
    }

    private void fillConfig() {
        this.getConfig().options().copyDefaults();
        this.saveDefaultConfig();
        this.config = this.getConfig();
    }

    private void matchConfig() {

    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        fillConfig();
        matchConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean hasShulker(Inventory inv)
    {
        for(Material box : shulkerBoxes)
        {
            if(inv.contains(box))
                return true;
        }
        return false;
    }
    public void updateShulker(BlockStateMeta im, ShulkerBox box, ItemStack item) {
        im.setBlockState(box);
        item.setItemMeta(im);
    }
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent ev) {
        if (ev.getEntity() instanceof Player) {
            Player player = (Player) ev.getEntity();
            PlayerInventory playerInventory = player.getInventory();
            ItemStack itemStack = ev.getItem().getItemStack();

            if (playerInventory.getType() != InventoryType.SHULKER_BOX     // Bypass if currently using shulker
            && hasShulker(playerInventory)
            && !playerInventory.contains(itemStack.getType()))
            {
                int maxStackSize = itemStack.getMaxStackSize();
                int pickupLeft = itemStack.getAmount();
                for (ItemStack i : player.getInventory().getContents()) {
                    if (i != null) {
                        if (i.getType().toString().contains("SHULKER_BOX")) {
                            if (i.getItemMeta() instanceof BlockStateMeta) {
                                BlockStateMeta im = (BlockStateMeta) i.getItemMeta();
                                ShulkerBox box = (ShulkerBox) im.getBlockState();
                                if (box.getInventory().contains(itemStack.getType())) {
                                    for (ItemStack seeker : box.getInventory()) {
                                        if (seeker == null) {
                                            continue;
                                        }
                                        if (seeker.isSimilar(itemStack)) {
                                            if (seeker.getAmount() >= maxStackSize) {
                                                continue;
                                            } else if (seeker.getAmount() + pickupLeft <= maxStackSize) {
                                                seeker.add(pickupLeft);
                                                pickupLeft = 0;
                                                updateShulker(im, box, i);
                                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, Util.getRandomNumber(0.2f, 0.35f), Util.getRandomNumber(0.7f, 0.1f) * 2);
                                                ev.getItem().remove();
                                                ev.setCancelled(true);
                                                break;
                                            } else {
                                                pickupLeft -= maxStackSize - seeker.getAmount();
                                                seeker.setAmount(maxStackSize);
                                                updateShulker(im, box, i);
                                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, Util.getRandomNumber(0.5f, 0.75f), Util.getRandomNumber(0.5f, 0.75f));
                                            }
                                        }
                                    }
                                    while (pickupLeft > 0) {
                                        if (box.getInventory().firstEmpty() != -1) {
                                            if (pickupLeft > maxStackSize) {
                                                box.getInventory().setItem(box.getInventory().firstEmpty(), new ItemStack(itemStack.getType(), maxStackSize));
                                                pickupLeft -= maxStackSize;
                                                updateShulker(im, box, i);
                                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, Util.getRandomNumber(0.5f, 0.75f), Util.getRandomNumber(0.5f, 0.75f));
                                            } else {
                                                box.getInventory().setItem(box.getInventory().firstEmpty(), new ItemStack(itemStack.getType(), pickupLeft));
                                                pickupLeft = 0;
                                                updateShulker(im, box, i);
                                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, Util.getRandomNumber(0.5f, 0.75f), Util.getRandomNumber(0.5f, 0.75f));
                                                ev.getItem().remove();
                                                ev.setCancelled(true);
                                                break;
                                            }
                                        } else {
                                            ev.getItem().setItemStack(new ItemStack(itemStack.getType(), pickupLeft));
                                            updateShulker(im, box, i);
                                            player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, Util.getRandomNumber(0.5f, 0.75f), Util.getRandomNumber(0.5f, 0.75f));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
