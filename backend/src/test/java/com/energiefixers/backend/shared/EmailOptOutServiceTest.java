package com.energiefixers.backend.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOptOutServiceTest {

    @Mock
    private EmailOptOutRepository repository;

    private EmailOptOutService service;

    @BeforeEach
    void setUp() {
        service = new EmailOptOutService(repository);
    }

    @Test
    void isOptedOut_returnsTrue_whenRecordExistsAndOptedOut() {
        EmailOptOut record = new EmailOptOut();
        record.setOptedOutAt(LocalDateTime.now());
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        assertTrue(service.isOptedOut("test@example.com"));
    }

    @Test
    void isOptedOut_returnsFalse_whenRecordExistsButNotOptedOut() {
        EmailOptOut record = new EmailOptOut();
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        assertFalse(service.isOptedOut("test@example.com"));
    }

    @Test
    void isOptedOut_returnsFalse_whenNoRecordExists() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertFalse(service.isOptedOut("test@example.com"));
    }

    @Test
    void isOptedOut_normalizesEmail() {
        EmailOptOut record = new EmailOptOut();
        record.setOptedOutAt(LocalDateTime.now());
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        assertTrue(service.isOptedOut("  TEST@Example.COM  "));
    }

    @Test
    void getOrCreateToken_returnsExistingToken() {
        EmailOptOut record = new EmailOptOut();
        record.setUnsubscribeToken("existing-token");
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        String token = service.getOrCreateToken("test@example.com");

        assertEquals("existing-token", token);
        verify(repository, never()).save(any());
    }

    @Test
    void getOrCreateToken_createsNewRecordWhenNotFound() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String token = service.getOrCreateToken("test@example.com");

        assertNotNull(token);
        verify(repository).save(any());
    }

    @Test
    void optOutByEmail_setsOptedOutOnExistingRecord() {
        EmailOptOut record = new EmailOptOut();
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        service.optOutByEmail("test@example.com");

        assertNotNull(record.getOptedOutAt());
        verify(repository, never()).save(any());
    }

    @Test
    void optOutByEmail_createsNewRecordWhenNotFound() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.optOutByEmail("test@example.com");

        verify(repository).save(any());
    }

    @Test
    void optOutByEmail_doesNothingWhenAlreadyOptedOut() {
        EmailOptOut record = new EmailOptOut();
        LocalDateTime optedOutAt = LocalDateTime.now();
        record.setOptedOutAt(optedOutAt);
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        service.optOutByEmail("test@example.com");

        assertEquals(optedOutAt, record.getOptedOutAt());
        verify(repository, never()).save(any());
    }

    @Test
    void optInByEmail_clearsOptedOutOnExistingRecord() {
        EmailOptOut record = new EmailOptOut();
        record.setOptedOutAt(LocalDateTime.now());
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        service.optInByEmail("test@example.com");

        assertNull(record.getOptedOutAt());
        verify(repository, never()).save(any());
    }

    @Test
    void optInByEmail_doesNothingWhenAlreadyOptedIn() {
        EmailOptOut record = new EmailOptOut();
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        service.optInByEmail("test@example.com");

        assertNull(record.getOptedOutAt());
        verify(repository, never()).save(any());
    }

    @Test
    void optInByEmail_doesNothingWhenNoRecordExists() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        service.optInByEmail("test@example.com");

        verify(repository, never()).save(any());
    }
}
