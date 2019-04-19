package cn.rongcloud.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Entity
@Table(name = "t_room_member")
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private @Getter @Setter String uid;
    private @Getter @Setter String rid;
    private @Getter @Setter int role;
    private @Getter @Setter Date joinDt;
    private @Getter @Setter String name;
    private @Getter @Setter boolean camera = true;
    private @Getter @Setter boolean mic = true;

    public RoomMember() {
    }

    public RoomMember(String uid, String rid) {
        this.uid = uid;
        this.rid = rid;
    }

    @Override
    public String toString() {
        return "RoomMember{" +
                "uid='" + uid + '\'' +
                ", rid='" + rid + '\'' +
                ", role=" + role +
                ", joinDt=" + joinDt +
                ", name='" + name + '\'' +
                ", camera=" + camera +
                ", mic=" + mic +
                '}';
    }
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "room_id", referencedColumnName = "rid")
//    private @Getter @Setter Room room;
}
