package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;

public abstract class GlobalElement extends ParameterWrapper {
    
    protected GlobalElement(String name) {
        super(name);
    }
    
    @Override public Class<? extends ParameterWrapper> getTypeClass() {
        return GlobalElement.class;
    }
    
    @Override protected String getLogPrefix() {
        return "Global";
    }
}