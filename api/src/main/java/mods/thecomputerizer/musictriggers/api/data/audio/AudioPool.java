package mods.thecomputerizer.musictriggers.api.data.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTLoadable;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.BaseTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.ListTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.TagHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AudioPool extends AudioRef implements NBTLoadable {

    private final Set<AudioRef> audio;
    private final Set<AudioRef> playableAudio;
    private final TriggerAPI trigger;
    @Getter private AudioRef queuedAudio;

    public AudioPool(TriggerAPI trigger) {
        super(trigger.getChannel(),"audio_pool");
        this.audio = new HashSet<>();
        this.playableAudio = new HashSet<>();
        this.trigger = trigger;
    }

    @Override public void close() {
        super.close();
        this.audio.clear();
        this.playableAudio.clear();
        this.queuedAudio = null;
    }

    @Override public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,"pool");
        NetworkHelper.writeSet(buf,this.audio,ref -> ref.encode(buf));
    }
    
    @Override public boolean equals(Object other) {
        if(other instanceof AudioPool) {
            AudioPool pool = (AudioPool)other;
            return equivalent(pool.channel,pool.audio);
        }
        return false;
    }
    
    public boolean equivalent(ChannelAPI channel, Collection<AudioRef> otherAudio) {
        if(this.channel!=channel) return false;
        for(AudioRef ref : this.audio) {
            boolean matched = false;
            for(AudioRef otherRef : otherAudio) {
                if(ref.equals(otherRef)) {
                    matched = true;
                    break;
                }
            }
            if(!matched) return false;
        }
        return true;
    }
    
    public @Nullable AudioRef getAudioForName(String name) {
        for(AudioRef ref : this.audio)
            if(ref.getName().equals(name)) return ref;
        return null;
    }
    
    public List<AudioRef> getFlattened() {
        List<AudioRef> flattened = new ArrayList<>();
        getFlattened(flattened,this);
        return flattened;
    }
    
    private void getFlattened(List<AudioRef> flattened, AudioPool pool) {
        for(AudioRef ref : pool.audio) {
            if(ref instanceof AudioPool) getFlattened(flattened,(AudioPool)ref);
            else flattened.add(ref);
        }
    }

    @Override public @Nullable InterruptHandler getInterruptHandler() {
        return Objects.nonNull(this.queuedAudio) ? this.queuedAudio.getInterruptHandler() : null;
    }
    
    @Override public String getName() {
        StringJoiner refJoinfer = new StringJoiner("+");
        for(AudioRef ref : this.audio) refJoinfer.add(ref.getName());
        return this.audio.size()==1 ? refJoinfer.toString() : "pool = "+refJoinfer;
    }
    
    @Override public @Nullable Parameter<?> getParameter(String name) {
        return Objects.nonNull(this.queuedAudio) ? this.queuedAudio.getParameter(name) : super.getParameter(name);
    }
    
    @Override public double getSpeed() {
        return Objects.nonNull(this.queuedAudio) ? this.queuedAudio.getSpeed() : 1d;
    }

    @Override public float getVolume(boolean unpaused) {
        return Objects.nonNull(this.queuedAudio) ? this.queuedAudio.getVolume(unpaused) : 0f;
    }

    public boolean hasAudio() {
        if(this.playableAudio.isEmpty()) recalculatePlayable(i -> i<=1);
        return !this.playableAudio.isEmpty();
    }
    
    @Override public boolean hasDataToSave() {
        for(AudioRef ref : this.audio)
            if(!this.playableAudio.contains(ref) && ref.getPlayState()==4) return true;
        return false;
    }
    
    public void injectHandlers(AudioRef ref, Collection<ChannelEventHandler> handlers) {
        this.audio.add(ref);
        this.playableAudio.add(ref);
        handlers.add(this);
        handlers.addAll(ref.loops);
    }
    
    @Override public boolean isLoaded() {
        return Objects.nonNull(this.queuedAudio) && !this.queuedAudio.isLoading() && this.queuedAudio.isLoaded();
    }
    
    @Override public boolean isLooping() {
        return Objects.nonNull(this.queuedAudio) && this.queuedAudio.looping;
    }
    
    @Override public boolean isQueued() {
        return hasQueue() && this.queuedAudio.isQueued();
    }
    
    public boolean hasQueue() {
        return Objects.nonNull(this.queuedAudio);
    }
    
    /**
     * Only accessible from the server side since audio is only played on the client
     */
    public void markPlayed(AudioRef ref) {
        if(!this.channel.isClientChannel()) this.playableAudio.remove(ref);
        else logError("Tried to manually mark {} as played which should only be done on the server side!");
    }
    
    @Override public void onConnected(CompoundTagAPI<?> worldData) {
        if(worldData.contains("audio")) {
            for(BaseTagAPI<?> audio : worldData.getListTag("audio")) {
                CompoundTagAPI<?> audioTag = audio.asCompoundTag();
                AudioRef ref = getAudioForName(audioTag.getString("name"));
                if(Objects.nonNull(ref) && hasPlayedEnough(audioTag.getPrimitiveTag("play_count").asInt()))
                    this.playableAudio.remove(ref);
            }
        }
    }
    
    public void onDisconnected() {
        recalculatePlayable(i -> i==3);
    }
    
    @Override public void onLoaded(CompoundTagAPI<?> globalData) {}

    @Override public void play(boolean unpaused) {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.play(unpaused);
    }
    
    @Override public void playable() {
        recalculatePlayable(i -> i<=2);
    }

    @Override public void playing(boolean unpaused) {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.playing(unpaused);
    }
    
    @Override public void queryInterrupt(@Nullable TriggerAPI next, AudioPlayer player) {
        if(Objects.isNull(this.queuedAudio)) this.channel.getPlayer().stopCurrentTrack();
        else this.queuedAudio.queryInterrupt(trigger,player);
    }

    @Override public void queue() {
        if(this.playableAudio.isEmpty()) return;
        AudioRef nextQueue = RandomHelper.getWeightedEntry(ThreadLocalRandom.current(),this.playableAudio);
        if(Objects.isNull(nextQueue)) return;
        if(nextQueue instanceof AudioPool) nextQueue.queue();
        this.queuedAudio = nextQueue;
    }
    
    protected void recalculatePlayable(Function<Integer,Boolean> func) {
        for(AudioRef ref : this.audio)
            if(func.apply(ref.getPlayState())) this.playableAudio.add(ref);
    }
    
    @Override public void saveGlobalTo(CompoundTagAPI<?> globalData) {}
    
    @Override public void saveWorldTo(CompoundTagAPI<?> worldData) {
        List<AudioRef> played = this.audio.stream()
                .filter(ref -> !this.playableAudio.contains(ref) && ref.getPlayState()==4)
                .collect(Collectors.toList());
        if(played.isEmpty()) return;
        ListTagAPI<?> tag = TagHelper.makeListTag();
        for(AudioRef ref : played) {
            CompoundTagAPI<?> audioTag = TagHelper.makeCompoundTag();
            audioTag.putString("name",ref.getName());
            audioTag.putInt("play_count",1);
            tag.addTag(audioTag);
        }
        worldData.putTag("audio",tag);
    }
    
    @Override public void setUniversals(UniversalParameters universals) {
        super.setUniversals(universals);
        for(AudioRef ref : this.audio) ref.setUniversals(universals);
    }

    @Override public void start(TriggerAPI trigger, boolean unpaused) {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.start(trigger,unpaused);
        else logWarn("Tried to start empty queue from {}",trigger);
    }

    @Override public void stop() {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.stop();
    }

    @Override public void stopped() {
        if(Objects.nonNull(this.queuedAudio)) {
            this.queuedAudio.stopped();
            if(this.queuedAudio.getPlayState()>0) this.playableAudio.remove(this.queuedAudio);
            if(this.playableAudio.isEmpty()) recalculatePlayable(i -> i<=1);
            this.queuedAudio = null;
        }
    }
}
