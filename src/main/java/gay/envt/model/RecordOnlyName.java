package gay.envt.model;

public class RecordOnlyName {
    private String name;

    public RecordOnlyName() {
        this.name = "";
    }

    public RecordOnlyName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
