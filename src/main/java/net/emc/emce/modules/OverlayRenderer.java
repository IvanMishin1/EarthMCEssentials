package net.emc.emce.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.emc.emce.EarthMCEssentials;
import net.emc.emce.config.ModConfig;
import net.emc.emce.utils.ModUtils;
import net.emc.emce.utils.ModUtils.State;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Iterator;
import java.util.List;

public class OverlayRenderer {
    public static void render(MatrixStack matrixStack) {
        if (!EarthMCEssentials.instance().getConfig().general.enableMod || !EarthMCEssentials.instance().shouldRender())
            return;

        final TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

        ModConfig config = EarthMCEssentials.instance().getConfig();

        State townlessState = config.townless.positionState;
        State nearbyState = config.nearby.positionState;

        List<String> townless = EarthMCEssentials.instance().getTownless();
        JsonArray nearby = EarthMCEssentials.instance().getNearbyPlayers();
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null)
            return;

        if (config.townless.enabled) {
            if (!config.townless.presetPositions) {
                // Position of the first player, who determines where the list will be.
                int townlessPlayerOffset = config.townless.yPos;

                Formatting townlessTextFormatting = Formatting.byName(config.townless.headingTextColour.name());
                MutableText townlessText = new TranslatableText("text_townless_header", townless.size()).formatted(townlessTextFormatting);

                // Draw heading.
                renderer.drawWithShadow(matrixStack, townlessText, config.townless.xPos, config.townless.yPos - 15, 16777215);

                if (EarthMCEssentials.instance().getTownless().size() > 0)
                {
                    int index = 0;
                    Iterator<String> it = townless.iterator();

                    while (it.hasNext())
                    {
                        String name = it.next();
                        Formatting playerTextFormatting = Formatting.byName(config.townless.playerTextColour.name());

                        if (config.townless.maxLength >= 1) {
                            if (index >= config.townless.maxLength) {
                                MutableText remainingText = new TranslatableText("text_townless_remaining", EarthMCEssentials.instance().getTownless().size()-index).formatted(playerTextFormatting);
                                renderer.drawWithShadow(matrixStack, remainingText, config.townless.xPos, townlessPlayerOffset, 16777215);
                                break;
                            }
                            index++;
                        }

                        MutableText playerName = new TranslatableText(name).formatted(playerTextFormatting);
                        renderer.drawWithShadow(matrixStack, playerName, config.townless.xPos, townlessPlayerOffset, 16777215);

                        // Add offset for the next player.
                        townlessPlayerOffset += 10;
                    }
                }
            } else { // No advanced positioning, use preset states.
                int townlessLongest, nearbyLongest;

                townlessLongest = Math.max(ModUtils.getLongestElement(townless), ModUtils.getTextWidth(new TranslatableText("text_townless_header", townless.size())));
                nearbyLongest = Math.max(ModUtils.getNearbyLongestElement(EarthMCEssentials.instance().getNearbyPlayers()), ModUtils.getTextWidth(new TranslatableText("text_nearby_header", nearby.size())));

                switch (townlessState) {
                    case TOP_MIDDLE -> {
                        if (nearbyState.equals(State.TOP_MIDDLE))
                            townlessState.setX(ModUtils.getWindowWidth() / 2 - (townlessLongest + nearbyLongest) / 2);
                        else
                            townlessState.setX(ModUtils.getWindowWidth() / 2 - townlessLongest / 2);

                        townlessState.setY(16);
                    }
                    case TOP_RIGHT -> {
                        townlessState.setX(ModUtils.getWindowWidth() - townlessLongest - 5);
                        townlessState.setY(ModUtils.getStatusEffectOffset(client.player.getStatusEffects()));
                    }
                    case LEFT -> {
                        townlessState.setX(5);
                        townlessState.setY(ModUtils.getWindowHeight() / 2 - ModUtils.getTownlessArrayHeight(townless, config.townless.maxLength) / 2);
                    }
                    case RIGHT -> {
                        townlessState.setX(ModUtils.getWindowWidth() - townlessLongest - 5);
                        townlessState.setY(ModUtils.getWindowHeight() / 2 - ModUtils.getTownlessArrayHeight(townless, config.townless.maxLength) / 2);
                    }
                    case BOTTOM_RIGHT -> {
                        townlessState.setX(ModUtils.getWindowWidth() - townlessLongest - 5);
                        townlessState.setY(ModUtils.getWindowHeight() - ModUtils.getTownlessArrayHeight(townless, config.townless.maxLength) - 22);
                    }
                    case BOTTOM_LEFT -> {
                        townlessState.setX(5);
                        townlessState.setY(ModUtils.getWindowHeight() - ModUtils.getTownlessArrayHeight(townless, config.townless.maxLength) - 22);
                    }
                    default -> // Defaults to top left
                    {
                        townlessState.setX(5);
                        townlessState.setY(16);
                    }
                }

                Formatting townlessTextFormatting = Formatting.byName(config.townless.headingTextColour.name());
                MutableText townlessText = new TranslatableText("text_townless_header", townless.size()).formatted(townlessTextFormatting);

                // Draw heading.
                renderer.drawWithShadow(matrixStack, townlessText, townlessState.getX(), townlessState.getY() - 10, 16777215);

                int rendered = 0;
                Iterator<String> it = townless.iterator();

                while (it.hasNext())
                {
                    String townlessName = it.next();
                    Formatting playerTextFormatting = Formatting.byName(config.townless.playerTextColour.name());

                    if (config.townless.maxLength > 0 && rendered >= config.townless.maxLength) {
                        MutableText remainingText = new TranslatableText("text_townless_remaining", townless.size() - rendered).formatted(playerTextFormatting);
                        renderer.drawWithShadow(matrixStack, remainingText, townlessState.getX(), townlessState.getY() + rendered*10, 16777215);
                        break;
                    }

                    MutableText playerName = new TranslatableText(townlessName).formatted(playerTextFormatting);

                    renderer.drawWithShadow(matrixStack, playerName, townlessState.getX(), townlessState.getY() + rendered++*10, 16777215);
                }
            }
        }

        if (config.nearby.enabled && ModUtils.isConnectedToEMC()) {
            if (!config.nearby.presetPositions) // Not using preset positions
            {
                // Position of the first player, who determines where the list will be.
                int nearbyPlayerOffset = config.nearby.yPos;

                Formatting nearbyTextFormatting = Formatting.byName(config.nearby.headingTextColour.name());
                MutableText nearbyText = new TranslatableText("text_nearby_header", nearby.size()).formatted(nearbyTextFormatting);

                // Draw heading.
                renderer.drawWithShadow(matrixStack, nearbyText, config.nearby.xPos, config.nearby.yPos - 15, 16777215);

                if (nearby.size() >= 1) {
                    if (client.player == null) return;

                    for (int i = 0; i < nearby.size(); i++) {
                        JsonObject currentPlayer = nearby.get(i).getAsJsonObject();
                        int distance = Math.abs(currentPlayer.get("x").getAsInt() - (int) client.player.getX()) +
                                Math.abs(currentPlayer.get("z").getAsInt() - (int) client.player.getZ());

                        if (currentPlayer.get("name").getAsString().equals(EarthMCEssentials.instance().getClientResident().getName()))
                            continue;

                        Formatting playerTextFormatting = Formatting.byName(config.nearby.playerTextColour.name());
                        MutableText playerText = new TranslatableText(currentPlayer.get("name").getAsString(), distance).formatted(playerTextFormatting);

                        renderer.drawWithShadow(matrixStack, playerText, config.nearby.xPos, nearbyPlayerOffset, 16777215);

                        // Add offset for the next player.
                        nearbyPlayerOffset += 10;
                    }
                }
            } else {
                int nearbyLongest, townlessLongest;

                nearbyLongest = Math.max(ModUtils.getNearbyLongestElement(EarthMCEssentials.instance().getNearbyPlayers()), ModUtils.getTextWidth(new TranslatableText("text_nearby_header", nearby.size())));
                townlessLongest = Math.max(ModUtils.getLongestElement(townless), ModUtils.getTextWidth(new TranslatableText("text_townless_header", townless.size())));

                switch (nearbyState) {
                    case TOP_MIDDLE -> {
                        if (townlessState.equals(State.TOP_MIDDLE)) {
                            nearbyState.setX(ModUtils.getWindowWidth() / 2 - (townlessLongest + nearbyLongest) / 2 + townlessLongest + 5);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(ModUtils.getWindowWidth() / 2 - nearbyLongest / 2);
                            nearbyState.setY(16);
                        }
                    }
                    case TOP_RIGHT -> {
                        if (townlessState.equals(State.TOP_RIGHT))
                            nearbyState.setX(ModUtils.getWindowWidth() - townlessLongest - nearbyLongest - 15);
                        else
                            nearbyState.setX(ModUtils.getWindowWidth() - nearbyLongest - 5);

                        if (client.player != null)
                            nearbyState.setY(ModUtils.getStatusEffectOffset(client.player.getStatusEffects()));
                    }
                    case LEFT -> {
                        if (townlessState.equals(State.LEFT)) {
                            nearbyState.setX(townlessLongest + 10);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(5);
                            nearbyState.setY(ModUtils.getWindowHeight() / 2 - ModUtils.getArrayHeight(nearby) / 2);
                        }
                    }
                    case RIGHT -> {
                        if (townlessState.equals(State.RIGHT)) {
                            nearbyState.setX(ModUtils.getWindowWidth() - townlessLongest - nearbyLongest - 15);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(ModUtils.getWindowWidth() - nearbyLongest - 5);
                            nearbyState.setY(ModUtils.getWindowHeight() / 2 - ModUtils.getArrayHeight(nearby) / 2);
                        }
                    }
                    case BOTTOM_RIGHT -> {
                        if (townlessState.equals(State.BOTTOM_RIGHT)) {
                            nearbyState.setX(ModUtils.getWindowWidth() - townlessLongest - nearbyLongest - 15);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(ModUtils.getWindowWidth() - nearbyLongest - 15);
                            nearbyState.setY(ModUtils.getWindowHeight() - ModUtils.getArrayHeight(nearby) - 10);
                        }
                    }
                    case BOTTOM_LEFT -> {
                        if (townlessState.equals(State.BOTTOM_LEFT)) {
                            nearbyState.setX(townlessLongest + 15);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(5);
                            nearbyState.setY(ModUtils.getWindowHeight() - ModUtils.getArrayHeight(nearby) - 10);
                        }
                    }
                    default -> // Defaults to top left
                    {
                        if (townlessState.equals(State.TOP_LEFT))
                            nearbyState.setX(townlessLongest + 15);
                        else
                            nearbyState.setX(5);

                        nearbyState.setY(16);
                    }
                }

                Formatting nearbyTextFormatting = Formatting.byName(config.nearby.headingTextColour.name());
                MutableText nearbyText = new TranslatableText("text_nearby_header", nearby.size()).formatted(nearbyTextFormatting);

                // Draw heading.
                renderer.drawWithShadow(matrixStack, nearbyText, nearbyState.getX(), nearbyState.getY() - 10, 16777215);

                if (nearby.size() >= 1)
                {
                    if (client.player == null) return;

                    for (int i = 0; i < nearby.size(); i++)
                    {
                        JsonObject currentPlayer = nearby.get(i).getAsJsonObject();

                        JsonElement xElement = currentPlayer.get("x");
                        JsonElement zElement = currentPlayer.get("z");
                        if (xElement == null || zElement == null) continue;

                        int distance = Math.abs(xElement.getAsInt() - (int) client.player.getX()) +
                                Math.abs(zElement.getAsInt() - (int) client.player.getZ());

                        if (EarthMCEssentials.instance().getClientResident() != null && currentPlayer.get("name").getAsString().equals(EarthMCEssentials.instance().getClientResident().getName()))
                            continue;

                        String prefix = "";

                        if (config.nearby.showRank) {
                            if (!currentPlayer.has("town")) prefix = "(Townless) ";
                            else prefix = "(" + currentPlayer.get("rank").getAsString() + ") ";
                        }

                        Formatting playerTextFormatting = Formatting.byName(config.nearby.playerTextColour.name());
                        MutableText playerText = new TranslatableText(prefix + currentPlayer.get("name").getAsString() + ": " + distance + "m").formatted(playerTextFormatting);

                        renderer.drawWithShadow(matrixStack, playerText, nearbyState.getX(), nearbyState.getY() + 10 * i, 16777215);
                    }
                }
            }
        }
    }
}
