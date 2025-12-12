package com.filadelfia.store.filadelfiastore.repository;

import com.filadelfia.store.filadelfiastore.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultAddressByUserId(@Param("userId") Long userId);
    
    boolean existsByUserIdAndIsDefaultTrue(Long userId);
    
    void deleteByUserIdAndId(Long userId, Long id);
}