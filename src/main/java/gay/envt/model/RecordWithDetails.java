package gay.envt.model;

public class RecordWithDetails {
    private String name;
    private String data;

    public RecordWithDetails(String name, String data) {
        this.name = name;
        this.data = data;
    }

    public RecordWithDetails() {
        this.name = "";
        this.data = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
