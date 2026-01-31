package com.gastapp.service;

import com.gastapp.model.Category;
import com.gastapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> findAllByUserId(UUID userId) {
        return categoryRepository.findByUserIdOrderByNombreAsc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByIdAndUserId(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        if (!categoryRepository.existsByIdAndUserId(id, userId)) {
            return false;
        }
        categoryRepository.deleteById(id);
        return true;
    }
}
