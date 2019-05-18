package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class SalesOrderLineItem extends AbstractLineItem {
    private static final long serialVersionUID = -3349984764721381940L;
}
