package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "settingentities")
@Getter @Setter
@NoArgsConstructor @ToString
public class SettingEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private long id;

    @Column(name = "settingkey")
    private String key;

    @Lob
    @Column(name = "settingcontent")
    private String content;

    public SettingEntity(String key, String content) {
        this.key = key;
        this.content = content;
    }
}