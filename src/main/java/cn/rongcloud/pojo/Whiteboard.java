package cn.rongcloud.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Entity
@Table(name = "t_whiteboard")
public class Whiteboard implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Getter long id;

    private @Getter @Setter String rid;
    private @Getter @Setter String wbid;
    private @Getter @Setter String wbRoom;
    private @Getter @Setter String name;
    private @Getter @Setter String creator;
    private @Getter @Setter Date createDt;
    private @Getter @Setter int pgCount;
    private @Getter @Setter int curPg;
}
