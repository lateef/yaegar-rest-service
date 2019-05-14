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
public class SalesInvoice extends AbstractInvoice {
    private static final long serialVersionUID = -5384688012365826776L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "line_item_id", referencedColumnName = "id")
    private Set<SalesInvoiceLineItem> lineItems;
}
