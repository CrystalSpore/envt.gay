package gay.envt.model;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DNSRecordJsonBody {
    private String type;
    private String name;
    private String data;
    @Nullable
    private final Integer priority = null;
    @Nullable
    private final Integer port = null;
    private final Integer ttl = 1800;
    @Nullable
    private final Integer weight = null;
    @Nullable
    private final Integer flags = null;
    @Nullable
    private final Integer tag = null;

    public DNSRecordJsonBody(String type, String name, String data) {
        this.type = type;
        this.name = name;
        this.data = data;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("type", this.type);
        ret.put("name", this.name);
        ret.put("data", this.data);
        ret.put("priority", this.priority);
        ret.put("port", this.port);
        ret.put("ttl", this.ttl);
        ret.put("weight", this.weight);
        ret.put("flags", this.flags);
        ret.put("tag", this.tag);
        return ret;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Nullable
    public Integer getPriority() {
        return priority;
    }

    @Nullable
    public Integer getPort() {
        return port;
    }

    public Integer getTtl() {
        return ttl;
    }

    @Nullable
    public Integer getWeight() {
        return weight;
    }

    @Nullable
    public Integer getFlags() {
        return flags;
    }

    @Nullable
    public Integer getTag() {
        return tag;
    }
}
