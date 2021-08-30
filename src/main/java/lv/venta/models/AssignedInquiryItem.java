package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "assigned_inquiry_item")
@Getter @Setter
@NoArgsConstructor
public class AssignedInquiryItem {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long assignId;

    @ManyToOne
    @JoinColumn(name = "itemId")
    private Item assignItem;

    @ManyToOne
    @JoinColumn(name = "inqId")
    private Inquiry assignInquiry;

    public AssignedInquiryItem(Item assignItem, Inquiry assignInquiry) {
        this.assignItem = assignItem;
        this.assignInquiry = assignInquiry;
    }
}
