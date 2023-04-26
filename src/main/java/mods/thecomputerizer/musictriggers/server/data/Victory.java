package mods.thecomputerizer.musictriggers.server.data;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Victory {

    private final int TIMEOUT;
    private final HashSet<Table> REFERNCES;
    private final Map<Table,Boolean> ACTIVE_REFERENCES;
    private final Map<Table,HashSet<Tuple<LivingEntity,MutableInt>>> ENTITY_CACHE;
    private final Map<Table,HashSet<Tuple<ServerBossEvent,MutableInt>>> BOSS_CACHE;
    private final Map<Table,HashSet<Tuple<ServerPlayer,MutableInt>>> PLAYER_CACHE;

    Victory(Stream<Table> references, int timeout) {
        this.TIMEOUT = timeout;
        this.REFERNCES = references.collect(Collectors.toCollection(HashSet::new));
        this.ACTIVE_REFERENCES = new HashMap<>();
        for(Table reference : this.REFERNCES)
            this.ACTIVE_REFERENCES.put(reference,false);
        this.ENTITY_CACHE = buildCache(reference -> reference.getName().matches("mob") &&
                !reference.getValOrDefault("resource_name",Collections.singletonList("any")).contains("BOSS"));
        this.BOSS_CACHE = buildCache(reference -> reference.getName().matches("mob") &&
                reference.getValOrDefault("resource_name",Collections.singletonList("any")).contains("BOSS"));
        this.PLAYER_CACHE = buildCache(reference -> reference.getName().matches("pvp"));
    }

    private <T> Map<Table, HashSet<Tuple<T,MutableInt>>> buildCache(Function<Table,Boolean> canAdd) {
        Map<Table, HashSet<Tuple<T,MutableInt>>> ret = new HashMap<>();
        for(Table reference : this.REFERNCES)
            if(canAdd.apply(reference))
                ret.put(reference,new HashSet<>());
        return ret;
    }

    @SuppressWarnings("unchecked")
    protected <T> void add(Table trigger, T obj) {
        try {
            if (trigger.getName().matches("mob")) {
                if (trigger.getValOrDefault("resource_name", Collections.singletonList("any")).contains("BOSS"))
                    innerAdd(trigger, obj, (Map<Table, HashSet<Tuple<T, MutableInt>>>) (Object) this.BOSS_CACHE);
                else innerAdd(trigger, obj, (Map<Table, HashSet<Tuple<T, MutableInt>>>) (Object) this.ENTITY_CACHE);
            } else innerAdd(trigger, obj, (Map<Table, HashSet<Tuple<T, MutableInt>>>) (Object) this.PLAYER_CACHE);
        }
        catch (ClassCastException e) {
            MusicTriggers.logExternally(Level.ERROR,"Something went wrong when attempting to add to a victory " +
                    "trigger! See the main log for the full stacktrace");
            Constants.MAIN_LOG.error("Something went wrong when attempting to add to a victory trigger! ",e);
        }
    }

    private <T> void innerAdd(Table trigger, T obj, Map<Table, HashSet<Tuple<T,MutableInt>>> backendMap) {
        if(backendMap.containsKey(trigger))
            backendMap.get(trigger).add(new Tuple<>(obj,new MutableInt(TIMEOUT)));
        else MusicTriggers.logExternally(Level.ERROR,"Tried to add to the wrong type of victory trigger for " +
                "trigger {}! This is an issue and should be reported!",trigger.getName()+"-"+
                trigger.getValOrDefault("identifier","not_set"));
    }

    protected void setActive(Table trigger, boolean status) {
        if(this.ACTIVE_REFERENCES.containsKey(trigger))
            this.ACTIVE_REFERENCES.put(trigger,status);
        else MusicTriggers.logExternally(Level.ERROR,"Tried to set the status of a victory trigger from the wrong " +
                "trigger {}! This is an issue and should be reported!",trigger.getName()+"-"+
                trigger.getValOrDefault("identifier","not_set"));
    }

    protected boolean runCalculation() {
        for(Table reference : this.REFERNCES) {
            if(this.ACTIVE_REFERENCES.get(reference)) continue;
            if(this.ENTITY_CACHE.containsKey(reference)) {
                int num = reference.getValOrDefault("level", 0);
                if (num > 0) {
                    boolean pass = false;
                    HashSet<Tuple<LivingEntity,MutableInt>> entities = this.ENTITY_CACHE.get(reference);
                    if (entities.size()==0) continue;
                    if (entities.size() >= num) {
                        float percent = reference.getValOrDefault("victory_percentage", 100f) / 100f;
                        float count = 0;
                        for (Tuple<LivingEntity,MutableInt> entityTuple : entities)
                            if (entityTuple.getA().isDeadOrDying() || entityTuple.getA().getHealth() <= 0f)
                                count++;
                        if (count / num >= percent) pass = true;
                    }
                    entities.removeIf(tuple -> tuple.getB().addAndGet(-5)<=0);
                    if(pass) return true;
                }
            }
            else if(this.BOSS_CACHE.containsKey(reference)) {
                int num = reference.getValOrDefault("level", 0);
                if (num > 0) {
                    boolean pass = false;
                    HashSet<Tuple<ServerBossEvent,MutableInt>> bossBars = this.BOSS_CACHE.get(reference);
                    if (bossBars.size()==0) continue;
                    if(bossBars.size() >= num) {
                        float percent = reference.getValOrDefault("victory_percentage", 100f) / 100f;
                        float count = 0;
                        for(Tuple<ServerBossEvent,MutableInt> bossTuple : bossBars)
                            if(bossTuple.getA().getProgress() <= 0f)
                                count++;
                        if(count / num >= percent) pass = true;
                    }
                    bossBars.removeIf(tuple -> tuple.getB().addAndGet(-5)<=0);
                    if(pass) return true;
                }
            }
            else if(this.PLAYER_CACHE.containsKey(reference)) {
                int num = reference.getValOrDefault("level", 0);
                if (num > 0) {
                    HashSet<Tuple<ServerPlayer, MutableInt>> players = this.PLAYER_CACHE.get(reference);
                    if (players.size() == 0) continue;
                    float percent = reference.getValOrDefault("victory_percentage", 100f) / 100f;
                    float count = 0;
                    for (Tuple<ServerPlayer, MutableInt> playerTuple : players)
                        if (playerTuple.getA().isDeadOrDying() || playerTuple.getA().getHealth() <= 0f)
                            count++;
                    players.removeIf(tuple -> tuple.getB().addAndGet(-5) <= 0);
                    if (count / num >= percent) return true;
                }
            }
        }
        return false;
    }
}