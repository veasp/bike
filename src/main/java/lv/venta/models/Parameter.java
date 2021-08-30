package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "Parameters")
@Getter @Setter
@NoArgsConstructor @ToString
public class Parameter {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long paramId;

    @ManyToOne
    @JoinColumn(name = "groupParamId")
    @ToString.Exclude
    private GroupParameter paramGroup;

    @Column(nullable = false)
    private String value;

    @OneToMany(mappedBy = "joinGroupParam")
    private Collection<JoinItemParams> joinItems;

    public Parameter(GroupParameter paramGroup, String value) {
        this.paramGroup = paramGroup;
        this.value = value;
    }
}
