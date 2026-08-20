package net.exyl.shieldopt;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class ShieldOptimizerMod implements ClientModInitializer {

    private int previousSlot = -1;
    private boolean isAxeActive = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                resetState();
                return;
            }

            HitResult hit = client.crosshairTarget;

            if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hit;

                if (entityHit.getEntity() instanceof PlayerEntity targetPlayer) {
                    if (targetPlayer.isBlocking()) {
                        int axeSlot = findItemSlot(client, true);

                        if (axeSlot != -1) {
                            if (!isAxeActive) {
                                previousSlot = client.player.getInventory().selectedSlot;
                                isAxeActive = true;
                            }

                            if (client.player.getInventory().selectedSlot != axeSlot) {
                                client.player.getInventory().selectedSlot = axeSlot;
                            }
                        }
                        return;
                    }
                }
            }

            if (isAxeActive) {
                int swordSlot = findItemSlot(client, false);
                int targetSlot = (previousSlot != -1) ? previousSlot : swordSlot;

                if (targetSlot != -1 && client.player.getInventory().selectedSlot != targetSlot) {
                    client.player.getInventory().selectedSlot = targetSlot;
                }

                resetState();
            }
        });
    }

    private int findItemSlot(MinecraftClient client, boolean isAxe) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (isAxe && stack.getItem() instanceof AxeItem) {
                return i;
            } else if (!isAxe && stack.getItem() instanceof SwordItem) {
                return i;
            }
        }
        return -1;
    }

    private void resetState() {
        isAxeActive = false;
        previousSlot = -1;
    }
}
