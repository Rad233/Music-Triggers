package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Collections;
import java.util.Map;

public class TriggerMob extends HolderTrigger {

    public TriggerMob(ChannelAPI channel) {
        super(channel,"mob");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"champion",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"detection_range",new ParameterInt(16));
        addParameter(map,"detection_y_ratio",new ParameterFloat(0.5f));
        addParameter(map,"display_matcher",new ParameterString("EXACT"));
        addParameter(map,"display_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"health",new ParameterFloat(100f));
        addParameter(map,"horde_health_percentage",new ParameterFloat(50f));
        addParameter(map,"horde_targeting_percentage",new ParameterFloat(50f));
        addParameter(map,"infernal",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"level",new ParameterInt(1));
        addParameter(map,"mob_nbt",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"mob_targeting",new ParameterBoolean(true));
        addParameter(map,"resource_matcher",new ParameterString("PARTIAL"));
        addParameter(map,"resource_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"victory_id",new ParameterString("not_set"));
        addParameter(map,"victory_percentage",new ParameterFloat(100f));
    }

    @Override
    public boolean isActive(TriggerContextAPI<?,?> ctx) {
        return ctx.isActiveMob(this);
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @Override
    public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"display_name","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
