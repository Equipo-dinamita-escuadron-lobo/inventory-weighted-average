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

    private Long factCode;

    private Long quantity;

    private BigDecimal unitPrice;

    private String details;

    private MovementType type;

    private Long balanceQuantity;

    private BigDecimal balanceUnitPrice;

    private ZonedDateTime date;

    private Long idProduct;

    private BigDecimal totalBalance;

    public void addDate(){
        this.date = ZonedDateTime.now(ZoneId.of("America/Bogota"));
    }

    public void addPurchase(Long lastQuantity, BigDecimal lastTotalBalance) {
        // 1. Calcula la nueva cantidad total en el balance
        this.balanceQuantity = lastQuantity + this.quantity;

        // Si no hay cantidad total, el precio es cero para evitar división por cero.
        if (this.balanceQuantity == 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }
        
        // 2. Calcula el valor total de la compra actual
        BigDecimal currentTotalValue = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));

        // 3. Suma ambos valores para obtener el nuevo valor total del inventario
        this.totalBalance = lastTotalBalance.add(currentTotalValue);

        // 4. Convierte la cantidad total a BigDecimal para la división
        BigDecimal totalQuantityBigDecimal = BigDecimal.valueOf(this.balanceQuantity);

        // 5. Divide el valor total entre la cantidad total para el promedio ponderado
        this.balanceUnitPrice = this.totalBalance.divide(totalQuantityBigDecimal, 2, RoundingMode.HALF_UP);
    }

    public void addSale(Long lastQuantity, BigDecimal lastUnitPrice, BigDecimal lastTotalBalance) {
        // 1. Calcula la nueva cantidad total en el balance
        this.balanceQuantity = lastQuantity - this.quantity;

        if (this.balanceQuantity < 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }
        if (this.balanceQuantity == 0) {
            resetBalancesIfZero();
            return;
        }

        // 2. se mantiene el último precio unitario para el balance
        this.balanceUnitPrice = lastUnitPrice;

        // 3. El precio unitario es el mismo que el del último balance
        this.unitPrice = lastUnitPrice;

        // 4. Calcula el valor total
        this.totalBalance = lastTotalBalance.subtract(this.unitPrice.multiply(BigDecimal.valueOf(this.quantity)) );

    }

    public void returnOnSale(Long lastQuantity, BigDecimal lastUnitPrice, BigDecimal lastTotalBalance) {
        // 1. Calcula la nueva cantidad total en el balance
        this.balanceQuantity = lastQuantity + this.quantity;

        if (this.balanceQuantity == 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }

        // 2. el balance total se aumenta
        this.totalBalance = lastUnitPrice.multiply(BigDecimal.valueOf(this.quantity)).add(lastTotalBalance);

        // 3. se calcula el nuevo precio unitario
        this.balanceUnitPrice = this.totalBalance.divide(BigDecimal.valueOf(this.balanceQuantity), 2, RoundingMode.HALF_UP);
        
    }

    public void returnOnPurchase(Long lastQuantity, BigDecimal lastTotalBalance) {
        // 1. Calcula la nueva cantidad total en el balance
        this.balanceQuantity = lastQuantity - this.quantity;

        // 
        if (this.balanceQuantity < 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }
        if (this.balanceQuantity == 0) {
            resetBalancesIfZero();
            return;
        }

        // 2. Calcula el valor total
        this.totalBalance = lastTotalBalance.subtract(this.unitPrice.multiply(BigDecimal.valueOf(this.quantity)) );

        // 4. Convierte la cantidad total a BigDecimal para la división
        BigDecimal totalQuantityBigDecimal = BigDecimal.valueOf(this.balanceQuantity);

        // 5. Divide el valor total entre la cantidad total para el promedio ponderado
        this.balanceUnitPrice = this.totalBalance.divide(totalQuantityBigDecimal, 2, RoundingMode.HALF_UP);
    }


    public void resetBalancesIfZero(){
            this.totalBalance = BigDecimal.ZERO;
            this.balanceUnitPrice = BigDecimal.ZERO;
            this.balanceQuantity = 0L;     
    }

}
