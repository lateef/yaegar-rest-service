package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class StockTransaction extends AbstractEntity {
    private static final long serialVersionUID = -7576476879020788149L;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "purchase_invoice_line_item_id", referencedColumnName = "id")
    private PurchaseInvoiceLineItem purchaseInvoiceLineItem;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "sales_invoice_line_item_id", referencedColumnName = "id")
    private SalesInvoiceLineItem salesInvoiceLineItem;
}
