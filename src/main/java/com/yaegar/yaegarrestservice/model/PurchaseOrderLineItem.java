package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class PurchaseOrderLineItem extends AbstractLineItem {
    private static final long serialVersionUID = 7301006936594728643L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;
}
