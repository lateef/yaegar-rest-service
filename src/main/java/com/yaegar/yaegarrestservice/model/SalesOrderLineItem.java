package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class SalesOrderLineItem extends AbstractLineItem {
    private static final long serialVersionUID = -3349984764721381940L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;
}
