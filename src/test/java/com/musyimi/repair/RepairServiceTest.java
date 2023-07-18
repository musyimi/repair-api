package com.musyimi.repair;

import com.musyimi.exception.DuplicateResourceException;
import com.musyimi.exception.RequestValidationException;
import com.musyimi.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock
    private RepairDao repairDao;
    private RepairService underTest;

    @BeforeEach
    void setUp() {
        underTest = new RepairService(repairDao);
    }

    @Test
    void getAllRepairs() {
        underTest.getAllRepairs();
        verify(repairDao).selectAllRepairs();
    }

    @Test
    void canGetRepair() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia", "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        Repair actual = underTest.getRepair(1);

        assertThat(actual).isEqualTo(repair);
    }

    @Test
    void willThrowWhenRepairReturnEmptyOptional() {
        int id = 1;

        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getRepair(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Repair with id [%s] is not found".formatted(id));
    }

    @Test
    void addRepair() {
        Integer phoneNumber = 800565222;

        when(repairDao.existsPersonWithPhoneNumber(phoneNumber)).thenReturn(false);

        RepairRegistrationRequest repairRegistrationRequest = new RepairRegistrationRequest(
          "Zumba", "HP Envy", "HP", "nOT CHSRGING", 800565222
        );

        underTest.addRepair(repairRegistrationRequest);

        ArgumentCaptor<Repair> repairArgumentCaptor =
                ArgumentCaptor.forClass(Repair.class);

        verify(repairDao).insertRepair(repairArgumentCaptor.capture());

        Repair capturedRepair = repairArgumentCaptor.getValue();

        assertThat(capturedRepair.getId()).isNull();
        assertThat(capturedRepair.getName()).isEqualTo(repairRegistrationRequest.name());
        assertThat(capturedRepair.getTitle()).isEqualTo(repairRegistrationRequest.title());
        assertThat(capturedRepair.getBrand()).isEqualTo(repairRegistrationRequest.brand());
        assertThat(capturedRepair.getIssue()).isEqualTo(repairRegistrationRequest.issue());
        assertThat(capturedRepair.getphoneNumber()).isEqualTo(repairRegistrationRequest.phoneNumber());
    }

    @Test
    void willThrowAnErrorIfPhoneNumberExistswhileaddingRepair() {
        Integer phoneNumber = 800565222;

        when(repairDao.existsPersonWithPhoneNumber(phoneNumber)).thenReturn(true);

        RepairRegistrationRequest repairRegistrationRequest = new RepairRegistrationRequest(
                "Zumba", "HP Envy", "HP", "not cgharging", 800565222
        );

        assertThatThrownBy(() -> underTest.addRepair(repairRegistrationRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Phone Number already in use");

        verify(repairDao, never()).insertRepair(any());

       }

    @Test
    void deleteById() {
        int id = 1;

        when(repairDao.existsRepairWithId(id)).thenReturn(true);

        underTest.deleteById(id);

        verify(repairDao).deleteById(id);

    }

    @Test
    void willThrowDeleteByIdNotFound() {
        int id = 1;

        when(repairDao.existsRepairWithId(id)).thenReturn(false);

        assertThatThrownBy(() -> underTest.deleteById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessage("Repair with id [%s] not found".formatted(id));

        verify(repairDao, never()).deleteById(id);

    }

    @Test
    void canUpdateAllRepairProperties() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia",
                "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        RepairUpdateRequest updateRequest = new RepairUpdateRequest(
                "Kamah", "Nokia 3310", "Nokaya",700000000, "Not charging"
        );

        when(repairDao.existsPersonWithPhoneNumber(700000000)).thenReturn(false);

        underTest.updateRepair(id, updateRequest);

        ArgumentCaptor<Repair> repairArgumentCaptor = ArgumentCaptor.forClass(Repair.class);

        verify(repairDao).updateRepair(repairArgumentCaptor.capture());

        Repair capturedRepair = repairArgumentCaptor.getValue();

        assertThat(capturedRepair.getphoneNumber()).isEqualTo(updateRequest.phoneNumber());
        assertThat(capturedRepair.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedRepair.getTitle()).isEqualTo(updateRequest.title());
        assertThat(capturedRepair.getIssue()).isEqualTo(updateRequest.issue());
        assertThat(capturedRepair.getBrand()).isEqualTo(updateRequest.brand());


    }

    @Test
    void canUpdateOnlyRepairName() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia",
                "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        RepairUpdateRequest updateRequest = new RepairUpdateRequest(
                "Kamah", null, null,null, null
        );

        underTest.updateRepair(id, updateRequest);

        ArgumentCaptor<Repair> repairArgumentCaptor = ArgumentCaptor.forClass(Repair.class);

        verify(repairDao).updateRepair(repairArgumentCaptor.capture());

        Repair capturedRepair = repairArgumentCaptor.getValue();

        assertThat(capturedRepair.getphoneNumber()).isEqualTo(repair.getphoneNumber());
        assertThat(capturedRepair.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedRepair.getTitle()).isEqualTo(repair.getTitle());
        assertThat(capturedRepair.getIssue()).isEqualTo(repair.getIssue());
        assertThat(capturedRepair.getBrand()).isEqualTo(repair.getBrand());


    }

    @Test
    void canUpdateOnlyRepairTitle() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia",
                "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        RepairUpdateRequest updateRequest = new RepairUpdateRequest(
                null, "Nokia", null,null, null
        );

        underTest.updateRepair(id, updateRequest);

        ArgumentCaptor<Repair> repairArgumentCaptor = ArgumentCaptor.forClass(Repair.class);

        verify(repairDao).updateRepair(repairArgumentCaptor.capture());

        Repair capturedRepair = repairArgumentCaptor.getValue();

        assertThat(capturedRepair.getphoneNumber()).isEqualTo(repair.getphoneNumber());
        assertThat(capturedRepair.getName()).isEqualTo(repair.getName());
        assertThat(capturedRepair.getTitle()).isEqualTo(updateRequest.title());
        assertThat(capturedRepair.getIssue()).isEqualTo(repair.getIssue());
        assertThat(capturedRepair.getBrand()).isEqualTo(repair.getBrand());


    }

    @Test
    void canUpdateOnlyRepairBrand() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia",
                "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        RepairUpdateRequest updateRequest = new RepairUpdateRequest(
                null, null, "Samsung",null, null
        );

        underTest.updateRepair(id, updateRequest);

        ArgumentCaptor<Repair> repairArgumentCaptor = ArgumentCaptor.forClass(Repair.class);

        verify(repairDao).updateRepair(repairArgumentCaptor.capture());

        Repair capturedRepair = repairArgumentCaptor.getValue();

        assertThat(capturedRepair.getphoneNumber()).isEqualTo(repair.getphoneNumber());
        assertThat(capturedRepair.getName()).isEqualTo(repair.getName());
        assertThat(capturedRepair.getTitle()).isEqualTo(repair.getTitle());
        assertThat(capturedRepair.getIssue()).isEqualTo(repair.getIssue());
        assertThat(capturedRepair.getBrand()).isEqualTo(updateRequest.brand());


    }

    @Test
    void canUpdateOnlyRepairIssue() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia",
                "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        RepairUpdateRequest updateRequest = new RepairUpdateRequest(
                null, null, null,null, "Power IC issue"
        );

        underTest.updateRepair(id, updateRequest);

        ArgumentCaptor<Repair> repairArgumentCaptor = ArgumentCaptor.forClass(Repair.class);

        verify(repairDao).updateRepair(repairArgumentCaptor.capture());

        Repair capturedRepair = repairArgumentCaptor.getValue();

        assertThat(capturedRepair.getphoneNumber()).isEqualTo(repair.getphoneNumber());
        assertThat(capturedRepair.getName()).isEqualTo(repair.getName());
        assertThat(capturedRepair.getTitle()).isEqualTo(repair.getTitle());
        assertThat(capturedRepair.getIssue()).isEqualTo(updateRequest.issue());
        assertThat(capturedRepair.getBrand()).isEqualTo(repair.getBrand());


    }

    @Test
    void canUpdateOnlyRepairPhoneNumber() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia",
                "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        RepairUpdateRequest updateRequest = new RepairUpdateRequest(
                null, null, null,900000111, null
        );

        underTest.updateRepair(id, updateRequest);

        ArgumentCaptor<Repair> repairArgumentCaptor = ArgumentCaptor.forClass(Repair.class);

        verify(repairDao).updateRepair(repairArgumentCaptor.capture());

        Repair capturedRepair = repairArgumentCaptor.getValue();

        assertThat(capturedRepair.getphoneNumber()).isEqualTo(updateRequest.phoneNumber());
        assertThat(capturedRepair.getName()).isEqualTo(repair.getName());
        assertThat(capturedRepair.getTitle()).isEqualTo(repair.getTitle());
        assertThat(capturedRepair.getIssue()).isEqualTo(repair.getIssue());
        assertThat(capturedRepair.getBrand()).isEqualTo(repair.getBrand());


    }

    @Test
    void willThrowWhenRepairPhoneNumberExists() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia",
                "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        RepairUpdateRequest updateRequest = new RepairUpdateRequest(
                null, null, null,900000111, null
        );

        when(repairDao.existsPersonWithPhoneNumber(900000111)).thenReturn(true);

        assertThatThrownBy(() -> underTest.updateRepair(id, updateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Phone Number Already in use");


        verify(repairDao, never()).updateRepair(any());

    }

    @Test
    void willThrowWhenNoChangesDone() {
        int id = 1;

        Repair repair = new Repair("Kamau", "Nikia 3300", "Nokia",
                "Charging port", 800565222);
        when(repairDao.selectRepairById(id))
                .thenReturn(Optional.of(repair));

        RepairUpdateRequest updateRequest = new RepairUpdateRequest(
                repair.getName(),
                repair.getTitle(),
                repair.getBrand(),
                repair.getphoneNumber(),
                repair.getIssue()
        );

        assertThatThrownBy(() -> underTest.updateRepair(id, updateRequest))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("No data changes found");

        verify(repairDao, never()).updateRepair(any());


    }


}