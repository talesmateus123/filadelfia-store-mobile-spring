package com.filadelfia.store.filadelfiastore.util;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class PageableValidator {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> FORBIDDEN_SORT_PROPERTIES = Set.of("password", "secret", "token");

    public Pageable validateAndSanitize(Pageable pageable, Set<String> allowedSortProperties) {
        int page = validatePage(pageable.getPageNumber());
        int size = validateSize(pageable.getPageSize());
        Sort sort = validateSort(pageable.getSort(), allowedSortProperties);
        
        return PageRequest.of(page, size, sort);
    }

    public Pageable validateAndSanitize(Pageable pageable, Set<String> allowedSortProperties, String defaultSort) {
        int page = validatePage(pageable.getPageNumber());
        int size = validateSize(pageable.getPageSize());
        Sort sort = validateSort(pageable.getSort(), allowedSortProperties, defaultSort);
        
        return PageRequest.of(page, size, sort);
    }

    public Pageable validatePagination(Pageable pageable) {
        int page = validatePage(pageable.getPageNumber());
        int size = validateSize(pageable.getPageSize());
        
        return PageRequest.of(page, size);
    }

    private int validatePage(int page) {
        return Math.max(page, 0);
    }

    private int validateSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private Sort validateSort(Sort sort, Set<String> allowedSortProperties) {
        return validateSort(sort, allowedSortProperties, null);
    }

    private Sort validateSort(Sort sort, Set<String> allowedSortProperties, String defaultSort) {
        if (sort == null || sort.isUnsorted()) {
            return createDefaultSort(defaultSort);
        }

        List<Sort.Order> safeOrders = new ArrayList<>();
        Set<String> usedProperties = new HashSet<>();

        for (Sort.Order order : sort) {
            String property = order.getProperty();
            
            // Valida cada propriedade e lança exception se for inválida
            validateSortProperty(property, allowedSortProperties, usedProperties);
            
            usedProperties.add(property);
            safeOrders.add(new Sort.Order(order.getDirection(), property));
        }

        return safeOrders.isEmpty() ? 
            createDefaultSort(defaultSort) : 
            Sort.by(safeOrders);
    }

    /**
     * Método alterado: agora valida e LANÇA EXCEPTION para propriedades inválidas
     * em vez de retornar boolean
     */
    private void validateSortProperty(String property, Set<String> allowedSortProperties, Set<String> usedProperties) {
        // Validações básicas
        if (property == null || property.trim().isEmpty()) {
            throw new IllegalArgumentException("Propriedade de ordenação não pode ser nula ou vazia");
        }

        // Verifica se é uma propriedade proibida
        if (FORBIDDEN_SORT_PROPERTIES.contains(property.toLowerCase())) {
            throw new IllegalArgumentException(
                "Campo de ordenação proibido por segurança: '" + property + "'"
            );
        }

        // Verifica se a propriedade é permitida
        if (!allowedSortProperties.contains(property)) {
            throw new IllegalArgumentException(
                "Campo de ordenação não permitido: '" + property + 
                "'. Campos permitidos: " + allowedSortProperties
            );
        }

        // Verifica duplicatas
        if (usedProperties.contains(property)) {
            throw new IllegalArgumentException(
                "Campo de ordenação duplicado: '" + property + "'"
            );
        }
    }

    private Sort createDefaultSort(String defaultSort) {
        if (defaultSort != null && !defaultSort.trim().isEmpty()) {
            return Sort.by(defaultSort).ascending();
        }
        return Sort.unsorted();
    }

    public Pageable createDefaultPageable() {
        return PageRequest.of(0, DEFAULT_PAGE_SIZE);
    }

    public void validateSortProperties(Set<String> requestedSort, Set<String> allowedSortProperties) {
        Set<String> invalidProperties = new HashSet<>(requestedSort);
        invalidProperties.removeAll(allowedSortProperties);
        
        if (!invalidProperties.isEmpty()) {
            throw new IllegalArgumentException(
                "Campos de ordenação não permitidos: " + invalidProperties + 
                ". Campos permitidos: " + allowedSortProperties
            );
        }
    }
}