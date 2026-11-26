package com.equipit.equipment;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentService {
    private final EquipmentRepository repository;
    private final Clock clock;

    public EquipmentService(EquipmentRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Equipment> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Equipment borrowEquipment(Long id, String firstName, String lastName) {
        Equipment equipment = repository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(id));
        equipment.borrow(firstName, lastName, LocalDate.now(clock));
        return repository.save(equipment);
    }

    @Transactional
    public Equipment returnEquipment(Long id) {
        Equipment equipment = repository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(id));
        equipment.giveBack(LocalDate.now(clock));
        return repository.save(equipment);
    }
}