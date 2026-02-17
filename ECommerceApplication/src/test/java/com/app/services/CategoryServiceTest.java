package com.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.app.entites.Category;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CategoryDTO;
import com.app.payloads.CategoryResponse;
import com.app.repositories.CategoryRepo;

/**
 * Test untuk CategoryService - fitur katalog kategori.
 * 
 * Sesuai deskripsi:
 * - Katalog standar
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests - Catalog Category")
class CategoryServiceTest {

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private ProductService productService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(1L);
        categoryDTO.setCategoryName("Electronics");
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Berhasil membuat kategori baru")
        void createCategory_Success() {
            when(categoryRepo.findByCategoryName("Electronics")).thenReturn(null);
            when(categoryRepo.save(any(Category.class))).thenReturn(category);
            when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

            CategoryDTO result = categoryService.createCategory(category);

            assertNotNull(result);
            assertEquals("Electronics", result.getCategoryName());
            verify(categoryRepo).save(category);
        }

        @Test
        @DisplayName("Gagal membuat kategori dengan nama yang sudah ada")
        void createCategory_DuplicateName_ThrowsException() {
            when(categoryRepo.findByCategoryName("Electronics")).thenReturn(category);

            APIException exception = assertThrows(APIException.class,
                () -> categoryService.createCategory(category));

            assertTrue(exception.getMessage().contains("exists"));
            verify(categoryRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Categories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("Berhasil mendapatkan daftar kategori dengan pagination")
        void getCategories_WithPagination_Success() {
            List<Category> categories = Arrays.asList(category);
            Page<Category> categoryPage = new PageImpl<>(categories);

            when(categoryRepo.findAll(any(Pageable.class))).thenReturn(categoryPage);
            when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

            CategoryResponse result = categoryService.getCategories(0, 10, "categoryId", "asc");

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("Electronics", result.getContent().get(0).getCategoryName());
        }

        @Test
        @DisplayName("Response pagination memiliki informasi page yang benar")
        void getCategories_HasCorrectPaginationInfo() {
            List<Category> categories = Arrays.asList(category);
            Page<Category> categoryPage = new PageImpl<>(categories);

            when(categoryRepo.findAll(any(Pageable.class))).thenReturn(categoryPage);
            when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

            CategoryResponse result = categoryService.getCategories(0, 10, "categoryId", "asc");

            assertNotNull(result.getPageNumber());
            assertNotNull(result.getPageSize());
            assertNotNull(result.getTotalPages());
            assertNotNull(result.getTotalElements());
        }

        @Test
        @DisplayName("Exception saat tidak ada kategori ditemukan")
        void getCategories_Empty_ThrowsException() {
            Page<Category> emptyPage = new PageImpl<>(Arrays.asList());

            when(categoryRepo.findAll(any(Pageable.class))).thenReturn(emptyPage);

            APIException exception = assertThrows(APIException.class,
                () -> categoryService.getCategories(0, 10, "categoryId", "asc"));

            assertTrue(exception.getMessage().toLowerCase().contains("no category"));
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Berhasil mengupdate kategori")
        void updateCategory_Success() {
            Category updatedCategory = new Category();
            updatedCategory.setCategoryId(1L);
            updatedCategory.setCategoryName("Updated Electronics");

            CategoryDTO updatedDTO = new CategoryDTO();
            updatedDTO.setCategoryId(1L);
            updatedDTO.setCategoryName("Updated Electronics");

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepo.save(any(Category.class))).thenReturn(updatedCategory);
            when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(updatedDTO);

            CategoryDTO result = categoryService.updateCategory(updatedCategory, 1L);

            assertNotNull(result);
            assertEquals("Updated Electronics", result.getCategoryName());
            verify(categoryRepo).save(any(Category.class));
        }

        @Test
        @DisplayName("Gagal update kategori yang tidak ditemukan")
        void updateCategory_NotFound_ThrowsException() {
            when(categoryRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(category, 999L));
        }

        @Test
        @DisplayName("Update kategori menyimpan dengan categoryId yang benar")
        void updateCategory_SetsCategoryId() {
            Category updatedCategory = new Category();
            updatedCategory.setCategoryName("New Name");

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepo.save(any(Category.class))).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                assertEquals(1L, c.getCategoryId());
                return c;
            });
            when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);

            categoryService.updateCategory(updatedCategory, 1L);

            verify(categoryRepo).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Berhasil menghapus kategori tanpa produk")
        void deleteCategory_Success() {
            category.setProducts(new java.util.ArrayList<>());
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
            doNothing().when(categoryRepo).delete(category);

            String result = categoryService.deleteCategory(1L);

            assertNotNull(result);
            assertTrue(result.contains("deleted"));
            verify(categoryRepo).delete(category);
        }

        @Test
        @DisplayName("Gagal menghapus kategori yang tidak ditemukan")
        void deleteCategory_NotFound_ThrowsException() {
            when(categoryRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(999L));

            verify(categoryRepo, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Category Validation Tests")
    class CategoryValidationTests {

        @Test
        @DisplayName("Kategori memiliki ID yang valid")
        void category_HasValidId() {
            assertNotNull(category.getCategoryId());
            assertTrue(category.getCategoryId() > 0);
        }

        @Test
        @DisplayName("Kategori memiliki nama yang tidak null")
        void category_HasNonNullName() {
            assertNotNull(category.getCategoryName());
            assertFalse(category.getCategoryName().isEmpty());
        }
    }
}
