package mods.thecomputerizer.musictriggers.config;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.gui.Mappings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

@SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
public class ConfigObject {
    private final Map<String, String> songholder;
    private final Map<String, Map<String, String[]>> triggerholder;
    private final Map<String, Map<String, String>> triggerMapper;
    private final Map<String, String[]> otherinfo;
    private final Map<String, Map<String, String[]>> otherlinkinginfo;
    private final Map<String, Map<String, String[]>> triggerlinking;
    private final Map<String, Map<Integer, String[]>> loopPoints;
    private final Map<String, Map<String, Map<Integer, String[]>>> linkingLoopPoints;
    private final Map<Integer, ConfigTitleCards.Title> titlecards;
    private final Map<Integer, ConfigTitleCards.Image> imagecards;
    private final Map<Integer, Boolean> ismoving;
    private final List<String> blockedmods;
    private final List<String> debugStuff;

    private final Map<String, List<Integer>> markSongInfoForWriting;
    private final Map<String, Map<String, List<Integer>>> markTriggerInfoForWriting;
    private final Map<String, Map<String, List<Integer>>> markLinkingInfoForWriting;
    private final Map<Integer, List<Integer>> markTitleInfoForWriting;
    private final Map<Integer, List<Integer>> markImageInfoForWriting;

    private final File mainConfig;
    private final File titleCardConfig;
    private final File debugConfig;
    private final File registrationConfig;

    public static final String[] otherInfoDefaults = new String[]{"1", "0", "false", "100", "1", "0", "0"};
    public static final String[] triggerInfoDefaults = new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
            "minecraft", "_", "16", "false", "100", "100", "100",
            "false", "0", "minecraft", "true", "true", "0", "0", "nope",
            "nope", "-111", "false","_", "true", "-1", "-111", "true",
            "false", "false", "false", "0", "minecraft"};
    public static final String[] linkingInfoDefaults = new String[]{"1", "1", "0", "0"};
    public static final String[] titleInfoDefaults = new String[]{"false", "red", "white", "false"};
    public static final String[] imageInfoDefaults = new String[]{"name", "750","0", "0", "100", "100", "false", "10", "10", "false", "10", "0", "4"};

    private ConfigObject(Map<String, String> songholder, Map<String, Map<String, String[]>> triggerholder, Map<String, Map<String, String>> triggerMapper, Map<String, String[]> otherinfo,
                         Map<String, Map<String, String[]>> otherlinkinginfo, Map<String, Map<String, String[]>> triggerlinking, Map<String, Map<Integer, String[]>> loopPoints,
                         Map<String, Map<String, Map<Integer, String[]>>> linkingLoopPoints, Map<Integer, ConfigTitleCards.Title> titlecards,
                         Map<Integer, ConfigTitleCards.Image> imagecards, Map<Integer, Boolean> ismoving, List<String> blockedmods, List<String> debugStuff) {
        this.songholder = songholder;
        this.triggerholder = triggerholder;
        this.triggerMapper = triggerMapper;
        this.otherinfo = otherinfo;
        this.otherlinkinginfo = otherlinkinginfo;
        this.triggerlinking = triggerlinking;
        this.loopPoints = loopPoints;
        this.linkingLoopPoints = linkingLoopPoints;
        this.titlecards = titlecards;
        this.imagecards = fixNullImageCards(imagecards);
        this.ismoving = ismoving;
        this.blockedmods = blockedmods;
        this.debugStuff = debugStuff;

        this.markSongInfoForWriting = new HashMap<>();
        this.markTriggerInfoForWriting = new HashMap<>();
        this.markLinkingInfoForWriting = new HashMap<>();
        this.markTitleInfoForWriting = new HashMap<>();
        this.markImageInfoForWriting = new HashMap<>();

        this.mainConfig = new File("config/MusicTriggers/musictriggers.toml");
        this.titleCardConfig = new File("config/MusicTriggers/transitions.toml");
        this.debugConfig = new File("config/MusicTriggers/debug.toml");
        this.registrationConfig = new File("config/MusicTriggers/registration.toml");
    }

    private static Map<Integer, ConfigTitleCards.Image> fixNullImageCards(Map<Integer, ConfigTitleCards.Image> input) {
        return input.entrySet().stream().filter(entry -> entry.getValue().getName()!=null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static List<String> compileDebugStuff() {
        List<String> ret = new ArrayList<>();
        ret.add(ConfigDebug.ShowDebugInfo+"");
        ret.add(ConfigDebug.ShowJustCurSong+"");
        ret.add(ConfigDebug.ShowGUIName+"");
        ret.add(ConfigDebug.SilenceIsBad+"");
        ret.add(ConfigRegistry.registerDiscs+"");
        ret.add(ConfigRegistry.clientSideOnly+"");
        return ret;
    }

    public static ConfigObject createFromCurrent() {
        Cloner cloner=new Cloner();
        List<String> blocked = new ArrayList<>();
        blocked.addAll(Arrays.asList(ConfigDebug.blockedmods));
        return new ConfigObject(cloner.deepClone(ConfigToml.songholder),cloner.deepClone(ConfigToml.triggerholder),cloner.deepClone(ConfigToml.triggerMapper),cloner.deepClone(ConfigToml.otherinfo),
                cloner.deepClone(ConfigToml.otherlinkinginfo),cloner.deepClone(ConfigToml.triggerlinking),cloner.deepClone(ConfigToml.loopPoints),
                cloner.deepClone(ConfigToml.linkingLoopPoints),cloner.deepClone(ConfigTitleCards.titlecards),cloner.deepClone(ConfigTitleCards.imagecards),
                cloner.deepClone(ConfigTitleCards.ismoving),cloner.deepClone(blocked),cloner.deepClone(compileDebugStuff()));
    }

    public List<String> getAllDebugStuff() {
        List<String> ret = new ArrayList<>();
        for(String s : this.blockedmods) {
            if(!s.matches("minecraft")) ret.add("blocked mod");
        }
        ret.add("show debug info");
        ret.add("show just current song");
        ret.add("show gui name");
        ret.add("silence is bad");
        ret.add("register discs");
        ret.add("clientside only");
        return  ret;
    }

    public List<String> getAllSongs() {
        List<String> ret = new ArrayList<>();
        for(int i=0;i<this.songholder.entrySet().size();i++) {
            ret.add(this.songholder.get("song"+i));
        }
        return  ret;
    }

    public List<String> getAllCodes() {
        List<String> ret = new ArrayList<>();
        for(int i=0;i<this.songholder.entrySet().size();i++) {
            ret.add("song"+i);
        }
        return  ret;
    }

    public List<String> getAllTriggersForCode(String code) {
        List<String> ret = new ArrayList<>();
        for(Map.Entry<String, String[]> stringEntry : this.triggerholder.get(code).entrySet()) {
            ret.add(stringEntry.getKey());
        }
        return ret;
    }

    public String translateCodedTrigger(String code, String trigger) {
        return this.triggerMapper.get(code).get(trigger);
    }

    public List<String> getAllSongsForLinking(String code) {
        List<String> ret = new ArrayList<>();
        if(this.triggerlinking.get(code)!=null) {
            for (Map.Entry<String, String[]> stringEntry : this.triggerlinking.get(code).entrySet()) {
                if(!stringEntry.getKey().matches(code)) ret.add(stringEntry.getKey());
            }
        }
        return  ret;
    }

    public List<String> getAllLinkingInfo(String code, String song) {
        List<String> ret = new ArrayList<>();
        for(String ignored : this.triggerlinking.get(code).get(song)) {
            ret.add("trigger");
        }
        ret.add("pitch");
        ret.add("volume");
        return ret;
    }

    public List<String> getAllTransitions() {
        List<String> ret = new ArrayList<>();
        for(int ignored : this.titlecards.keySet()) {
            ret.add("title card");
        }
        for(int ignored : this.imagecards.keySet()) {
            ret.add("image card");
        }
        return ret;
    }

    public List<String> getAllTransitionParametersAtIndex(boolean title, int i) {
        List<String> ret = new ArrayList<>();
        if(title) {
            for(String ignored : this.titlecards.get(i).getTriggers()) {
                ret.add("trigger");
            }
            for(String ignored : this.titlecards.get(i).getTitles()) {
                ret.add("title");
            }
            for(String ignored : this.titlecards.get(i).getSubTitles()) {
                ret.add("subtitle");
            }
            ret.add("play_once");
            ret.add("title_color");
            ret.add("subtitle_color");
            ret.add("vague");
        } else {
            i=i-this.titlecards.size();
            if(this.imagecards.get(i).getTriggers()==null) this.imagecards.get(i).addTriggers(new ArrayList<>());
            for(String ignored : this.imagecards.get(i).getTriggers()) {
                ret.add("trigger");
            }
            ret.add("name");
            ret.add("time");
            ret.add("vertical");
            ret.add("horizontal");
            ret.add("scale_x");
            ret.add("scale_y");
            ret.add("play_once");
            ret.add("fade_in");
            ret.add("fade_out");
            ret.add("vague");
            if(this.ismoving.get(i)){
                ret.add("delay");
                ret.add("split");
                ret.add("frames_skipped");
            }
        }
        return ret;
    }

    public Map<String, Boolean> getAllImages() {
        Map<String, Boolean> ret = new HashMap<>();
        File imageFolder = new File("."+"/config/MusicTriggers/songs/assets/musictriggers/textures/");
        File[] listOfFiles= imageFolder.listFiles();
        assert listOfFiles != null;
        for(File f : listOfFiles) {
            if(f.isDirectory()) ret.put(f.getName(), true);
            else if(f.getName().contains(".png") && !f.getName().contains(".png.mcmeta") && Arrays.stream(listOfFiles).toList().contains(new File(f.getPath()+".mcmeta"))) ret.put(f.getName(),true);
            else if(f.getName().contains(".gif")) ret.put(f.getName(), true);
            else if(f.getName().contains(".mp4")) ret.put(f.getName(), true);
            else if(f.getName().contains(".png")) ret.put(f.getName(), false);
        }
        return ret;
    }

    public List<String> getAllLoops(String code, String song, boolean linked) {
        List<String> ret = new ArrayList<>();
        if(!linked) {
            if(this.loopPoints.get(code)!=null) {
                for (int ignored : this.loopPoints.get(code).keySet()) {
                    ret.add("Loop");
                }
            }
        } else if(this.linkingLoopPoints.get(code)!=null && this.linkingLoopPoints.get(code).get(song)!=null) {
            for(int ignored : this.linkingLoopPoints.get(code).get(song).keySet()) {
                ret.add("Loop");
            }
        }
        return ret;
    }

    public List<String> getAllLoopInfo() {
        List<String> ret = new ArrayList<>();
        ret.add("amount");
        ret.add("min");
        ret.add("max");
        return ret;
    }

    public List<String> extractStringListFromMapKeys(Map<String, ?> map) {
        List<String> ret = new ArrayList<>();
        for(Map.Entry<String, ?> stringEntry : map.entrySet()) {
            ret.add(stringEntry.getKey());
        }
        return ret;
    }

    public String decode(String code) {
        return this.songholder.get(code);
    }

    public String addSong(String name) {
        String code = "song" + this.songholder.keySet().size();
        this.songholder.put(code, name);
        this.triggerholder.put(code, new HashMap<>());
        this.otherinfo.put(code, new String[]{"1", "0", "false", "100", "1", "0", "0"});
        return code;
    }

    public void removeSong(String code) {
        this.songholder.remove(code);
        this.triggerholder.remove(code);
        this.otherinfo.remove(code);
    }

    public String addTrigger(String code, String trigger) {
        this.triggerMapper.putIfAbsent(code, new HashMap<>());
        String codedTrigger = "trigger-"+triggerholder.get(code).keySet().size();
        this.triggerMapper.get(code).put(codedTrigger,trigger);
        this.triggerholder.get(code).put(codedTrigger, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                "minecraft", "_", "16", "false", "100", "100", "100",
                "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                "nope", "-111", "false","_", "true", "-1", "-111", "true",
                "false", "false", "false", "0", "minecraft"});
        return codedTrigger;
    }

    public void removeTrigger(String code, String trigger) {
        this.triggerholder.get(code).remove(trigger);
    }

    public void addLinkingSong(String code, String name) {
        this.triggerlinking.putIfAbsent(code, new HashMap<>());
        this.otherlinkinginfo.putIfAbsent(code, new HashMap<>());
        this.triggerlinking.get(code).put(name, new String[]{});
        this.otherlinkinginfo.get(code).put(name, new String[]{"1", "1"});
    }

    public void removeLinkingSong(String code, String name) {
        this.triggerlinking.get(code).remove(name);
        this.otherlinkinginfo.get(code).remove(name);
    }

    public void addLinkingTrigger(String code, String name, String trigger) {
        List<String> triggers = Arrays.stream(this.triggerlinking.get(code).get(name)).collect(Collectors.toList());
        triggers.add(trigger);
        this.triggerlinking.get(code).put(name, triggers.toArray(new String[0]));
    }

    public int addTransition(boolean title, boolean ismoving, String name) {
        int index;
        if(title) {
            index = this.titlecards.size();
            this.titlecards.put(index, new ConfigTitleCards.Title());
        } else {
            index = this.imagecards.size();
            MusicTriggers.logger.info(index);
            this.imagecards.put(index, new ConfigTitleCards.Image());
            this.imagecards.get(index).setName(name);
            this.ismoving.put(index, ismoving);
            this.markImageInfoForWriting.putIfAbsent(index, new ArrayList<>());
            this.markImageInfoForWriting.get(index).add(0);
            index+=this.titlecards.size();
        }
        return index;
    }

    public void addLoop(String code, String song, boolean linked) {
        if(!linked) this.loopPoints.get(code).put(this.loopPoints.get(code).size(), new String[]{"0","0","0"});
        else this.linkingLoopPoints.get(code).get(song).put(this.linkingLoopPoints.get(code).get(song).size(), new String[]{"0","0","0"});
    }

    public boolean isLinkingInfoTrigger(String code, String song, int index) {
        return index<this.triggerlinking.get(code).get(song).length;
    }

    public boolean isTitle(int index) {
        return index<this.titlecards.size();
    }

    public void removeLinkingTrigger(String code, String name, int index) {
        List<String> triggers = Arrays.stream(this.triggerlinking.get(code).get(name)).collect(Collectors.toList());
        triggers.remove(index);
        this.triggerlinking.get(code).put(name, triggers.toArray(new String[0]));
    }

    public void removeTransition(boolean title, int index) {
        if(title) this.titlecards.remove(index);
        else {
            index-=this.titlecards.size();
            this.imagecards.remove(index);
        }
    }

    public void addTransitionTrigger(boolean title, int index) {
        if(title) this.titlecards.get(index).getTriggers().add("trigger");
        else {
            index-=this.titlecards.size();
            this.imagecards.get(index).getTriggers().add("trigger");
        }
    }

    public void addTitle(int index) {
        this.titlecards.get(index).getTitles().add("title");
    }

    public void addSubtitle(int index) {
        this.titlecards.get(index).getSubTitles().add("subtitle");
    }

    public boolean checkIfTransitionIndexIsArray(boolean title, int transitionIndex, int index) {
        if(title) {
            if(index<this.titlecards.get(transitionIndex).getTriggers().size()) return true;
            else {
                index-=this.titlecards.get(transitionIndex).getTriggers().size();
                if(index<this.titlecards.get(transitionIndex).getTitles().size()) return true;
                else {
                    index-=this.titlecards.get(transitionIndex).getTitles().size();
                    return index < this.titlecards.get(transitionIndex).getSubTitles().size();
                }
            }
        }
        else {
            transitionIndex-=this.titlecards.size();
            return index<this.imagecards.get(transitionIndex).getTriggers().size();
        }
    }

    public void removeTransitionTrigger(boolean title, int transitionIndex, int index) {
        if(title) {
            if(index<this.titlecards.get(transitionIndex).getTriggers().size()) this.titlecards.get(transitionIndex).getTriggers().remove(index);
            else {
                index-=this.titlecards.get(transitionIndex).getTriggers().size();
                if(index<this.titlecards.get(transitionIndex).getTitles().size()) this.titlecards.get(transitionIndex).getTitles().remove(index);
                else {
                    index-=this.titlecards.get(transitionIndex).getTitles().size();
                    if(index<this.titlecards.get(transitionIndex).getSubTitles().size()) this.titlecards.get(transitionIndex).getSubTitles().remove(index);
                }
            }
        }
        else {
            transitionIndex-=this.titlecards.size();
            this.imagecards.get(transitionIndex).getTriggers().remove(index);
        }
    }

    public void removeLoop(String code, String song, boolean linked, int loopIndex) {
        if(!linked) this.loopPoints.get(code).remove(loopIndex);
        else this.linkingLoopPoints.get(code).get(song).remove(loopIndex);
    }

    public String getSongInfoAtIndex(String code, int index) {
        if(index<5) return this.otherinfo.get(code)[index];
        else return "Trigger";
    }

    public String getTriggerInfoAtIndex(String code, String trigger, int index) {
        return this.triggerholder.get(code).get(trigger)[index];
    }

    public String getLinkingInfoAtIndex(String code, String song, int index) {
        int triggerSize = this.triggerlinking.get(code).get(song).length;
        if(index<triggerSize) return this.triggerlinking.get(code).get(song)[index];
        else return this.otherlinkinginfo.get(code).get(song)[index-triggerSize];
    }

    public String getTransitionInfoAtIndex(boolean title, int transitionIndex, int index) {
        if(title) {
            List<String> triggers = this.titlecards.get(transitionIndex).getTriggers();
            if(index< triggers.size()) return triggers.get(index);
            index-=triggers.size();
            List<String> titles = this.titlecards.get(transitionIndex).getTitles();
            if(index< titles.size()) return titles.get(index);
            index-=titles.size();
            List<String> subtitles = this.titlecards.get(transitionIndex).getSubTitles();
            if(index< subtitles.size()) return subtitles.get(index);
            index-=subtitles.size();
            switch (index) {
                case 0 :
                    return this.titlecards.get(transitionIndex).getPlayonce().toString();
                case 1 :
                    return this.titlecards.get(transitionIndex).getTitlecolor();
                case 2 :
                    return this.titlecards.get(transitionIndex).getSubtitlecolor();
                case 3 :
                    return this.titlecards.get(transitionIndex).getVague().toString();
            }
        } else {
            transitionIndex-=this.titlecards.size();
            List<String> triggers = this.imagecards.get(transitionIndex).getTriggers();
            if(index< triggers.size()) {
                return triggers.get(index);
            }
            index-=triggers.size();
            switch (index) {
                case 0 :
                    return this.imagecards.get(transitionIndex).getName();
                case 1 :
                    return this.imagecards.get(transitionIndex).getTime()+"";
                case 2 :
                    return this.imagecards.get(transitionIndex).getVertical()+"";
                case 3 :
                    return this.imagecards.get(transitionIndex).getHorizontal()+"";
                case 4 :
                    return this.imagecards.get(transitionIndex).getScaleX()+"";
                case 5 :
                    return this.imagecards.get(transitionIndex).getScaleY()+"";
                case 6 :
                    return this.imagecards.get(transitionIndex).getPlayonce().toString();
                case 7 :
                    return this.imagecards.get(transitionIndex).getFadeIn()+"";
                case 8 :
                    return this.imagecards.get(transitionIndex).getFadeOut()+"";
                case 9 :
                    return this.imagecards.get(transitionIndex).getVague().toString();
                case 10 :
                    return this.imagecards.get(transitionIndex).getDelay()+"";
                case 11 :
                    return this.imagecards.get(transitionIndex).getSplit()+"";
                case 12 :
                    return this.imagecards.get(transitionIndex).getSkip()+"";
            }
        }
        return null;
    }

    public String getOtherInfoAtIndex(int index) {
        if(index<this.blockedmods.size()) return this.blockedmods.get(index);
        else {
            index-=this.blockedmods.size();
            return this.debugStuff.get(index);
        }
    }

    public String getAllTriggersForTransition(int i) {
        StringBuilder builder = new StringBuilder();
        if(i<this.titlecards.size()) {
            for(String trigger : this.titlecards.get(i).getTriggers()) {
                builder.append(trigger).append(" ");
            }
        }
        else {
            i=i-titlecards.size();
            for(String trigger : this.imagecards.get(i).getTriggers()) {
                builder.append(trigger).append(" ");
            }
        }
        return builder.toString();
    }

    public String buildLoopTitle(String code, String song, boolean linked, int index) {
        StringBuilder ret = new StringBuilder();
        if(!linked) {
            if(loopPoints!=null && loopPoints.get(code)!=null && loopPoints.get(code).get(index)!=null) {
                for (String l : loopPoints.get(code).get(index)) {
                    ret.append(l).append(" ");
                }
            }
        } else {
            if(linkingLoopPoints!=null && linkingLoopPoints.get(code)!=null && linkingLoopPoints.get(code).get(song)!=null && linkingLoopPoints.get(code).get(song).get(index)!=null) {
                for (String l : linkingLoopPoints.get(code).get(song).get(index)) {
                    ret.append(l).append(" ");
                }
            }
        }
        return ret.toString();
    }

    public String getLoopParameter(String code, String song, boolean linked, int loopIndex, int index) {
        String ret;
        if(!linked) ret = loopPoints.get(code).get(loopIndex)[index];
        else ret = linkingLoopPoints.get(code).get(song).get(loopIndex)[index];
        return ret;
    }

    public void editOtherInfoParameter(String code, int index, String newVal) {
        this.markSongInfoForWriting.putIfAbsent(code, new ArrayList<>());
        if(!this.markSongInfoForWriting.get(code).contains(index)) {
            this.markSongInfoForWriting.get(code).add(index);
        }
        this.otherinfo.get(code)[index] = newVal;
    }

    public void addAllExistingParameters() {
        for(Map.Entry<String, Map<String, String[]>> stringMapEntry : this.triggerholder.entrySet()) {
            addExistingEditedOtherInfoParameters(stringMapEntry.getKey());
            for(Map.Entry<String, String[]> stringEntry : this.triggerholder.get(stringMapEntry.getKey()).entrySet()) {
                addExistingEditedTriggerInfoParameters(stringMapEntry.getKey(), stringEntry.getKey());
            }
            if(this.otherlinkinginfo.get(stringMapEntry.getKey()) != null) {
                for (Map.Entry<String, String[]> stringEntry : this.otherlinkinginfo.get(stringMapEntry.getKey()).entrySet()) {
                    addExistingEditedLinkingInfoParameters(stringMapEntry.getKey(), stringEntry.getKey());
                }
            }
        }
        for(int i : this.titlecards.keySet()) {
            this.addExistingEditedTitleInfoParameters(i);
        }
        for(int i : this.imagecards.keySet()) {
            this.addExistingEditedImageInfoParameters(i);
        }
    }

    private void addExistingEditedOtherInfoParameters(String code) {
        this.markSongInfoForWriting.putIfAbsent(code, new ArrayList<>());
        for(int i=0;i<this.otherinfo.get(code).length;i++) {
            if(!this.otherinfo.get(code)[i].matches(otherInfoDefaults[i])) {
                if(!this.markSongInfoForWriting.get(code).contains(i)) {
                    this.markSongInfoForWriting.get(code).add(i);
                }
            }
        }
    }

    private void addExistingEditedTriggerInfoParameters(String code, String trigger) {
        this.markTriggerInfoForWriting.putIfAbsent(code, new HashMap<>());
        this.markTriggerInfoForWriting.get(code).putIfAbsent(trigger, new ArrayList<>());
        for(int i=0;i<this.triggerholder.get(code).get(trigger).length;i++) {
            if(!this.triggerholder.get(code).get(trigger)[i].matches(triggerInfoDefaults[i])) {
                if(!this.markTriggerInfoForWriting.get(code).get(trigger).contains(i)) {
                    this.markTriggerInfoForWriting.get(code).get(trigger).add(i);
                }
            }
        }
    }

    private void addExistingEditedLinkingInfoParameters(String code, String song) {
        this.markLinkingInfoForWriting.putIfAbsent(code, new HashMap<>());
        this.markLinkingInfoForWriting.get(code).putIfAbsent(song, new ArrayList<>());
        for(int i=0;i<this.otherlinkinginfo.get(code).get(song).length;i++) {
            if(!this.otherlinkinginfo.get(code).get(song)[i].matches(linkingInfoDefaults[i])) {
                if(!this.markLinkingInfoForWriting.get(code).get(song).contains(i)) {
                    this.markLinkingInfoForWriting.get(code).get(song).add(i);
                }
            }
        }
    }

    private void addExistingEditedTitleInfoParameters(int i) {
        this.markTitleInfoForWriting.putIfAbsent(i, new ArrayList<>());
        String[] info = titleInfoToArray(titlecards.get(i));
        for(int j=0;j<info.length;j++) {
            if(!info[j].matches(titleInfoDefaults[j])) {
                if(!this.markTitleInfoForWriting.get(i).contains(j)) {
                    this.markTitleInfoForWriting.get(i).add(j);
                }
            }
        }
    }

    private void addExistingEditedImageInfoParameters(int i) {
        this.markImageInfoForWriting.putIfAbsent(i, new ArrayList<>());
        String[] info = imageInfoToArray(imagecards.get(i));
        for(int j=0;j<info.length;j++) {
            if(!info[j].matches(imageInfoDefaults[j])) {
                if(!this.markImageInfoForWriting.get(i).contains(j)) {
                    this.markImageInfoForWriting.get(i).add(j);
                }
            }
        }
    }

    private String[] titleInfoToArray(ConfigTitleCards.Title title) {
        List<String> ret = new ArrayList<>();
        ret.add(title.getPlayonce().toString());
        ret.add(title.getTitlecolor());
        ret.add(title.getSubtitlecolor());
        ret.add(title.getVague().toString());
        return ret.toArray(new String[0]);
    }

    private String[] imageInfoToArray(ConfigTitleCards.Image image) {
        List<String> ret = new ArrayList<>();
        ret.add(image.getName());
        ret.add(image.getTime()+"");
        ret.add(image.getVertical()+"");
        ret.add(image.getHorizontal()+"");
        ret.add(image.getScaleX()+"");
        ret.add(image.getScaleY()+"");
        ret.add(image.getPlayonce().toString());
        ret.add(image.getFadeIn()+"");
        ret.add(image.getFadeOut()+"");
        ret.add(image.getVague().toString());
        ret.add(image.getDelay()+"");
        ret.add(image.getSplit()+"");
        ret.add(image.getSkip()+"");
        return ret.toArray(new String[0]);
    }

    public void editTriggerInfoParameter(String code, String trigger, int index, String newVal) {
        this.markTriggerInfoForWriting.putIfAbsent(code, new HashMap<>());
        this.markTriggerInfoForWriting.get(code).putIfAbsent(trigger, new ArrayList<>());
        if(!this.markTriggerInfoForWriting.get(code).get(trigger).contains(index)) {
            this.markTriggerInfoForWriting.get(code).get(trigger).add(index);
        }
        this.triggerholder.get(code).get(trigger)[index] = newVal;
    }

    public void editLinkingInfoParameter(String code, String song, int index, String newVal) {
        this.markLinkingInfoForWriting.putIfAbsent(code, new HashMap<>());
        this.markLinkingInfoForWriting.get(code).putIfAbsent(song, new ArrayList<>());
        int triggerSize = this.triggerlinking.get(code).get(song).length;
        if(index>=triggerSize && !this.markLinkingInfoForWriting.get(code).get(song).contains(index-triggerSize)) this.markLinkingInfoForWriting.get(code).get(song).add(index-triggerSize);
        if(index<triggerSize) this.triggerlinking.get(code).get(song)[index] = newVal;
        else this.otherlinkinginfo.get(code).get(song)[index-triggerSize] = newVal;
    }

    public void editTransitionInfoAtIndex(boolean title, int transitionIndex, int index, String newVal, int change) {
        if(title) {
            List<String> triggers = this.titlecards.get(transitionIndex).getTriggers();
            if(index<triggers.size()) {
                this.titlecards.get(transitionIndex).getTriggers().set(index, newVal);
                return;
            }
            index-=triggers.size();
            List<String> titles = this.titlecards.get(transitionIndex).getTitles();
            if(index< titles.size()) {
                this.titlecards.get(transitionIndex).getTitles().set(index, newVal);
                return;
            }
            index-=titles.size();
            List<String> subtitles = this.titlecards.get(transitionIndex).getSubTitles();
            if(index< subtitles.size()) {
                this.titlecards.get(transitionIndex).getSubTitles().set(index, newVal);
                return;
            }
            index-=subtitles.size();
            this.markTitleInfoForWriting.putIfAbsent(transitionIndex, new ArrayList<>());
            switch (index) {
                case 0 -> {
                    this.titlecards.get(transitionIndex).setPlayonce(!this.titlecards.get(transitionIndex).getPlayonce());
                    if (!this.markTitleInfoForWriting.get(transitionIndex).contains(index)) this.markTitleInfoForWriting.get(transitionIndex).add(index);
                }
                case 1 -> {
                    this.titlecards.get(transitionIndex).setTitlecolor(newVal);
                    if (!this.markTitleInfoForWriting.get(transitionIndex).contains(index)) this.markTitleInfoForWriting.get(transitionIndex).add(index);
                }
                case 2 -> {
                    this.titlecards.get(transitionIndex).setSubtitlecolor(newVal);
                    if (!this.markTitleInfoForWriting.get(transitionIndex).contains(index)) this.markTitleInfoForWriting.get(transitionIndex).add(index);
                }
                case 3 -> {
                    this.titlecards.get(transitionIndex).setVague(!this.titlecards.get(transitionIndex).getVague());
                    if (!this.markTitleInfoForWriting.get(transitionIndex).contains(index)) this.markTitleInfoForWriting.get(transitionIndex).add(index);
                }
            }
        } else {
            transitionIndex-=this.titlecards.size();
            List<String> triggers = this.imagecards.get(transitionIndex).getTriggers();
            if(index< triggers.size()) {
                this.imagecards.get(transitionIndex).getTriggers().set(index, newVal);
                return;
            }
            markImageInfoForWriting.putIfAbsent(transitionIndex, new ArrayList<>());
            index-=triggers.size();
            switch (index) {
                case 0 -> {
                    this.imagecards.get(transitionIndex).setName(newVal);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 1 -> {
                    this.imagecards.get(transitionIndex).setTime(this.imagecards.get(transitionIndex).getTime() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 2 -> {
                    this.imagecards.get(transitionIndex).setVertical(this.imagecards.get(transitionIndex).getVertical() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 3 -> {
                    this.imagecards.get(transitionIndex).setHorizontal(this.imagecards.get(transitionIndex).getHorizontal() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 4 -> {
                    this.imagecards.get(transitionIndex).setScaleX(this.imagecards.get(transitionIndex).getScaleX() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 5 -> {
                    this.imagecards.get(transitionIndex).setScaleY(this.imagecards.get(transitionIndex).getScaleY() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 6 -> {
                    this.imagecards.get(transitionIndex).setPlayonce(!this.imagecards.get(transitionIndex).getPlayonce());
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 7 -> {
                    this.imagecards.get(transitionIndex).setFadeIn(this.imagecards.get(transitionIndex).getFadeIn() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 8 -> {
                    this.imagecards.get(transitionIndex).setFadeOut(this.imagecards.get(transitionIndex).getFadeOut() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 9 -> {
                    this.imagecards.get(transitionIndex).setVague(!this.imagecards.get(transitionIndex).getVague());
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index))
                        this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 10 -> {
                    this.imagecards.get(transitionIndex).setDelay(this.imagecards.get(transitionIndex).getDelay() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 11 -> {
                    this.imagecards.get(transitionIndex).setSplit(this.imagecards.get(transitionIndex).getSplit() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
                case 12 -> {
                    this.imagecards.get(transitionIndex).setSkip(this.imagecards.get(transitionIndex).getSkip() + change);
                    if (!this.markImageInfoForWriting.get(transitionIndex).contains(index)) this.markImageInfoForWriting.get(transitionIndex).add(index);
                }
            }
        }
    }

    public void editOtherInfoAtIndex(int index, String newVal) {
        if(index<this.blockedmods.size()) this.blockedmods.set(index, newVal);
        else {
            index-=this.blockedmods.size();
            this.debugStuff.set(index, newVal);
        }
    }

    public void editLoopInfoAtIndex(String code, String song, boolean linked, int loopIndex, int index, String newVal) {
        if(!linked) this.loopPoints.get(code).get(loopIndex)[index] = newVal;
        else this.linkingLoopPoints.get(code).get(song).get(loopIndex)[index] = newVal;
    }

    public void write() throws IOException {
        StringBuilder mainBuilder = new StringBuilder();
        mainBuilder.append(this.writeUniversal());
        for(int j=0;j<this.songholder.entrySet().size();j++) {
            String code = "song"+j;
            MusicTriggers.logger.info("writing code: "+code);
            if(this.triggerholder.get(code)!=null && !this.triggerholder.get(code).entrySet().isEmpty()) {
                mainBuilder.append(formatSongBrackets(this.songholder.get(code))).append("\n");
                if (this.markSongInfoForWriting.get(code) != null) {
                    for (int i : this.markSongInfoForWriting.get(code)) {
                        if(i<5) mainBuilder.append("\t").append(Mappings.songparameters.get(i)).append(" = \"").append(this.otherinfo.get(code)[i]).append("\"\n");
                    }
                }
                for (Map.Entry<String, String[]> stringEntry : this.triggerholder.get(code).entrySet()) {
                    String trigger = stringEntry.getKey();
                    mainBuilder.append("\t").append(formatTriggerBrackets(code, this.songholder.get(code))).append("\n");
                    mainBuilder.append("\t\tname = \"").append(translateCodedTrigger(code,trigger)).append("\"\n");
                    if (this.markTriggerInfoForWriting.get(code) != null && this.markTriggerInfoForWriting.get(code).get(trigger) != null) {
                        boolean zone = false;
                        for (int i : this.markTriggerInfoForWriting.get(code).get(trigger)) {
                            if (!Mappings.parameters.get(i).matches("zone")) {
                                mainBuilder.append("\t\t").append(Mappings.parameters.get(i)).append(" = \"").append(this.triggerholder.get(code).get(trigger)[i]).append("\"\n");
                            } else zone = true;
                        }
                        if (zone) {
                            mainBuilder.append("\t\t").append("[").append(this.songholder.get(code)).append(".trigger.zone]\n");
                            formatZoneParameter(mainBuilder, this.triggerholder.get(code).get(trigger)[7]);
                        }
                    }
                }
                if (this.triggerlinking.get(code) != null && !getAllSongsForLinking(code).isEmpty()) {
                    mainBuilder.append("\t[").append(this.songholder.get(code)).append(".link]\n");
                    mainBuilder.append(this.formatLinkingDefaults(code)).append("\n");
                    if(Integer.parseInt(this.otherinfo.get(code)[5])!=0) mainBuilder.append("\t\tfade_in = \"").append(this.otherinfo.get(code)[5]).append("\"\n");
                    if(Integer.parseInt(this.otherinfo.get(code)[6])!=0) mainBuilder.append("\t\tfade_out = \"").append(this.otherinfo.get(code)[6]).append("\"\n");
                    for (Map.Entry<String, String[]> stringEntry : this.triggerlinking.get(code).entrySet()) {
                        String song = stringEntry.getKey();
                        if (!song.matches(code)) {
                            mainBuilder.append(this.formatLinkingBrackets(code, this.songholder.get(code))).append("\n");
                            mainBuilder.append("\t\t\tsong = \"").append(song).append("\"\n");
                            if (this.markLinkingInfoForWriting.get(code) != null && this.markLinkingInfoForWriting.get(code).get(song) != null) {
                                for (int i : this.markLinkingInfoForWriting.get(code).get(song)) {
                                    mainBuilder.append("\t\t\t").append(Mappings.linkingparameters.get(i)).append(" = \"").append(this.otherlinkinginfo.get(code).get(song)[i]).append("\"\n");
                                }
                            }
                            mainBuilder.append(this.formatLinkingTriggers(code, song)).append("\n");
                            if (this.linkingLoopPoints.get(code) != null && this.linkingLoopPoints.get(code).get(song) != null) {
                                for (int l : this.linkingLoopPoints.get(code).get(song).keySet()) {
                                    mainBuilder.append(this.formatLinkingLoopPointsBrackets(code, this.songholder.get(code), song));
                                    mainBuilder.append("\t\t\t\tamount = \"").append(this.linkingLoopPoints.get(code).get(song).get(l)[0]).append("\"\n");
                                    mainBuilder.append("\t\t\t\tmin = \"").append(this.linkingLoopPoints.get(code).get(song).get(l)[1]).append("\"\n");
                                    mainBuilder.append("\t\t\t\tmax = \"").append(this.linkingLoopPoints.get(code).get(song).get(l)[2]).append("\"\n");
                                }
                            }
                        }
                    }
                }
                if (this.loopPoints.get(code) != null) {
                    for (int l : this.loopPoints.get(code).keySet()) {
                        mainBuilder.append(this.formatLoopPointsBrackets(code, this.songholder.get(code)));
                        mainBuilder.append("\t\tamount = \"").append(this.loopPoints.get(code).get(l)[0]).append("\"\n");
                        mainBuilder.append("\t\tmin = \"").append(this.loopPoints.get(code).get(l)[1]).append("\"\n");
                        mainBuilder.append("\t\tmax = \"").append(this.loopPoints.get(code).get(l)[2]).append("\"\n");
                    }
                }
                mainBuilder.append("\n");
            }
        }
        FileWriter writeMain = new FileWriter(this.mainConfig);
        writeMain.write(mainBuilder.toString());
        writeMain.close();
        StringBuilder transitionBuilder = new StringBuilder();
        for(int i : this.titlecards.keySet()) {
            transitionBuilder.append(formatTitleBrackets());
            this.formatTitleTriggers(i, transitionBuilder);
            markTitleInfoForWriting.putIfAbsent(i, new ArrayList<>());
            Mappings.buildTitleOutputForGuiFromIndex(this.titlecards.get(i), transitionBuilder, markTitleInfoForWriting.get(i));
            transitionBuilder.append("\n");
        }
        for(int i : this.imagecards.keySet()) {
            transitionBuilder.append(formatImageBrackets());
            this.formatImageTriggers(i, transitionBuilder);
            markImageInfoForWriting.putIfAbsent(i, new ArrayList<>());
            Mappings.buildImageOutputForGuiFromIndex(this.imagecards.get(i), transitionBuilder, markImageInfoForWriting.get(i), this.ismoving.get(i));
            transitionBuilder.append("\n");
        }
        FileWriter writeTransitions = new FileWriter(this.titleCardConfig);
        writeTransitions.write(transitionBuilder.toString());
        writeTransitions.close();
        this.writeOther();
    }

    public void writeOther() throws IOException {
        String d = "# Show the debug info\n" + "showdebuginfo = \""+this.debugStuff.get(0)+"\"\n" +
                "# If ShowDebugInfo is set to true, but you only want to see the song name\n" + "showjustcursong = \""+this.debugStuff.get(1)+"\"\n" +
                "# Show an overlay for the name of the current GUI\n" + "showguiname = \""+this.debugStuff.get(2)+"\"\n" +
                "# Only silence blocked music when there is music from Music Triggers already playing\n" + "silenceisbad = \""+this.debugStuff.get(3)+"\"\n" +
                "# List of mod ids to remove the music from so there is not any overlap\n" + "blockedmods = ["+this.formatBlockedMods()+"]\n";
        FileWriter debugWriter = new FileWriter(this.debugConfig);
        debugWriter.write(d);
        debugWriter.close();
        String sb = "# Music Discs\n" + "registerdiscs = \""+this.debugStuff.get(4)+"\"\n" +
                "# Client Side Only (Some triggers will not be able to trigger)\n" + "clientsideonly = \""+this.debugStuff.get(5)+"\"\n";
        FileWriter registrationWriter = new FileWriter(this.registrationConfig);
        registrationWriter.write(sb);
        registrationWriter.close();
    }

    private String formatSongBrackets(String name) {
        if ((Collections.frequency(this.songholder.values(), name))>1) return "[["+name+"]]";
        else return "["+name+"]";
    }

    private String formatTriggerBrackets(String code, String song) {
        if (this.triggerholder.get(code).entrySet().size()>1) return "[["+song+".trigger]]";
        else return "["+song+".trigger]";
    }

    private String formatLinkingBrackets(String code, String song) {
        if (this.triggerlinking.get(code).keySet().size()>1) return "\t\t[["+song+".link.trigger]]";
        else return "\t\t["+song+".link.trigger]";
    }

    private String formatTitleBrackets() {
        if (this.titlecards.size()>1) return "[[title]]\n";
        else return "[title]\n";
    }

    private String formatImageBrackets() {
        if (this.imagecards.size()>1) return "[[image]]\n";
        else return "[image]\n";
    }

    private String formatLoopPointsBrackets(String code, String song) {
        if (this.loopPoints.get(code).size()>1) return "\t[["+song+".loop]]\n";
        else return "\t["+song+".loop]\n";
    }

    private String formatLinkingLoopPointsBrackets(String code, String song, String linkedSong) {
        if (this.linkingLoopPoints.get(code).get(linkedSong).size()>1) return "\t\t\t[["+song+".link.trigger.loop]]\n";
        else return "\t\t\t["+song+".link.trigger.loop]\n";
    }

    private String formatLinkingDefaults(String code) {
        List<String> temp = this.getAllTriggersForCode(code);
        List<String> triggers = new ArrayList<>();
        for(String trigger : temp) {
            if(!this.triggerholder.get(code).get(trigger)[10].matches("minecraft")) triggers.add(translateCodedTrigger(code,trigger)+"-"+this.triggerholder.get(code).get(trigger)[10]);
            else triggers.add(translateCodedTrigger(code,trigger));
        }
        StringBuilder defaults = new StringBuilder();
        defaults.append("\t\tdefault = [ ");
        for(String trigger : triggers) {
            defaults.append("\"").append(trigger).append("\" ");
        }
        defaults.append("]");
        return defaults.toString();
    }

    private String formatLinkingTriggers(String code, String song) {
        List<String> triggers = Arrays.stream(this.triggerlinking.get(code).get(song)).toList();
        StringBuilder triggerbuilder = new StringBuilder();
        triggerbuilder.append("\t\t\tname = [ ");
        for(String trigger : triggers) {
            triggerbuilder.append("\"").append(trigger).append("\" ");
        }
        triggerbuilder.append("]");
        return triggerbuilder.toString();
    }

    private void formatTitleTriggers(int i, StringBuilder triggerbuilder) {
        triggerbuilder.append("\ttriggers = [ ");
        for(String trigger : this.titlecards.get(i).getTriggers()) {
            triggerbuilder.append("\"").append(trigger).append("\" ");
        }
        triggerbuilder.append("]\n");
    }

    private void formatImageTriggers(int i, StringBuilder triggerbuilder) {
        triggerbuilder.append("\ttriggers = [ ");
        for(String trigger : this.imagecards.get(i).getTriggers()) {
            triggerbuilder.append("\"").append(trigger).append("\" ");
        }
        triggerbuilder.append("]\n");
    }

    private String formatBlockedMods() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for(String mod : this.blockedmods) {
            sb.append(mod).append(" ");
        }
        return sb.toString();
    }

    private void formatZoneParameter(StringBuilder builder, String zone) {
        String[] broken = stringBreaker(zone, ",");
        builder.append("\t\t\tx_min = \"").append(broken[0]).append("\"\n");
        builder.append("\t\t\ty_min = \"").append(broken[1]).append("\"\n");
        builder.append("\t\t\tz_min = \"").append(broken[2]).append("\"\n");
        builder.append("\t\t\tx_max = \"").append(broken[3]).append("\"\n");
        builder.append("\t\t\ty_max = \"").append(broken[4]).append("\"\n");
        builder.append("\t\t\tz_max = \"").append(broken[5]).append("\"\n");
    }

    private String writeUniversal() {
        String ret ="";
        if(MusicPicker.universalDelay!=0 || MusicPicker.universalFadeIn!=0 || MusicPicker.universalFadeOut!=0) {
            ret+="[universal]\n";
            if(MusicPicker.universalDelay!=0) ret+="\tdelay = \""+MusicPicker.universalDelay+"\"\n";
            if(MusicPicker.universalFadeIn!=0) ret+="\tfade_in = \""+MusicPicker.universalFadeIn+"\"\n";
            if(MusicPicker.universalFadeOut!=0) ret+="\tfade_out = \""+MusicPicker.universalFadeOut+"\"\n";
            ret+="\n";
        }
        return ret;
    }
}
