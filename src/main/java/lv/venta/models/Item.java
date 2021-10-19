package lv.venta.models;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lv.venta.enums.ItemStatus;

import java.util.Collection;

@Entity
@Table(name = "items")
@Getter @Setter @NoArgsConstructor @ToString
public class Item {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long itemId;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column
    private int quantity;

    @Column
    private ItemStatus status = ItemStatus.HIDDEN;

    @Column
    @Lob
    private String image;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private ItemGroup itemGroup;

    @OneToMany(mappedBy = "joinItem", orphanRemoval = true)
    private Collection<JoinItemParams> joinItems;

    @OneToMany(mappedBy = "assignItem")
    private Collection<AssignedInquiryItem> assignedInquiries;

    public Item(String itemName, int quantity, ItemStatus status, ItemGroup itemGroup) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.status = status;
        this.itemGroup = itemGroup;
    }
}