package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class SalesInvoiceLineItem extends AbstractLineItem {
    private static final long serialVersionUID = 5776429744023037140L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;
}
