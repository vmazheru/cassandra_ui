package com.ask.cui.ui.components.objecttree;

public enum DBObjectType {
    TABLE, VIEW;
    
    public String getDisplayName() {
        switch (this) {
            case TABLE: return "Tables";
            case VIEW: return "Views";
            default : return null;
        }
    }
}
