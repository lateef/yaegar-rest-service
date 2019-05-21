package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static com.yaegar.yaegarrestservice.model.enums.PaymentTerm.NONE;
import static com.yaegar.yaegarrestservice.model.enums.TransactionType.PURCHASE_ORDER;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PurchaseOrderRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Test
    public void whenSave_thenReturnPurchaseOrder() {
        //given
        final Company principalCompany = new Company("Principal company");
        entityManager.persist(principalCompany);
        entityManager.flush();

        final Supplier unknownSupplier = new Supplier();
        unknownSupplier.setName("UNKNOWN");
        unknownSupplier.setPrincipalCompany(principalCompany);

        final Transaction transaction = new Transaction();
        transaction.setTransactionType(PURCHASE_ORDER);
        entityManager.persist(transaction);
        entityManager.flush();

        PurchaseOrder expectedPurchaseOrder = new PurchaseOrder();
        expectedPurchaseOrder.setNumber(UUID.randomUUID());
        expectedPurchaseOrder.setPaymentTerm(NONE);
        expectedPurchaseOrder.setPaid(ZERO);
        expectedPurchaseOrder.setTotalPrice(ZERO);
        expectedPurchaseOrder.setSupplier(unknownSupplier);
        expectedPurchaseOrder.setTransaction(transaction);
        entityManager.persist(expectedPurchaseOrder);
        entityManager.flush();

        //when
        PurchaseOrder actualPurchaseOrder = purchaseOrderRepository.save(expectedPurchaseOrder);

        //then
        assertThat(actualPurchaseOrder, sameBeanAs(expectedPurchaseOrder));
    }

}