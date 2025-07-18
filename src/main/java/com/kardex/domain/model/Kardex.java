package com.kardex.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Kardex {
    private Long id;

    private Long quantity;

    private BigDecimal unitPrice;

    private String details;

    private Long balanceQuantity;

    private BigDecimal balanceUnitPrice;

    private ZonedDateTime date;

    private Product product;

    public void addDate(){
        this.date = ZonedDateTime.now(ZoneId.of("America/Bogota"));
    }

    public void addPurchaseBalance(Long lastQuantity, BigDecimal lastUnitPrice) {
        // 1. Calcula la nueva cantidad total en el balance
        this.balanceQuantity = lastQuantity + this.quantity;

        // Si no hay cantidad total, el precio es cero para evitar división por cero.
        if (this.balanceQuantity == 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }

        // 2. Calcula el valor total del inventario anterior
        BigDecimal lastTotalValue = lastUnitPrice.multiply(BigDecimal.valueOf(lastQuantity));
        
        // 3. Calcula el valor total de la compra actual
        BigDecimal currentTotalValue = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));

        // 4. Suma ambos valores para obtener el nuevo valor total del inventario
        BigDecimal totalValue = lastTotalValue.add(currentTotalValue);

        // 5. Convierte la cantidad total a BigDecimal para la división
        BigDecimal totalQuantityBigDecimal = BigDecimal.valueOf(this.balanceQuantity);

        // 6. Divide el valor total entre la cantidad total para el promedio ponderado
        this.balanceUnitPrice = totalValue.divide(totalQuantityBigDecimal, 4, RoundingMode.HALF_UP);   
    }

    public void addSaleBalance(Long lastQuantity, BigDecimal lastUnitPrice) {
        // 1. Calcula la nueva cantidad total en el balance
        this.balanceQuantity = lastQuantity - this.quantity;

        if (this.balanceQuantity == 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }

        // 2. se mantiene el último precio unitario para el balance
        this.balanceUnitPrice = lastUnitPrice;

        // 3. El precio unitario es el mismo que el del último balance
        this.unitPrice = lastUnitPrice;

    }

}
