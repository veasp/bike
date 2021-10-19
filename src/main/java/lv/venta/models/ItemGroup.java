package lv.venta.models;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "itemgroups")
@Getter @Setter @NoArgsConstructor @ToString
public class ItemGroup {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long groupId;

    @Column(nullable = false, length = 100)
    private String groupName;

    @Column(nullable = false, length = 1024)
    private String groupDesc;

    @Column(nullable = false)
    private double price;

    @OneToMany(mappedBy = "itemGroup", orphanRemoval = true)
    private Collection<GroupParameter> parameters;

    @Column
    @Lob
    private String image;

    @OneToMany(mappedBy = "itemGroup", orphanRemoval = true)
    @ToString.Exclude
    private Collection<Item> items;

    @OneToMany(mappedBy = "inqItemGroup", orphanRemoval = true)
    @ToString.Exclude
    private Collection<InquiryItem> inquiryItems;

    public ItemGroup(String groupName, String groupDesc, double price) {
        this.groupName = groupName;
        this.groupDesc = groupDesc;
        this.price = price;
    }
}