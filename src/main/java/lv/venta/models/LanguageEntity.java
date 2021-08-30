package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "languages")
@Getter @Setter
@NoArgsConstructor @ToString
public class LanguageEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private long id;

    @Column
    private String locale;

    @Column(name = "messagekey")
    private String key;

    @Column(name = "messagecontent", length = 10000)
    private String content;

    public LanguageEntity(String locale, String key, String content) {
        this.locale = locale;
        this.key = key;
        this.content = content;
    }
}