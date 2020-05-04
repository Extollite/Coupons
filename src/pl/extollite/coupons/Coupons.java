package pl.extollite.coupons;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class Coupons extends PluginBase {
    private static final String format = "yyyy-MM-dd HH:mm:ss Z";

    private static Coupons instance;

    static Coupons getInstance() {
        return instance;
    }

    private String prefix;
    private String giveMsg;
    private Map<Item, List<String>> couponCommands = new HashMap<>();
    private Map<String, Item> coupons = new HashMap<>();
    private List<String> bannedWorlds;
    private Date flyEnd = null;
    private TaskHandler flyTask = null;
    private int flyEndWarning;
    private String flyEndMessage;
    private String flyEndWarningMsg;
    private String bannedWorldMessage;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        instance = this;
        List<String> authors = this.getDescription().getAuthors();
        this.getLogger().info(TextFormat.DARK_GREEN + "Plugin by " + authors.get(0));
        parseConfig();
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.getServer().getCommandMap().register("givecoupon", new CouponsCommand(this));
    }

    private void parseConfig() {
        Config configFile = getConfig();
        prefix = configFile.getString("prefix");
        giveMsg = configFile.getString("giveMessage");
        int flyStartDelay = configFile.getInt("flyStartDelay");
        flyEndWarning = configFile.getInt("flyEndWarning");
        flyEndMessage = configFile.getString("flyEndMessage");
        String flyBackToWork = configFile.getString("flyBackToWork");
        flyEndWarningMsg = configFile.getString("flyEndWarningMessage");
        bannedWorlds = configFile.getStringList("banned-worlds");
        bannedWorldMessage = configFile.getString("banned-worldMessage");
        try {
            String date = configFile.getString("flyEnd", "");
            if(!date.isEmpty()){
                flyEnd = new SimpleDateFormat(format).parse(date);
                if(flyEnd.after(new Date(System.currentTimeMillis()))){
                    flyEnd.setTime(flyEnd.getTime()+flyStartDelay*1000);
                    Duration d = Duration.between(LocalDateTime.now(), LocalDateTime.ofInstant(flyEnd.toInstant(),
                            ZoneId.systemDefault()));
                    getServer().getScheduler().scheduleDelayedTask(Coupons.getInstance(), () -> {
                        for(Player player : Coupons.getInstance().getServer().getOnlinePlayers().values()){
                            EssentialsAPI.getInstance().setCanFly(player, true);
                        }
                        Coupons.getInstance().getServer().broadcastMessage(Coupons.getInstance().getPrefix()+flyBackToWork);
                    }, flyStartDelay*20);
                    this.setFlyTask(getServer().getScheduler().scheduleDelayedTask(Coupons.getInstance(), () -> {
                        for(Player player : Coupons.getInstance().getServer().getOnlinePlayers().values()){
                            EssentialsAPI.getInstance().setCanFly(player, false);
                        }
                        Coupons.getInstance().setFlyEnd(null);
                        Coupons.getInstance().getServer().broadcastMessage(Coupons.getInstance().getPrefix()+Coupons.getInstance().getFlyEndMessage());
                    }, (int) (d.getSeconds()*20)));
                    for(int i = 1; i <= flyEndWarning; i++){
                        int finalI = i;
                        this.getServer().getScheduler().scheduleDelayedTask(this
                                , () ->
                                        this.getServer().broadcastMessage(Coupons.getInstance().getPrefix()+Coupons.getInstance().getFlyEndWarningMsg().replace("%time%", String.valueOf(finalI)))
                                , (int) ( (d.getSeconds() - i )*20));
                    }
                    configFile.set("flyEnd", new SimpleDateFormat(format).format(flyEnd));
                    configFile.save();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Config cfg = new Config(getDataFolder()+"/coupons.yml", Config.YAML);
        Set<String> keys = cfg.getKeys(false);
        if(keys.isEmpty()){
            cfg.load(getResource("coupons.yml"));
            cfg.save();
            keys = cfg.getKeys(false);
        }
        for(String key : keys){
            ConfigSection couponSection = cfg.getSection(key);
            Item item = Item.get(couponSection.getInt("id"));
            item.setCustomName(couponSection.getString("name"));
            item.setLore(couponSection.getStringList("lore").toArray(new String[0]));
            item.addEnchantment(Enchantment.get(0));
            coupons.put(key, item.clone());
            List<String> commandList = couponSection.getStringList("commands");
            commandList.add(couponSection.getString("message"));
            couponCommands.put(item.clone(), commandList);
        }
    }

    public Map<String, Item> getCoupons() {
        return coupons;
    }

    public Map<Item, List<String>> getCouponCommands() {
        return couponCommands;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getGiveMsg() {
        return giveMsg;
    }

    public Date getFlyEnd() {
        return flyEnd;
    }

    public void setFlyEnd(Date flyEnd) {
        this.flyEnd = flyEnd;
    }

    public TaskHandler getFlyTask() {
        return flyTask;
    }

    public void setFlyTask(TaskHandler flyTask) {
        this.flyTask = flyTask;
    }

    public static String getFormat() {
        return format;
    }

    public int getFlyEndWarning() {
        return flyEndWarning;
    }

    public String getFlyEndMessage() {
        return flyEndMessage;
    }

    public String getFlyEndWarningMsg() {
        return flyEndWarningMsg;
    }

    public List<String> getBannedWorlds() {
        return bannedWorlds;
    }

    public String getBannedWorldMessage() {
        return bannedWorldMessage;
    }
}
