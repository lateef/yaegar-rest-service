package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class PurchaseInvoiceLineItem extends AbstractLineItem {
    private static final long serialVersionUID = -2035642148704627220L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;
}
