package com.kardex.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Kardex {
    private Long id;

    private String factCode;

    private int quantity;

    private BigDecimal unitPrice;

    private String details;

    private MovementType type;

    private int balanceQuantity;

    private BigDecimal balanceUnitPrice;

    private ZonedDateTime date;

    private Long productId;

    private BigDecimal totalBalance;

    /**
     * Finaliza la entrada del kardex agregando fecha, actualizando detalles y generando código de factura si es necesario
     */
    public void finalizeKardexEntry() {
        addDate();
        generateAdjustmentFactCode();
        updateDetailIfNotNull();
    }

    private void addDate(){
        this.date = ZonedDateTime.now(ZoneId.of("America/Bogota"));
    }

    public void addPurchase(int lastQuantity, BigDecimal lastTotalBalance) {
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
        finalizeKardexEntry();
    }

    public void addSale(int lastQuantity, BigDecimal lastUnitPrice, BigDecimal lastTotalBalance) {
        // 1. Calcula la nueva cantidad total en el balance
        this.balanceQuantity = lastQuantity - this.quantity;
        
        if (this.balanceQuantity < 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }
        if (this.balanceQuantity == 0) {
            resetBalancesIfZero();
            finalizeKardexEntry(); 
            return;
        }
        
        // 2. se mantiene el último precio unitario para el balance
        this.balanceUnitPrice = lastUnitPrice;
        
        // 3. El precio unitario es el mismo que el del último balance
        this.unitPrice = lastUnitPrice;

        // 4. Calcula el valor total
        this.totalBalance = lastTotalBalance.subtract(this.unitPrice.multiply(BigDecimal.valueOf(this.quantity)) );
        finalizeKardexEntry();
    }

    public void returnOnSale(int lastQuantity, BigDecimal lastUnitPrice, BigDecimal lastTotalBalance) {
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
        finalizeKardexEntry();      
    }

    public void returnOnPurchase(int lastQuantity, BigDecimal lastTotalBalance) {
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
        finalizeKardexEntry();
    }


    public void resetBalancesIfZero(){
            this.totalBalance = BigDecimal.ZERO;
            this.balanceUnitPrice = BigDecimal.ZERO;
            this.balanceQuantity = 0;   
            this.unitPrice = BigDecimal.ZERO;  
    }

    private void updateDetailIfNotNull() {
        if (this.details == null || this.details.isEmpty()) {
            // Formar el detalle con el tipo de movimiento y el código.  Venta - Factura: 500
            this.details = String.format("%s - Factura: %s", this.type.getDescription(), this.factCode);
        }else{
            // Agregar el tipo de movimiento al detalle existente
            this.details = String.format("%s | %s - Factura: %s", this.details, this.type.getDescription(), this.factCode);
        }
    }

    /*
        Cuando el de factura es 0 es un ajuste de inventario entonces el factCode se genera un codigo de la siguiente manera:
        AYYMMDDHHMMSS(letra aleatoria) ejmplo A240614153045X
    */
    private void generateAdjustmentFactCode() {
        if (this.factCode != null && !this.factCode.isEmpty()) {
            return; // Ya tiene un código de factura válido
        }
        String timestamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        char randomLetter = (char) ('A' + new Random().nextInt(26));
        this.factCode = String.format("A%s%c", timestamp, randomLetter);
    }

}