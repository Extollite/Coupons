package pl.extollite.coupons;

import cn.nukkit.command.Command;
import cn.nukkit.command.PluginIdentifiableCommand;

public abstract class CommandManager extends Command implements PluginIdentifiableCommand {
    private Coupons plugin;

    public CommandManager(Coupons plugin, String name, String desc, String usage) {
        super(name, desc, usage);

        this.plugin = plugin;
    }

    public Coupons getPlugin() {
        return plugin;
    }
}
