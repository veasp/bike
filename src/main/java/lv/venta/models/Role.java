package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor
public class Role {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roleId;

    private String name;

    @OneToMany(mappedBy = "role")
    private Collection<RegisteredUser> users;

    public Role(String name) {
        this.name = name;
    }
}
