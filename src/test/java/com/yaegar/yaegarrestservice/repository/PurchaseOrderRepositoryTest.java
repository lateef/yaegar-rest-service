package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Payment;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static com.yaegar.yaegarrestservice.model.enums.PaymentType.PURCHASE_ORDER;
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
        final Payment payment = new Payment();
        payment.setPaymentType(PURCHASE_ORDER);
        entityManager.persist(payment);
        entityManager.flush();

        Set<Payment> payments = new HashSet<>();
        payments.add(payment);

        PurchaseOrder expectedPurchaseOrder = new PurchaseOrder();
        expectedPurchaseOrder.setPayments(payments);
        entityManager.persist(expectedPurchaseOrder);
        entityManager.flush();

        //when
        PurchaseOrder actualPurchaseOrder = purchaseOrderRepository.save(expectedPurchaseOrder);

        //then
        assertThat(actualPurchaseOrder, sameBeanAs(expectedPurchaseOrder));
    }

}