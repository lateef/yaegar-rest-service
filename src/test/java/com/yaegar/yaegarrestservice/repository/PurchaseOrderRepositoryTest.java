package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static com.yaegar.yaegarrestservice.model.enums.TransactionType.PURCHASE_ORDER;
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
        final Transaction transaction = new Transaction();
        transaction.setTransactionType(PURCHASE_ORDER);
        entityManager.persist(transaction);
        entityManager.flush();

        PurchaseOrder expectedPurchaseOrder = new PurchaseOrder();
        expectedPurchaseOrder.setTransaction(transaction);
        entityManager.persist(expectedPurchaseOrder);
        entityManager.flush();

        //when
        PurchaseOrder actualPurchaseOrder = purchaseOrderRepository.save(expectedPurchaseOrder);

        //then
        assertThat(actualPurchaseOrder, sameBeanAs(expectedPurchaseOrder));
    }

}