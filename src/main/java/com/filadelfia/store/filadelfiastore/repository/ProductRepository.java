package com.filadelfia.store.filadelfiastore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.filadelfia.store.filadelfiastore.model.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByActiveTrue();
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    @NonNull Optional<Product> findById(@NonNull Long id);
    List<Product> findByCategoryNameAndActiveTrue(String categoryName);
    List<Product> findTop4ByActiveTrueOrderByIdDesc();
    
    // Stock management queries
    List<Product> findByActiveTrueAndStockLessThan(Integer threshold);
    Long countByActiveTrueAndStockLessThan(Integer threshold);

    // Enhanced search queries
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> findByNameOrDescriptionContainingIgnoreCaseAndActiveTrue(@Param("searchTerm") String searchTerm);
}