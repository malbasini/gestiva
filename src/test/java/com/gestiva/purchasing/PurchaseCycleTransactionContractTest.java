package com.gestiva.purchasing;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.service.GoodsReceiptService;
import com.gestiva.purchasing.invoice.service.SupplierInvoiceService;
import com.gestiva.purchasing.order.web.PurchaseOrderWebService;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseCycleTransactionContractTest {

    private void assertClassTransactional(Class<?> serviceClass) {
        Transactional annotation =
                AnnotatedElementUtils.findMergedAnnotation(
                        serviceClass,
                        Transactional.class
                );

        assertNotNull(
                annotation,
                serviceClass.getSimpleName()
                        + " deve essere transazionale."
        );

        assertFalse(
                annotation.readOnly(),
                "Il servizio deve consentire scritture transazionali."
        );
    }

    @Test
    void goodsReceiptServiceMustBeTransactional() {
        assertClassTransactional(GoodsReceiptService.class);
    }

    @Test
    void supplierInvoiceServiceMustBeTransactional() {
        assertClassTransactional(SupplierInvoiceService.class);
    }

    @Test
    void purchaseOrderWebServiceMustBeTransactional() {
        assertClassTransactional(PurchaseOrderWebService.class);
    }

    @Test
    void businessExceptionMustTriggerDefaultRollbackAndRegistrationMustCoverCheckedExceptions()
            throws Exception {

        assertTrue(
                RuntimeException.class.isAssignableFrom(
                        BusinessException.class
                ),
                "BusinessException deve essere una RuntimeException."
        );

        Method registration =
                SupplierInvoiceService.class.getMethod(
                        "registerSupplierInvoice",
                        Long.class,
                        Long.class
                );

        Transactional annotation =
                AnnotatedElementUtils.findMergedAnnotation(
                        registration,
                        Transactional.class
                );

        assertNotNull(
                annotation,
                "La registrazione deve dichiarare @Transactional."
        );

        assertTrue(
                Arrays.asList(annotation.rollbackFor())
                        .contains(Exception.class),
                "La registrazione deve prevedere rollbackFor = Exception.class."
        );
    }
}
