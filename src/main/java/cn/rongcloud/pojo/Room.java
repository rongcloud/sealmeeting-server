package cn.rongcloud.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Entity
@Table(name = "t_room")
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private @Getter @Setter String rid;
    private @Getter @Setter String name;
    private @Getter @Setter String portrait;
    private @Getter @Setter Date createDt;
    private @Getter @Setter String display;

    @Override
    public String toString() {
        return "Room{" +
                "rid='" + rid + '\'' +
                ", name='" + name + '\'' +
                ", portrait='" + portrait + '\'' +
                ", createDt=" + createDt +
                ", display='" + display + '\'' +
                '}';
    }
}
