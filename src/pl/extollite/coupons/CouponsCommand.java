package pl.extollite.coupons;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouponsCommand extends CommandManager {

    private Coupons plugin;

    public CouponsCommand(Coupons plugin) {
        super(plugin, "givecoupon", "givecoupon command", "/givecoupon <player_name> <name>");
        this.plugin = plugin;
        Map<String, CommandParameter[]> parameters = new HashMap<>();
        parameters.put("set", new CommandParameter[]{
                new CommandParameter("Player Name", CommandParamType.TARGET, false),
                new CommandParameter("Coupons Name", false, plugin.getCoupons().keySet().toArray(new String[0]))
        });
        this.setCommandParameters(parameters);
        this.setPermission("coupons.command");
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.isOp() && !sender.hasPermission("coupons.command")) {
                return true;
            }
        }
        if (args.length == 2) {
            Item item = plugin.getCoupons().get(args[1]);
            if(item != null){
                Player p = this.plugin.getServer().getPlayerExact(args[0]);
                if(p.getInventory().canAddItem(item)){
                    p.getInventory().addItem(item.clone());
                    p.sendAllInventories();
                }
                else{
                    p.dropItem(item.clone());
                }
                p.sendMessage(plugin.getPrefix()+plugin.getGiveMsg().replace("%coupon_name%", item.getName()));
                sender.sendMessage(plugin.getPrefix()+"Coupon was given to player!");
            }
            else{
                sender.sendMessage(plugin.getPrefix()+"This coupon don't exists!");
            }
            return true;
        }
        sender.sendMessage(plugin.getPrefix()+TextFormat.GREEN + "Usage: ");
        sender.sendMessage(TextFormat.GREEN + "/givecoupon <player_name> <name>");
        return true;
    }
}

