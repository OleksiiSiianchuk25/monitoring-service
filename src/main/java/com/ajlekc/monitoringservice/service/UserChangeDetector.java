package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserChangeDetector {

    private final UserRepository userRepository;

    public ChangeType classifyFetchedUser(User fetchedUser) {
        if (fetchedUser == null || fetchedUser.getExternalId() == null) {
            return ChangeType.NEW;
        }

        Optional<User> existing = userRepository.findFirstByExternalIdOrderByInternalId(fetchedUser.getExternalId());
        if (existing.isEmpty()) {
            log.debug("User {} is NEW", fetchedUser.getExternalId());
            return ChangeType.NEW;
        }

        User stored = existing.get();
        fetchedUser.setInternalId(stored.getInternalId());

        if (areEquivalent(stored, fetchedUser)) {
            log.debug("User {} is UNCHANGED", fetchedUser.getExternalId());
            return ChangeType.UNCHANGED;
        }

        log.debug("User {} is UPDATED", fetchedUser.getExternalId());
        return ChangeType.UPDATED;
    }

    // TODO: rewrite User hashcode and equals methods
    private boolean areEquivalent(User a, User b) {
        return Objects.equals(a.getName(), b.getName())
                && Objects.equals(a.getUsername(), b.getUsername())
                && Objects.equals(a.getEmail(), b.getEmail())
                && Objects.equals(a.getPhone(), b.getPhone())
                && Objects.equals(a.getWebsite(), b.getWebsite())
                && Objects.equals(a.getAddress(), b.getAddress())
                && Objects.equals(a.getCompany(), b.getCompany());
    }
}