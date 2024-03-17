package mods.thecomputerizer.musictriggers.api.data.trigger;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;

import javax.annotation.Nullable;
import java.util.*;

public class TriggerCombination extends TriggerAPI {

    /**
     * Each child list is an either/or list of triggers.
     * All child lists must have at least 1 trigger active for the parent combination to be active
     */
    private final List<List<TriggerAPI>> children;

    private final List<TriggerAPI> priorityChildren;
    private TriggerAPI priorityTrigger;

    protected TriggerCombination(ChannelAPI channel) {
        super(channel,"combination");
        this.children = new ArrayList<>();
        this.priorityChildren = new ArrayList<>();
    }

    public void addChild(TriggerAPI ... triggers) {
        this.children.add(Arrays.asList(triggers));
        setParentStatus(false,triggers);
    }

    @Override
    public @Nullable Parameter<?> getParameter(String name) {
        return Objects.nonNull(this.priorityTrigger) ? this.priorityTrigger.getParameter(name) : super.getParameter(name);
    }

    @Override
    public boolean isEnabled() {
        for(List<TriggerAPI> child : this.children)
            for(TriggerAPI trigger : child)
                if(!trigger.isEnabled()) return false;
        return true;
    }

    @Override
    protected void initExtraParameters(Map<String, Parameter<?>> map) {
        map.clear();
    }

    @Override
    public boolean isActive(TriggerContextAPI<?,?> ctx) {
        this.priorityChildren.clear();
        for(List<TriggerAPI> child : this.children)
            if(!isChildActive(ctx,child)) return false;
        updatePriorityTrigger();
        return true;
    }

    protected boolean isChildActive(TriggerContextAPI<?,?> ctx, List<TriggerAPI> triggers) {
        for(TriggerAPI trigger : triggers)
            if(trigger.isActive(ctx)) {
                this.priorityChildren.add(trigger);
                return true;
            }
        return false;
    }

    @Override
    public boolean matches(Collection<TriggerAPI> triggers) {
        if(this.children.size()==triggers.size()) {
            for(List<TriggerAPI> child : this.children)
                for(TriggerAPI other : triggers)
                    if(!TriggerHelper.matchesAny(child,other)) return false;
            return true;
        }
        return false;
    }

    @Override
    public boolean matches(TriggerAPI trigger) {
        if(trigger instanceof TriggerCombination) {
            TriggerCombination combo = (TriggerCombination)trigger;
            if(this.children.size()==combo.children.size()) {
                for(List<TriggerAPI> child : this.children) {
                    if(matchesChild(child,combo)) continue;
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected boolean matchesChild(List<TriggerAPI> child, TriggerCombination combo) {
        for(List<TriggerAPI> otherChild : combo.children)
            if(child.size()==otherChild.size())
                if(!TriggerHelper.matchesAll(child,otherChild)) return false;
        return true;
    }

    @Override
    public void setState(State state) {
        for(List<TriggerAPI> child : this.children)
            for(TriggerAPI trigger : child)
                trigger.setState(state);
    }

    protected void setParentStatus(boolean removal) {
        for(List<TriggerAPI> child : this.children)
            for(TriggerAPI trigger : child)
                setParentStatus(trigger,removal);
    }

    protected void setParentStatus(TriggerAPI trigger, boolean removal) {
        Set<TriggerCombination> parents = trigger.getParents();
        if(removal) parents.remove(this);
        else parents.add(this);
    }

    protected void setParentStatus(boolean removal, TriggerAPI ... triggers) {
        for(TriggerAPI trigger : triggers) setParentStatus(trigger,removal);
    }

    private void updatePriorityTrigger() {
        this.priorityTrigger = null;
        for(TriggerAPI trigger : this.priorityChildren)
            if(Objects.isNull(this.priorityTrigger) ||
                    trigger.getParameterAsInt("priority")>this.priorityTrigger.getParameterAsInt("priority"))
                this.priorityTrigger = trigger;
    }

    @Override
    public boolean verifyRequiredParameters() {
        for(List<TriggerAPI> child : this.children) {
            for(TriggerAPI trigger : child) {
                if(!trigger.verifyRequiredParameters()) {
                    logError("Unable to construct trigger combination due to 1 or more triggers failing verification!");
                    setParentStatus(true);
                    return false;
                }
            }
        }
        return true;
    }
}