package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "GroupParameters")
@Getter @Setter
@NoArgsConstructor @ToString
public class GroupParameter {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long groupParamId;

    @Column(nullable = false, length = 32)
    private String paramName;

    @ManyToOne
    @JoinColumn(name = "groupId")
    @ToString.Exclude
    private ItemGroup itemGroup;

    @OneToMany(mappedBy = "paramGroup", orphanRemoval = true)
    private Collection<Parameter> parameters;

    @OneToMany(mappedBy = "joinGroupParam")
    private Collection<JoinItemParams> joinItems;

    public GroupParameter(String paramName, ItemGroup itemGroup) {
        this.paramName = paramName;
        this.itemGroup = itemGroup;
    }
}
