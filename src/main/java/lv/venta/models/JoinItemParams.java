package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "join_item_params")
@Getter @Setter
@NoArgsConstructor
public class JoinItemParams {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long joinId;

    @ManyToOne
    @JoinColumn(name = "itemId")
    private Item joinItem;

    @ManyToOne
    @JoinColumn(name = "groupParamId")
    private GroupParameter joinGroupParam;

    @ManyToOne
    @JoinColumn(name = "paramId")
    private Parameter joinParam;

    public JoinItemParams(Item joinItem, GroupParameter joinGroupParam, Parameter joinParam) {
        this.joinItem = joinItem;
        this.joinGroupParam = joinGroupParam;
        this.joinParam = joinParam;
    }

    @Override
    public String toString() {
        return "JoinItemParams{GroupParam=" + joinGroupParam.getParamName() + ", value=" + joinParam.getValue() + '}';
    }
}
