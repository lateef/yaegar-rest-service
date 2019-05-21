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
public class SalesInvoiceLineItem extends AbstractLineItem {
    private static final long serialVersionUID = 5776429744023037140L;

    @Column(name = "sales_order_line_item_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID salesOrderLineItemId;
}
