package de.eministarvr.tpa2;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class TPAPlugin extends JavaPlugin {

    private HashMap<Player, Player> tpaRequests = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin aktivieren
        System.out.println("TPAPlugin wurde aktiviert!");
    }

    @Override
    public void onDisable() {
        // Plugin deaktivieren
        System.out.println("TPAPlugin wurde deaktiviert!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl benutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("tpa")) {
            if (args.length == 0) {
                player.sendMessage("Bitte benutze: /tpa <Spieler>");
                return true;
            }
            String targetName = args[0];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer == null) {
                player.sendMessage("Der Spieler ist nicht online!");
                return true;
            }

            tpaRequests.put(targetPlayer, player);
            sendTPARequest(player, targetPlayer);
            player.sendMessage("Du hast " + targetPlayer.getName() + " eine Anfrage geschickt!");
            return true;

        } else if (label.equalsIgnoreCase("tpaaccept")) {
            if (args.length == 0) {
                player.sendMessage("Bitte benutze: /tpaaccept <Spieler>");
                return true;
            }
            String requesterName = args[0];
            Player requesterPlayer = Bukkit.getPlayer(requesterName);

            if (requesterPlayer == null || !tpaRequests.containsKey(player) || tpaRequests.get(player) != requesterPlayer) {
                player.sendMessage("Du hast keine Anfrage von diesem Spieler!");
                return true;
            }

            teleportPlayer(requesterPlayer, player.getLocation());
            tpaRequests.remove(player);
            return true;

        } else if (label.equalsIgnoreCase("tpadeny")) {
            if (args.length == 0) {
                player.sendMessage("Bitte benutze: /tpadeny <Spieler>");
                return true;
            }
            String requesterName = args[0];
            Player requesterPlayer = Bukkit.getPlayer(requesterName);

            if (requesterPlayer == null || !tpaRequests.containsKey(player) || tpaRequests.get(player) != requesterPlayer) {
                player.sendMessage("Du hast keine Anfrage von diesem Spieler!");
                return true;
            }

            requesterPlayer.sendMessage("Deine TPA-Anfrage wurde abgelehnt.");
            player.sendMessage("Du hast die Anfrage von " + requesterPlayer.getName() + " abgelehnt.");
            tpaRequests.remove(player);
            return true;

        } else if (label.equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;

        } else if (label.equalsIgnoreCase("credit")) {
            player.sendMessage(ChatColor.AQUA + "Dieses Plugin wurde von EministarVR erstellt.");
            player.sendMessage(ChatColor.GREEN + "Vielen Dank, dass du es benutzt!");
            return true;
        }

        return false;
    }

    private void sendTPARequest(Player sender, Player target) {
        TextComponent message = new TextComponent(sender.getName() + " möchte sich zu dir teleportieren. ");

        TextComponent acceptButton = new TextComponent("[Annehmen]");
        acceptButton.setColor(ChatColor.GREEN);
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaaccept " + sender.getName()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("Klicke hier, um anzunehmen.")}));

        TextComponent denyButton = new TextComponent(" [Ablehnen]");
        denyButton.setColor(ChatColor.RED);
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + sender.getName()));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("Klicke hier, um abzulehnen.")}));

        message.addExtra(acceptButton);
        message.addExtra(denyButton);

        target.spigot().sendMessage(message);
    }

    private void teleportPlayer(Player player, Location location) {
        player.teleportAsync(location).thenAccept(success -> {
            if (success) {
                player.sendMessage("Du wurdest erfolgreich teleportiert!");
            } else {
                player.sendMessage("Teleportation fehlgeschlagen!");
            }
        }).exceptionally(e -> {
            player.sendMessage("Ein Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace(); // Für Server-Logs zur Fehleranalyse
            return null;
        });
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "-------- TPA Plugin Hilfe --------");
        player.sendMessage(ChatColor.GOLD + "/tpa <Spieler>" + ChatColor.WHITE + " - Sende eine Teleportanfrage.");
        player.sendMessage(ChatColor.GOLD + "/tpaaccept <Spieler>" + ChatColor.WHITE + " - Akzeptiere eine Teleportanfrage.");
        player.sendMessage(ChatColor.GOLD + "/tpadeny <Spieler>" + ChatColor.WHITE + " - Lehne eine Teleportanfrage ab.");
        player.sendMessage(ChatColor.GOLD + "/credit" + ChatColor.WHITE + " - Zeigt Informationen über den Entwickler.");
        player.sendMessage(ChatColor.YELLOW + "----------------------------------");
    }
}
