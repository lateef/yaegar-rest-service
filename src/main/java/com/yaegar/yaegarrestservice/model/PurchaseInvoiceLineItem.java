package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class PurchaseInvoiceLineItem extends AbstractLineItem {
    private static final long serialVersionUID = -2035642148704627220L;

    @Column(name = "purchase_order_line_item_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID purchaseOrderLineItemId;
}
