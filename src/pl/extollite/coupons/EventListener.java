package pl.extollite.coupons;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import cn.yescallop.essentialsnk.EssentialsAPI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EventListener implements Listener {
    public EventListener() {

    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent ev) {
        if (ev.isCancelled())
            return;
        if(ev.getFrom().getLevel() == null || ev.getTo().getLevel() == null)
            return;
        if (!ev.getFrom().getLevel().equals(ev.getTo().getLevel())) {
            if (Coupons.getInstance().getBannedWorlds().contains(ev.getTo().getLevel().getName())) {
                Coupons.getInstance().getServer().getScheduler().scheduleDelayedTask(Coupons.getInstance(), () -> {
                    EssentialsAPI.getInstance().setCanFly(ev.getPlayer(), false);
                }, 5);
            } else if (Coupons.getInstance().getFlyEnd() != null && Coupons.getInstance().getFlyEnd().after(new Date(System.currentTimeMillis()))) {
                Coupons.getInstance().getServer().getScheduler().scheduleDelayedTask(Coupons.getInstance(), () -> {
                    EssentialsAPI.getInstance().setCanFly(ev.getPlayer(), true);
                }, 5);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        if(ev.getPlayer() != null && ev.getPlayer().getAdventureSettings() != null)
            EssentialsAPI.getInstance().setCanFly(ev.getPlayer(), false);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        if (!Coupons.getInstance().getBannedWorlds().contains(ev.getPlayer().getLevel().getName())
                && Coupons.getInstance().getFlyEnd() != null && Coupons.getInstance().getFlyEnd().after(new Date(System.currentTimeMillis()))) {
            EssentialsAPI.getInstance().setCanFly(ev.getPlayer(), true);
        }
    }

    @EventHandler
    public void onExp(BlockBreakEvent ev){
        if(ev.getPlayer().getLevel().getName().equalsIgnoreCase("plotcreative") || ev.getPlayer().getLevel().getName().equalsIgnoreCase("bergwerk")){
            ev.setDropExp(0);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent ev) {
        if (ev.isCancelled())
            return;
        Player player = ev.getPlayer();
        if (player.isOp())
            return;
        if(player.hasPermission("fly.command.bypass"))
            return;
        String level = player.getLevel().getName();
        if (!Coupons.getInstance().getBannedWorlds().contains(level))
            return;
        String msg = ev.getMessage();
        msg = msg.trim();
        String[] cmd = msg.split("\\s+");
        if (cmd[0].substring(1).equals("fly")) {
            player.sendMessage(Coupons.getInstance().getPrefix()+Coupons.getInstance().getBannedWorldMessage());
            ev.setCancelled();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (ev.isCancelled())
            return;
        Item item = ev.getItem();
        if (item == null) {
            return;
        }
        for (Map.Entry<Item, List<String>> entry : Coupons.getInstance().getCouponCommands().entrySet()) {
            if (entry.getKey().equals(item)) {
                Player p = ev.getPlayer();
                List<String> commands = entry.getValue();
                String message = commands.remove(commands.size() - 1);
                CommandSender commandSender = Coupons.getInstance().getServer().getConsoleSender();
                for (String command : commands) {
                    if (command.startsWith("fly")) {
                        for (Player player : Coupons.getInstance().getServer().getOnlinePlayers().values()) {
                            if (!Coupons.getInstance().getBannedWorlds().contains(player.getLevel().getName())) {
                                EssentialsAPI.getInstance().setCanFly(player, true);
                            }
                        }
                        String[] cmd = command.split("\\s+");
                        int end = 900;
                        if (cmd.length == 2) {
                            end = Integer.parseInt(cmd[1]);
                        }
                        if (Coupons.getInstance().getFlyEnd() == null || Coupons.getInstance().getFlyEnd().before(new Date(System.currentTimeMillis()))) {
                            Coupons.getInstance().setFlyEnd(new Date(System.currentTimeMillis() + end * 1000));
                            Coupons.getInstance().setFlyTask(Coupons.getInstance().getServer().getScheduler().scheduleDelayedTask(Coupons.getInstance(), () -> {
                                for (int i = 1; i <= Coupons.getInstance().getFlyEndWarning(); i++) {
                                    int finalI = i;
                                    Coupons.getInstance().getServer().getScheduler().scheduleDelayedTask(Coupons.getInstance()
                                            , () ->
                                                    Coupons.getInstance().getServer().broadcastMessage(Coupons.getInstance().getPrefix() + Coupons.getInstance().getFlyEndWarningMsg().replace("%time%", String.valueOf(finalI)))
                                            , (Coupons.getInstance().getFlyEndWarning() - i) * 20);
                                }
                                Coupons.getInstance().getServer().getScheduler().scheduleDelayedTask(Coupons.getInstance()
                                        , () -> {
                                            for (Player player : Coupons.getInstance().getServer().getOnlinePlayers().values()) {
                                                EssentialsAPI.getInstance().setCanFly(player, false);
                                            }
                                            Coupons.getInstance().getServer().broadcastMessage(Coupons.getInstance().getPrefix() + Coupons.getInstance().getFlyEndMessage());
                                            Coupons.getInstance().setFlyEnd(null);
                                        }
                                        , (Coupons.getInstance().getFlyEndWarning()) * 20);
                            }, (end - Coupons.getInstance().getFlyEndWarning()) * 20));
                        } else {
                            Coupons.getInstance().setFlyEnd(new Date(Coupons.getInstance().getFlyEnd().getTime() + end * 1000));
                            Coupons.getInstance().getFlyTask().setDelay(Coupons.getInstance().getFlyTask().getDelay() + end * 20);
                        }
                        Config cfg = Coupons.getInstance().getConfig();
                        cfg.set("flyEnd", new SimpleDateFormat(Coupons.getFormat()).format(Coupons.getInstance().getFlyEnd()));
                        cfg.save();
                    } else {
                        for (Player player : Coupons.getInstance().getServer().getOnlinePlayers().values()) {
                            Coupons.getInstance().getServer().dispatchCommand(commandSender, command.replace("%player_name%", player.getName()));
                        }
                    }
                }
                commands.add(message);
                Coupons.getInstance().getServer().broadcastMessage(Coupons.getInstance().getPrefix() + message.replace("%player_name%", p.getName()));
                item.setCount(item.getCount() - 1);
                if(item.getCount() > 0){
                    p.getInventory().setItemInHand(item);
                }
                else{
                    p.getInventory().setItemInHand(new Item(BlockID.AIR));
                }
                p.sendAllInventories();
                ev.setCancelled();
                return;
            }
        }
    }

}
