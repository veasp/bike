package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "InquiryItems")
@Getter @Setter
@NoArgsConstructor @ToString
public class InquiryItem {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long inqItemId;

    @ManyToOne
    @JoinColumn(name = "inqId")
    private Inquiry inquiry;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private ItemGroup inqItemGroup;

    @ElementCollection
    @CollectionTable(name = "inq_item_params",
            joinColumns = {@JoinColumn(name = "inq_item_id", referencedColumnName = "inqItemId")})
    @MapKeyColumn(name = "param_key")
    @Column
    private Map<String, String> paramValue = new HashMap<>();

    public InquiryItem(Inquiry inquiry, ItemGroup inqItemGroup, Map<String, String> paramValue) {
        this.inquiry = inquiry;
        this.inqItemGroup = inqItemGroup;
        this.paramValue = paramValue;
    }
}
