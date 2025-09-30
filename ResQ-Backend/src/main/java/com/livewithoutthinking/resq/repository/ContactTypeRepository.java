package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactTypeRepository extends JpaRepository<ContactType, Integer> {
}
