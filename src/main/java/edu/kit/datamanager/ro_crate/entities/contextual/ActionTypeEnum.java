package edu.kit.datamanager.ro_crate.entities.contextual;

/**
 * Enumeration class representing action types.
 * @author sabrinechelbi
 */
public enum ActionTypeEnum {

    CREATE("CreateAction"),
    UPDATE("UpdateAction");

    private final String name;

    ActionTypeEnum(String name) {
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
}
