package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class PurchaseInvoice extends AbstractInvoice {
    private static final long serialVersionUID = -7193612821895434354L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "line_item_id", referencedColumnName = "id")
    private Set<PurchaseInvoiceLineItem> lineItems;
}
