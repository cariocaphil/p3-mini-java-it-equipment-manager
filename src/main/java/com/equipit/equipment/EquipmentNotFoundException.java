package com.equipit.equipment;

public class EquipmentNotFoundException extends RuntimeException {
    public EquipmentNotFoundException(Long id) {
        super("Equipment %d not found".formatted(id));
    }
}
