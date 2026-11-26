package com.equipit.equipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 26);

    @Mock
    private EquipmentRepository repository;

    private EquipmentService equipmentService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-26T12:00:00Z"), ZoneOffset.UTC);
        equipmentService = new EquipmentService(repository, clock);
    }

    @Test
    void borrowEquipment_whenAvailable_marksAsBorrowedAndSaves() {
        Equipment equipment = new Equipment("Laptop", "Computer");
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.save(equipment)).thenReturn(equipment);

        Equipment result = equipmentService.borrowEquipment(1L, "Ada", "Lovelace");

        assertThat(result).isSameAs(equipment);
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getBorrowerFirstName()).isEqualTo("Ada");
        assertThat(result.getBorrowerLastName()).isEqualTo("Lovelace");
        assertThat(result.getBorrowedAt()).isEqualTo(TODAY);
        verify(repository).save(equipment);
    }

    @Test
    void borrowEquipment_whenPreviouslyReturned_keepsLastReturnedAtUnchanged() {
        Equipment equipment = new Equipment("Laptop", "Computer");
        LocalDate previousReturn = LocalDate.of(2026, 1, 15);
        equipment.borrow("Old", "Borrower", LocalDate.of(2026, 1, 1));
        equipment.giveBack(previousReturn);
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.save(equipment)).thenReturn(equipment);

        Equipment result = equipmentService.borrowEquipment(1L, "Ada", "Lovelace");

        assertThat(result.getLastReturnedAt()).isEqualTo(previousReturn);
        assertThat(result.getBorrowedAt()).isEqualTo(TODAY);
    }

    @Test
    void borrowEquipment_whenAlreadyBorrowed_throwsAndDoesNotSave() {
        Equipment equipment = new Equipment("Laptop", "Computer");
        equipment.borrow("Grace", "Hopper", LocalDate.of(2026, 11, 1));
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));

        assertThatThrownBy(() -> equipmentService.borrowEquipment(1L, "Ada", "Lovelace"))
                .isInstanceOf(EquipmentStateException.class);

        assertThat(equipment.getBorrowerFirstName()).isEqualTo("Grace");
        assertThat(equipment.getBorrowerLastName()).isEqualTo("Hopper");
        assertThat(equipment.getBorrowedAt()).isEqualTo(LocalDate.of(2026, 11, 1));
        verify(repository, never()).save(any());
    }

    @Test
    void borrowEquipment_whenNotFound_throwsAndDoesNotSave() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.borrowEquipment(99L, "Ada", "Lovelace"))
                .isInstanceOf(EquipmentNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void returnEquipment_whenBorrowed_marksAsAvailableClearsBorrowerAndSaves() {
        Equipment equipment = new Equipment("Laptop", "Computer");
        equipment.borrow("Ada", "Lovelace", LocalDate.of(2026, 11, 1));
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.save(equipment)).thenReturn(equipment);

        Equipment result = equipmentService.returnEquipment(1L);

        assertThat(result).isSameAs(equipment);
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getBorrowerFirstName()).isNull();
        assertThat(result.getBorrowerLastName()).isNull();
        assertThat(result.getBorrowedAt()).isNull();
        assertThat(result.getLastReturnedAt()).isEqualTo(TODAY);
        verify(repository).save(equipment);
    }

    @Test
    void returnEquipment_whenAlreadyAvailable_throwsAndDoesNotSave() {
        Equipment equipment = new Equipment("Laptop", "Computer");
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));

        assertThatThrownBy(() -> equipmentService.returnEquipment(1L))
                .isInstanceOf(EquipmentStateException.class);

        assertThat(equipment.isAvailable()).isTrue();
        assertThat(equipment.getLastReturnedAt()).isNull();
        verify(repository, never()).save(any());
    }

    @Test
    void returnEquipment_whenNotFound_throwsAndDoesNotSave() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.returnEquipment(99L))
                .isInstanceOf(EquipmentNotFoundException.class);

        verify(repository, never()).save(any());
    }
}
