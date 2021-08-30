package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lv.venta.enums.InquiryStatus;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Table(name = "inquiries")
@Getter @Setter
@NoArgsConstructor @ToString
public class Inquiry {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long inqId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private RegisteredUser inquiryUser;

    @Column(nullable = false)
    private InquiryStatus inquiryStatus = InquiryStatus.NEW;

    // Requested

    @OneToMany(mappedBy = "inquiry", orphanRemoval = true)
    @ToString.Exclude
    private Collection<InquiryItem> requestedItems;

    @Column(length = 512)
    private String comments;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateFrom;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateTo;

    // If approved

    @OneToMany(mappedBy = "assignInquiry", orphanRemoval = true)
    private Collection<AssignedInquiryItem> assignedItems;

    @Column(length = 2048)
    private String adminNotes;

    // If finished

    @Lob
    @Column
    private String finishedItemReport;

    public Inquiry(RegisteredUser inquiryUser, String comments, LocalDateTime dateFrom, LocalDateTime dateTo) {
        this.inquiryUser = inquiryUser;
        this.comments = comments;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}
