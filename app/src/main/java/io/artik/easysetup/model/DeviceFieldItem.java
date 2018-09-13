package io.artik.easysetup.model;

/**
 * Created by vsingh on 19/02/17.
 */

public class DeviceFieldItem {

    private String name;
    private String description;
    private String value;


    public DeviceFieldItem(String name, String value, String description) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
